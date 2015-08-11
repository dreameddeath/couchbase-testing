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

package com.dreameddeath.core.couchbase;


import com.dreameddeath.core.helper.annotation.processor.DaoGeneratorAnnotationProcessor;
import com.dreameddeath.testing.AnnotationProcessorTestingWrapper;
import com.dreameddeath.testing.Utils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
/**
 * Created by Christophe Jeunesse on 02/01/2015.
 */
public class DaoGeneratorTest extends Assert {


    Utils.TestEnvironment _env;
    @Before
    public void initTest() throws  Exception{
        _env = new Utils.TestEnvironment("ViewTests", Utils.TestEnvironment.TestEnvType.COUCHBASE);
       // _env.addDocumentDao(new TestDaoProcesorDao(),TestDaoProcessor.class);
        _env.start();
    }

    @Test
    public void runAnnotationProcessor() throws Exception {
        AnnotationProcessorTestingWrapper annotTester = new AnnotationProcessorTestingWrapper();
        annotTester.
                withAnnotationProcessor(new DaoGeneratorAnnotationProcessor()).
                withTempDirectoryPrefix("DaoAnnotationProcessorTest");
        AnnotationProcessorTestingWrapper.Result result = annotTester.run(this.getClass().getClassLoader().getResource("daoSourceFiles").getPath());
        assertTrue(result.getResult());
        assertTrue(result.hasClass("test.dao.TestDaoDao"));
        result.cleanUp();
    }
}
