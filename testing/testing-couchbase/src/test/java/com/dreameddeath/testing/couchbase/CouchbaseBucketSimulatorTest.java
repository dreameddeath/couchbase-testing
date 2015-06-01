/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.testing.couchbase;

import com.couchbase.client.core.message.dcp.MutationMessage;
import com.couchbase.client.core.message.dcp.RemoveMessage;
import com.couchbase.client.core.message.dcp.SnapshotMarkerMessage;
import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.dcp.ICouchbaseDCPEnvironment;
import com.dreameddeath.core.couchbase.dcp.exception.HandlerException;
import com.dreameddeath.core.couchbase.dcp.impl.AbstractDCPFlowHandler;
import com.dreameddeath.core.couchbase.dcp.impl.DefaultCouchbaseDCPEnvironment;
import com.dreameddeath.core.couchbase.impl.GenericCouchbaseTranscoder;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.transcoder.DocumentDecodingException;
import com.dreameddeath.core.model.exception.transcoder.DocumentEncodingException;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import com.dreameddeath.testing.couchbase.dcp.CouchbaseDCPConnectorSimulator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by Christophe Jeunesse on 28/05/2015.
 */
public class CouchbaseBucketSimulatorTest {
    private static Logger LOG = LoggerFactory.getLogger(CouchbaseBucketSimulatorTest.class);

    public static class TestDCPFlowHandler extends AbstractDCPFlowHandler{
        private int errors=0;
        private int mutations=0;
        private int deletions=0;
        public TestDCPFlowHandler(ITranscoder<TestDoc> transcoder){
            super(transcoder);
        }

        @Override
        public void manageSnapshotMessage(SnapshotMarkerMessage message) {

        }

        @Override
        public void manageMutationMessage(MutationMessage message, CouchbaseDocument mappedObject) {
            LOG.debug("Mutation message received {}", message.key());
            if(mutations<=3) {
                assertEquals("/test/1", mappedObject.getBaseMeta().getKey());
                assertEquals(mutations + 1, mappedObject.getBaseMeta().getCas());
                assertEquals(0, mappedObject.getBaseMeta().getEncodedFlags().intValue());
            }
            else{
                assertEquals(mutations - 3, mappedObject.getBaseMeta().getCas());
                assertEquals("/test/cnt", mappedObject.getBaseMeta().getKey());
            }
            assertEquals(mappedObject.getClass(), TestDoc.class);
            TestDoc doc = (TestDoc) mappedObject;
            if(mutations==0) {
                assertEquals("test1", doc.field1);
                assertEquals("test2", doc.field2);
            }
            else if(mutations==1){
                assertEquals("test1", doc.field1);
                assertEquals("test2update", doc.field2);
            }
            else if(mutations==2){
                assertEquals("prependtest1", doc.field1);
                assertEquals("test2update", doc.field2);
            }
            else if(mutations==3){
                assertEquals("prependtest1", doc.field1);
                assertEquals("test2updateappend", doc.field2);
            }
            else if(mutations==4){
                assertEquals("2", doc.field1);
                assertEquals(null, doc.field2);
            }
            else if(mutations==5){
                assertEquals("4", doc.field1);
                assertEquals(null, doc.field2);
            }

            mutations++;

        }

        @Override
        public void manageDeletionMessage(RemoveMessage message) {
            assertEquals("/test/1",message.key());
            deletions++;
        }

        @Override
        public void manageException(HandlerException message) {
            errors++;
        }
    }

    public static class TestDoc extends CouchbaseDocument{
        public String field1;
        public String field2;

        public String toString(){
            if(field1==null){ return field2; }
            if(field2==null){ return field1; }
            return field1+";"+field2;
        }

    }

    public static class LocalBucketDocument extends BucketDocument<TestDoc> {
        public LocalBucketDocument(TestDoc obj){super(obj);}
    }


    @Test
    public void testCouchbaseDCPSimulator()throws Exception{
        GenericCouchbaseTranscoder<TestDoc> transcoder =new GenericCouchbaseTranscoder<>(TestDoc.class,LocalBucketDocument.class);
        transcoder.setTranscoder(new ITranscoder<TestDoc>() {
            @Override
            public Class<TestDoc> getBaseClass() {
                return TestDoc.class;
            }

            @Override
            public TestDoc decode(byte[] buf) throws DocumentDecodingException {
                String[] values = new String(buf).split(";");
                TestDoc newDoc = new TestDoc();
                newDoc.field1 = values[0];
                if(values.length>1) {
                    newDoc.field2 = values[1];
                }
                else{
                    newDoc.field2=null;
                }
                return newDoc;
            }

            @Override
            public byte[] encode(TestDoc doc) throws DocumentEncodingException {
                return doc.toString().getBytes();
            }
        });
        CouchbaseBucketSimulator cbSimulator = new CouchbaseBucketSimulator("test");
        cbSimulator.addTranscoder(transcoder);
        ICouchbaseDCPEnvironment env = DefaultCouchbaseDCPEnvironment.builder().streamName(UUID.randomUUID().toString()).threadPoolSize(1).build();
        TestDCPFlowHandler handler = new TestDCPFlowHandler(transcoder.getTranscoder());
        CouchbaseDCPConnectorSimulator connector = new CouchbaseDCPConnectorSimulator(env, Arrays.asList("localhost:8091"),"default","",handler,cbSimulator);
        connector.run();
        TestDoc testDoc = new TestDoc();
        testDoc.getBaseMeta().setKey("/test/1");
        testDoc.field1 = "test1";
        testDoc.field2 = "test2";
        cbSimulator.add(testDoc, transcoder);
        testDoc.field2 += "update";
        cbSimulator.replace(testDoc,transcoder);
        testDoc.field1="prepend";
        testDoc.field2=null;
        cbSimulator.prepend(testDoc, transcoder);
        testDoc.field1=null;
        testDoc.field2="append";
        cbSimulator.append(testDoc, transcoder);

        cbSimulator.counter("/test/cnt", 1L, 2L);
        cbSimulator.counter("/test/cnt",2L);
        cbSimulator.delete(testDoc, transcoder);
        connector.stop();

        assertEquals(0, handler.errors);
        assertEquals(6,handler.mutations);
        assertEquals(1,handler.deletions);

    }

}