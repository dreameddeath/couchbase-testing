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

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.dcp.*;
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

/**
 * Created by Christophe Jeunesse on 28/05/2015.
 */
public class CouchbaseBucketSimulatorTest {
    private static Logger LOG = LoggerFactory.getLogger(CouchbaseBucketSimulatorTest.class);

    public static class TestEventHandler implements DCPEventHandler {
        @Override
        public void onEvent(DCPEvent event, long sequence, boolean endOfBatch) throws Exception {
            LOG.debug("Event recieved {}",event.getType());
        }
    }
    public static class TestExceptionHandler implements DCPExceptionHandler {
        @Override
        public void handleEventException(Throwable ex, long sequence, Object event) {
            LOG.error("Event exception",ex);
        }

        @Override
        public void handleOnStartException(Throwable ex) {
            LOG.error("Start exception",ex);
        }

        @Override
        public void handleOnShutdownException(Throwable ex) {
            LOG.error("End exception", ex);
        }
    }

    public static class TestDoc extends CouchbaseDocument{
        public String field1;
        public String field2;

        public String toString(){
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
                String[] values= new String(buf).split(";");
                TestDoc newDoc = new TestDoc();
                newDoc.field1 = values[0];
                newDoc.field2 = values[1];
                return newDoc;
            }

            @Override
            public byte[] encode(TestDoc doc) throws DocumentEncodingException {
                return doc.toString().getBytes();
            }
        });
        CouchbaseBucketSimulator cbSimulator = new CouchbaseBucketSimulator("test");
        cbSimulator.addTranscoder(transcoder);
        CouchbaseDCPEnvironment env = DefaultCouchbaseDCPEnvironment.builder().streamName(UUID.randomUUID().toString()).build();
        CouchbaseDCPConnector connector = new CouchbaseDCPConnectorSimulator(env, Arrays.asList("localhost:8091"),"default","",new TestEventHandler(),new TestExceptionHandler(),cbSimulator);
        connector.run();
        TestDoc testDoc = new TestDoc();
        testDoc.getBaseMeta().setKey("/test/1");
        testDoc.field1 = "test1";
        testDoc.field2 = "test2";
        cbSimulator.add(testDoc,transcoder);

    }

}