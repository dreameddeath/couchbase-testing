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

package com.dreameddeath.couchbase.core.process.rest;

import com.dreameddeath.core.process.service.factory.impl.ExecutorClientFactory;
import com.dreameddeath.core.process.service.factory.impl.ExecutorServiceFactory;
import com.dreameddeath.core.process.service.factory.impl.ProcessingServiceFactory;
import com.dreameddeath.core.service.annotation.processor.ServiceExposeAnnotationProcessor;
import com.dreameddeath.core.service.testing.TestingRestServer;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.couchbase.core.process.remote.annotation.processor.ProcessRestServiceProcessor;
import com.dreameddeath.couchbase.core.process.rest.process.TestJobCreateService;
import com.dreameddeath.couchbase.core.process.rest.process.TestJobUpdateService;
import com.dreameddeath.couchbase.core.process.rest.process.rest.RestTestDocJobCreateService;
import com.dreameddeath.couchbase.core.process.rest.process.rest.RestTestDocJobUpdateService;
import com.dreameddeath.testing.AnnotationProcessorTestingWrapper;
import com.dreameddeath.testing.couchbase.CouchbaseBucketSimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
public class RefRestServiceTest extends Assert {
    private static TestingRestServer server;

    private static AnnotationProcessorTestingWrapper.Result generatorResult;
    private static CuratorTestUtils curatorUtils;

    public static void compileTestServiceGen() throws Exception{
        AnnotationProcessorTestingWrapper annotTester = new AnnotationProcessorTestingWrapper();
        annotTester.
                withAnnotationProcessor(new ServiceExposeAnnotationProcessor()).
                withAnnotationProcessor(new ProcessRestServiceProcessor()).
                withTempDirectoryPrefix("ProcessRestServiceGeneratorTest");
        //generatorResult = annotTester.run(TestServicesTest.class.getClassLoader().getResource("testingServiceGen").getPath());
        //assertTrue(generatorResult.getResult());
    }

    @BeforeClass
    public static void initialise() throws Exception{
        compileTestServiceGen();
        curatorUtils = new CuratorTestUtils().prepare(1);
        server = new TestingRestServer("serverTesting", curatorUtils.getClient("TestServicesTest"));
        CouchbaseBucketSimulator cbSimulator = new CouchbaseBucketSimulator("test");
        cbSimulator.start();
        CouchbaseSessionFactory sessionFactory = new CouchbaseSessionFactory.Builder().build();

        //env = new Utils.TestEnvironment("billingOrder", Utils.TestEnvironment.TestEnvType.COUCHBASE_ELASTICSEARCH);

        ExecutorServiceFactory execFactory=new ExecutorServiceFactory();
        ProcessingServiceFactory processFactory=new ProcessingServiceFactory();

        processFactory.addJobProcessingService(TestJobCreateService.class);
        processFactory.addJobProcessingService(TestJobUpdateService.class);
        //executorClientFactory = new ExecutorClientFactory(sessionFactory,execFactory,processFactory);


        server.registerBeanClass("testJobCreate",RestTestDocJobCreateService.class);
        server.registerBeanClass("testJobUpdate",RestTestDocJobUpdateService.class);
        server.registerBeanObject("couchbaseSessionFactory",sessionFactory);
        server.registerBeanObject("stdJobExecutorFactory", new ExecutorClientFactory(sessionFactory,execFactory,processFactory));
        server.start();
        Thread.sleep(100);
    }


    @Test
    public void runTest(){

    }
}