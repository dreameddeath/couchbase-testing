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

package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import org.junit.Test;
import rx.Observable;

import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Created by Christophe Jeunesse on 27/12/2015.
 */
public class CouchbaseDocumentWithKeyPatternDaoTest {
    public static class TestDoc extends CouchbaseDocument{
        public String toto;
        public String tutu;
        public String titi;
    }

    public static class TestWithKeyPattern extends CouchbaseDocumentWithKeyPatternDao<TestDoc>{
        @Override
        protected String getKeyRawPattern() {
            return "test/{ tutu : \\w+ }/sub/{ toto : [^/]{1,} }/sub2/{titi}/subStd/\\d{2}";
        }

        @Override
        public Class<? extends BucketDocument<TestDoc>> getBucketDocumentClass() {
            return null;
        }

        @Override
        public Observable<TestDoc> asyncBuildKey(ICouchbaseSession session, TestDoc newObject) {
            return null;
        }

        @Override
        public String getKeyFromParams(Object... params) {
            return null;
        }

        @Override
        protected TestDoc updateTransientFromKeyPattern(TestDoc obj, String... params) {
            obj.tutu=params[0];
            obj.toto=params[1];
            obj.titi=params[2];
            return obj;
        }

        public TestDoc unitTestParamExtract(TestDoc doc,String key){
            return updateTransientFromKeyPattern(doc,getKeyPattern().extractParamsArrayFromKey(key));
        }
    }


    @Test
    public void testKeyPattern(){
        TestWithKeyPattern dao =new TestWithKeyPattern();
        dao.init();
        Pattern pattern =dao.getKeyPattern().getKeyPattern();
        assertTrue(pattern.matcher("test/a2ezee/sub/e-2-azfbb/sub2/test/subStd/22").matches());
        assertFalse(pattern.matcher("test/a2e!zee/sub/e-2-azfbb/sub2/test/subStd/22").matches());
        Map<String,String> matchingExtractionResult = dao.getKeyPattern().extractParamsFromKey("test/a2ezee/sub/e-2-azfbb/sub2/test/subStd/22");
        assertEquals(3,matchingExtractionResult.size());
        assertEquals("a2ezee",matchingExtractionResult.get("tutu"));
        assertEquals("e-2-azfbb",matchingExtractionResult.get("toto"));
        assertEquals("test",matchingExtractionResult.get("titi"));

        TestDoc test=new TestDoc();
        test = dao.unitTestParamExtract(test,"test/a2ezee/sub/e-2-azfbb/sub2/test/subStd/22");
        assertEquals("a2ezee",test.tutu);
        assertEquals("e-2-azfbb",test.toto);
        assertEquals("test",test.titi);
    }
}