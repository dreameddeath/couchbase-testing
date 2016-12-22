/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.infrastructure.plugin.soap;

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.json.JsonProviderFactory;
import com.dreameddeath.core.service.soap.SoapServiceTypeHelper;
import com.dreameddeath.core.service.utils.rest.RestServiceTypeHelper;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.webserver.ProxyWebServer;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.dreameddeath.interfaces.test.v0.data.TestResponse;
import com.dreameddeath.interfaces.test.v0.data.in.TestRequest;
import com.dreameddeath.interfaces.test.v0.message.TestWebService;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 09/09/2016.
 */
public class SoapWebServerPluginTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(SoapWebServerPluginTest.class);
    private CuratorTestUtils testUtils;

    @Before
    public void setup() throws Exception {
        testUtils = new CuratorTestUtils();
        testUtils.prepare(1);
    }

    @Test
    public void testDaemon() throws Exception {
        final AtomicInteger nbErrors = new AtomicInteger(0);
        String connectionString = testUtils.getCluster().getConnectString();

        ConfigManagerFactory.addPersistentConfigurationEntry(CommonConfigProperties.ZOOKEEPER_CLUSTER_ADDREES.getName(), connectionString);
        final AbstractDaemon daemon = AbstractDaemon.builder()
                .withName("testing Daemon")
                .withUserFactory(new StandardMockUserFactory())
                .build();

        daemon.addWebServer(RestWebServer.builder().withName("tests")
                .withPlugin(SoapWebServerPlugin.builder())
                .withApplicationContextConfig("applicationContext.xml"));
        daemon.addWebServer(ProxyWebServer.builder().withPort(8080).withAddress("127.0.0.1")
                .withName("proxy")
                .withDiscoverDomainAndPath(RestServiceTypeHelper.SERVICE_TYPE,"test","REST")
                .withDiscoverDomainAndPath(SoapServiceTypeHelper.SERVICE_TYPE,"test","SOAP")
        );
        Thread stopping_thread = new Thread(() -> {

            try {
                {
                    Integer response = ClientBuilder.newClient()
                            .register(JsonProviderFactory.getProvider("service"))
                            .target("http://127.0.0.1:8080")
                            .path("/proxy-apis/REST/tests#tests#tests/1.0")
                            .request()
                            .get(Integer.class);
                    assertEquals(12L, response.longValue());
                }
                {
                    Integer response = ClientBuilder.newClient()
                            .register(JsonProviderFactory.getProvider("service"))
                            .target("http://127.0.0.1:8080")
                            .path("/proxy-apis/REST/tests#tests#tests/1.0/23")
                            .queryParam("qnb", "3")
                            .request()
                            .get(Integer.class);
                    assertEquals(12L + 23 + 3, response.longValue());
                }
                {
                    JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
                    factoryBean.setServiceClass(TestWebService.class);
                    factoryBean.setAddress("http://127.0.0.1:8080//proxy-apis/SOAP/soapTest/1.0");

                    TestWebService soapTest = (TestWebService) factoryBean.create();
                    TestRequest request = new TestRequest();
                    request.setName("MySelf");
                    TestResponse response = soapTest.testOperation(request);
                    assertNotNull(response);
                    assertEquals(request.getName(),response.getName());
                    assertEquals("Hello "+request.getName(),response.getGreeting());
                }
            }
            catch (Throwable e){

            }
            try {
                daemon.getDaemonLifeCycle().stop();
            }
            catch(Throwable e){

            }
        });
        daemon.getDaemonLifeCycle().addLifeCycleListener(new IDaemonLifeCycle.DefaultListener(1000000) {
            @Override
            public void lifeCycleStarted(final IDaemonLifeCycle lifeCycle) {
                stopping_thread.start();
            }
        });
        daemon.startAndJoin();
        stopping_thread.join();
        assertEquals(0L, nbErrors.get());
    }

    @After
    public void close() throws Exception {
        if (testUtils != null) testUtils.stop();
    }
}