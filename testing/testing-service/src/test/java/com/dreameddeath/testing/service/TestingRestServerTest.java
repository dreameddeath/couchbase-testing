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

package com.dreameddeath.testing.service;

import com.dreameddeath.core.service.annotation.processor.ServiceExpositionDef;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 11/04/2015.
 */
public class TestingRestServerTest extends Assert{
    public static final String RESULT_VALUE = "test Successfull";
    public TestingRestServer server;
    public CuratorTestUtils testUtils;

    public interface ITestServer{
        Observable<String> genValue();
    }


    @Before
    public void initServer() throws Exception{
        testUtils = new CuratorTestUtils().prepare(1);
        server = new TestingRestServer("serverTesting", testUtils.getClient("serverTesting"));
        server.registerService("TestService",ServiceExpositionDef.newRestServerInstance(new TestServerImpl()));
        server.start();
    }

    @Test
    public void testServer() throws Exception{
        String value = ServiceExpositionDef.getRestClientIntance(TestServerImpl.class,ITestServer.class,server.getClientFactory()).genValue().toBlocking().first();
        assertEquals(RESULT_VALUE,value);
    }

    @After
    public void stopServer()throws Exception{
        server.stop();
        testUtils.stop();
    }
}