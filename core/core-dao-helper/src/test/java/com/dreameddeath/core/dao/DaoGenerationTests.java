/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.dao;

import com.dreameddeath.core.dao.helper.annotation.processor.DaoAnnotationProcessor;
import com.dreameddeath.testing.AnnotationProcessorTestingWrapper;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by CEAJ8230 on 14/04/2015.
 */
public class DaoGenerationTests extends Assert {
    CouchbaseBucketSimulator _couchbaseClient;
    AnnotationProcessorTestingWrapper.Result _compiledEnv;

    @Before
    public void initTest() throws  Exception{
        _couchbaseClient = new CouchbaseBucketSimulator("test","test");
        AnnotationProcessorTestingWrapper annotTester = new AnnotationProcessorTestingWrapper();
        annotTester.
                withAnnotationProcessor(new DaoAnnotationProcessor()).
                withTempDirectoryPrefix("DaoAnnotationProcessorTest");
        _compiledEnv= annotTester.run(this.getClass().getClassLoader().getResource("daoSourceFiles").getPath());
        assertTrue(_compiledEnv.getResult());

    }

    @Test
    public void runAnnotationProcessor() throws Exception {

    }

    @After
    public void cleanupTest(){
        if(_couchbaseClient!=null) {
            _couchbaseClient.shutdown();
        }
        if(_compiledEnv!=null) {
            _compiledEnv.cleanUp();
        }
    }


}
