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

package com.dreameddeath.core.service.soap;

import com.dreameddeath.core.context.impl.GlobalContextFactoryImpl;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.log.MDCUtils;
import com.dreameddeath.core.service.TestServicesTest;
import com.dreameddeath.core.service.model.common.ClientInstanceInfo;
import com.dreameddeath.core.service.model.common.ServicesByNameInstanceDescription;
import com.dreameddeath.core.service.registrar.ClientRegistrar;
import com.dreameddeath.core.service.soap.cxf.SoapCxfClientFactory;
import com.dreameddeath.core.service.soap.handler.SoapContextClientHandler;
import com.dreameddeath.core.service.soap.handler.SoapHandlerFactory;
import com.dreameddeath.core.service.soap.handler.SoapUserClientHandler;
import com.dreameddeath.core.service.soap.testing.SoapTestingServer;
import com.dreameddeath.core.service.testing.TestingRestServer;
import com.dreameddeath.core.service.utils.ServiceObjectMapperConfigurator;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.dreameddeath.interfaces.test.v0.data.TestResponse;
import com.dreameddeath.interfaces.test.v0.data.in.OffersCCO;
import com.dreameddeath.interfaces.test.v0.data.in.TestRequest;
import com.dreameddeath.interfaces.test.v0.message.TestWebService;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 07/09/2016.
 */
public class SoapServiceTest extends Assert {
    private static final Logger LOG = LoggerFactory.getLogger(TestServicesTest.class);

    private static SoapTestingServer server;

    private static CuratorTestUtils curatorUtils;


    @BeforeClass
    public static void initialise() throws Exception{
        curatorUtils = new CuratorTestUtils().prepare(1);
        server = new SoapTestingServer("serverTesting", curatorUtils.getClient("TestServicesTest"));

        server.start();
        Thread.sleep(100);
    }

    @Test
    public void testServiceRegister() throws Exception {
        LOG.debug("Connector port {}", server.getLocalPort());
        String connectionString = "http://localhost:"+server.getLocalPort();
        Response response = ClientBuilder.newBuilder().build()
                .target(connectionString)
                .register(new JacksonJsonProvider(ObjectMapperFactory.BASE_INSTANCE.getMapper(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR)))
                .path("/listing/services/instances")
                .request(MediaType.APPLICATION_JSON_TYPE).get();
        LOG.debug("Response {}", response.getStatus());
        ServicesByNameInstanceDescription readDescription = response.readEntity(ServicesByNameInstanceDescription.class);
        assertEquals(1, readDescription.getServiceInstanceMap().keySet().size());
        //Map<String,Model> listModels = ((Swagger)readDescription.getServiceInstanceMap().get("testService#1.0").get(0).getSpec()).getDefinitions();
    }

    @Test
    public void testClient() throws Exception {
        ClientRegistrar clientRegistrar = new ClientRegistrar(server.getCuratorClient(),TestingRestServer.DOMAIN, SoapServiceTypeHelper.SERVICE_TYPE,server.getDaemonUid().toString(),server.getServerUid().toString());
        SoapCxfClientFactory<TestWebService> clientFactory = new SoapCxfClientFactory(server.getServiceDiscoverer(),clientRegistrar);
        GlobalContextFactoryImpl globalContextFactory = new GlobalContextFactoryImpl();
        globalContextFactory.setUserFactory(new StandardMockUserFactory());

        SoapHandlerFactory handlerFactory = new SoapHandlerFactory();
        handlerFactory.addHandler(new SoapUserClientHandler(new StandardMockUserFactory()));
        handlerFactory.addHandler(new SoapContextClientHandler(globalContextFactory));
        handlerFactory.addHandler(new TestTraceIdHandler());
        clientFactory.setHandlerFactory(handlerFactory);
        String connectionString = "http://localhost:" + server.getLocalPort();


        ISoapClient<TestWebService> soapClient=clientFactory.getClient("TestWebserviceSoap","1.0");
        Thread.sleep(500);
        {
            TestRequest request = new TestRequest();
            for (int num = 0; num < 5; num++) {
                OffersCCO offer = new OffersCCO();
                offer.setCode("CODE_" + num);
                offer.setLabel("LABEL_" + num);
                request.getOffer().add(offer);
            }
            //TestWebService instance = soapClient.getInstance();
            MDCUtils.setGlobalTraceId("TEST_GID1");
            MDCUtils.setTraceId("TEST_TID1");
            TestResponse response = soapClient.getInstance().testOperation(request);
            assertNotNull(response);
            assertEquals(request.getOffer().size(), response.getOffer().size());
            assertNotNull(response.getTid());
            //assertE
        }

        {
            assertEquals(1L, server.getClientDiscoverer().getNbInstances("TestWebserviceSoap", "1.0"));
            List<ClientInstanceInfo> response = ClientBuilder.newBuilder().build()
                    .target(connectionString)
                    .register(new JacksonJsonProvider(ObjectMapperFactory.BASE_INSTANCE.getMapper(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR)))
                    .path("/listing/services/clients")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(new GenericType<List<ClientInstanceInfo>>() {
                    });

            assertEquals(1L, response.size());
            assertEquals(server.getDaemonUid().toString(), response.get(0).getDaemonUid());
            assertEquals(server.getServerUid().toString(), response.get(0).getWebServerUid());
            assertEquals(soapClient.getFullName(), response.get(0).getServiceName());
            assertEquals(soapClient.getUuid().toString(), response.get(0).getUid());
        }
        clientRegistrar.close();
        Thread.sleep(500);
        assertEquals(0L,server.getClientDiscoverer().getNbInstances("TestWebserviceSoap","1.0"));
        {
            List<ClientInstanceInfo> responseAfter = ClientBuilder.newBuilder().build()
                    .target(connectionString)
                    .register(new JacksonJsonProvider(ObjectMapperFactory.BASE_INSTANCE.getMapper(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR)))
                    .path("/listing/services/clients")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(new GenericType<List<ClientInstanceInfo>>() {
                    });
            assertEquals(0L, responseAfter.size());
        }
    }

    @AfterClass
    public static void stopServer()throws Exception{
        if(server!=null) {
            server.stop();
        }
        if(curatorUtils!=null){
            try {
                curatorUtils.stop();
            }
            catch(Throwable e){
                LOG.warn("failed to cleanup",e);
            }
        }
    }
}
