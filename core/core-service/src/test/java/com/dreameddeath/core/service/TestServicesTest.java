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

package com.dreameddeath.core.service;

import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.annotation.processor.ServiceExposeAnnotationProcessor;
import com.dreameddeath.core.service.client.AbstractServiceClientFactory;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.client.rest.RestServiceClientFactory;
import com.dreameddeath.core.service.context.IGlobalContext;
import com.dreameddeath.core.service.context.IGlobalContextFactory;
import com.dreameddeath.core.service.context.feature.ClientFeatureFactory;
import com.dreameddeath.core.service.context.feature.ContextClientFeature;
import com.dreameddeath.core.service.context.feature.UserClientFeature;
import com.dreameddeath.core.service.model.common.ClientInstanceInfo;
import com.dreameddeath.core.service.model.common.ServicesByNameInstanceDescription;
import com.dreameddeath.core.service.registrar.ClientRegistrar;
import com.dreameddeath.core.service.swagger.TestingDocument;
import com.dreameddeath.core.service.testing.DummyContextFactory;
import com.dreameddeath.core.service.testing.TestingRestServer;
import com.dreameddeath.core.service.utils.RestServiceTypeHelper;
import com.dreameddeath.core.service.utils.ServiceObjectMapperConfigurator;
import com.dreameddeath.core.user.AuthorizeAllUser;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.user.IUserFactory;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.dreameddeath.testing.AnnotationProcessorTestingWrapper;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.swagger.models.Model;
import io.swagger.models.Swagger;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static com.dreameddeath.core.model.document.CouchbaseDocument.DocumentFlag;


/**
 * Created by Christophe Jeunesse on 20/03/2015.
 */

public class TestServicesTest extends Assert{
    private static final Logger LOG = LoggerFactory.getLogger(TestServicesTest.class);

    private static TestingRestServer server;

    private static AnnotationProcessorTestingWrapper.Result generatorResult;
    private static CuratorTestUtils curatorUtils;

    public static void compileTestServiceGen() throws Exception{
        AnnotationProcessorTestingWrapper annotTester = new AnnotationProcessorTestingWrapper();
        annotTester.
                withAnnotationProcessor(new ServiceExposeAnnotationProcessor()).
                withTempDirectoryPrefix("ServiceGeneratorTest");
        generatorResult = annotTester.run(TestServicesTest.class.getClassLoader().getResource("testingServiceGen").getPath());
        assertTrue(generatorResult.getResult());
    }

    @BeforeClass
    public static void initialise() throws Exception{
        compileTestServiceGen();
        curatorUtils = new CuratorTestUtils().prepare(1);
        server = new TestingRestServer("serverTesting", curatorUtils.getClient("TestServicesTest"));

        server.registerBeanClass("test",TestServiceRestService.class);
        server.registerBeanClass("testGenImpl",generatorResult.getClass("com.dreameddeath.core.service.gentest.TestServiceGenImpl"));
        server.registerBeanClass("testGen",generatorResult.getClass("com.dreameddeath.core.service.gentest.TestServiceGenImplRestService"));
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
        assertEquals(2, readDescription.getServiceInstanceMap().keySet().size());
        Map<String,Model> listModels = ((Swagger)readDescription.getServiceInstanceMap().get("testService#1.0").get(0).getSpec()).getDefinitions();
        assertEquals(5,listModels.size());
        assertEquals(6,listModels.get("TestingDocument").getProperties().size());
    }

    @Test
    public void testClientRegister() throws Exception {
        ClientRegistrar clientRegistrar = new ClientRegistrar(server.getCuratorClient(),TestingRestServer.DOMAIN, RestServiceTypeHelper.SERVICE_TYPE,server.getDaemonUid().toString(),server.getServerUid().toString());
        AbstractServiceClientFactory clientFactory = new RestServiceClientFactory(server.getServiceDiscoverer(),clientRegistrar);

        IServiceClient client = clientFactory.getClient("testService","1.0");
        Thread.sleep(100);
        assertEquals(1L,server.getClientDiscoverer().getNbInstances("testService","1.0"));
        String connectionString = "http://localhost:"+server.getLocalPort();
        List<ClientInstanceInfo> response = ClientBuilder.newBuilder().build()
                .target(connectionString)
                .register(new JacksonJsonProvider(ObjectMapperFactory.BASE_INSTANCE.getMapper(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR)))
                .path("/listing/services/clients")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<ClientInstanceInfo>>(){});

        assertEquals(1L,response.size());
        assertEquals(server.getDaemonUid().toString(),response.get(0).getDaemonUid());
        assertEquals(server.getServerUid().toString(),response.get(0).getWebServerUid());
        assertEquals(client.getFullName(),response.get(0).getServiceName());
        assertEquals(RestServiceTypeHelper.SERVICE_TYPE,response.get(0).getServiceType());
        assertEquals(client.getUuid().toString(),response.get(0).getUid());
        clientRegistrar.close();
        Thread.sleep(500);
        assertEquals(0L,server.getClientDiscoverer().getNbInstances("testService","1.0"));

        List<ClientInstanceInfo> responseAfter = ClientBuilder.newBuilder().build()
                .target(connectionString)
                .register(new JacksonJsonProvider(ObjectMapperFactory.BASE_INSTANCE.getMapper(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR)))
                .path("/listing/services/clients")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<ClientInstanceInfo>>(){});
        assertEquals(0L,responseAfter.size());

    }


    @Test
    public void testService() throws Exception{
        LOG.debug("Connector port {}", server.getLocalPort());
        RestServiceClientFactory clientFactory = new RestServiceClientFactory(server.getServiceDiscoverer());
        IGlobalContextFactory transcoder = new DummyContextFactory();
        ClientFeatureFactory featureFactory = new ClientFeatureFactory();
        featureFactory.addFeature(new ContextClientFeature(transcoder));
        featureFactory.addFeature(new UserClientFeature(new StandardMockUserFactory()));
        clientFactory.setFeatureFactory(featureFactory);


        TestServiceRestClientImpl service = new TestServiceRestClientImpl();
        //service.setContextTranscoder(transcoder);
        service.setServiceClient(clientFactory.getClient("testService","1.0"));

        ITestService.Input input = new ITestService.Input();
        input.id = "10";
        input.rootId = "20";
        input.otherField = DateTime.now();
        Observable<ITestService.Result> resultObservable= service.runWithRes(null, input);
        ITestService.Result result = resultObservable.toBlocking().single();

        LOG.debug("Result {}", result.id);
        assertEquals(input.id, result.id);
        assertEquals(input.rootId, result.rootId);

        Observable<ITestService.Result> resultGetObservable=service.getWithRes("30", "15");
        ITestService.Result resultGet = resultGetObservable.toBlocking().single();

        LOG.debug("Result {}", resultGet.id);
        assertEquals("30",resultGet.rootId);
        assertEquals("15", resultGet.id);

        Observable<ITestService.Result> resultPutObservable=service.putWithQuery("30", "15");
        ITestService.Result resultPut = resultPutObservable.toBlocking().single();

        LOG.debug("Result {}", resultPut.id);
        assertEquals("30 put",resultPut.rootId);
        assertEquals("15 put", resultPut.id);

        Object serviceGen =generatorResult.getClass("com.dreameddeath.core.service.gentest.TestServiceGenImplRestClient").newInstance();
        serviceGen.getClass().getMethod("setContextFactory",IGlobalContextFactory.class).invoke(serviceGen,transcoder);
        serviceGen.getClass().getMethod("setUserFactory",IUserFactory.class).invoke(serviceGen,new StandardMockUserFactory());
        serviceGen.getClass().getMethod("setServiceClientFactory",AbstractServiceClientFactory.class).invoke(serviceGen,clientFactory);

        Object resultGenObservable = serviceGen.getClass().getMethod("runWithRes", IGlobalContext.class,ITestService.Input.class).invoke(serviceGen,null,input);
        try {
            ITestService.Result resultGen = (ITestService.Result) ((Observable) resultGenObservable).toBlocking().first();
            LOG.debug("Result {}",resultGen);
            assertEquals(input.id+" gen",resultGen.id);
            assertEquals(input.rootId + " gen", resultGen.rootId);
        }
        catch(Exception e){
            throw e;
        }

        Object resultPostGenObservable = serviceGen.getClass().getMethod("getWithRes", String.class,String.class).invoke(serviceGen,"30","15");
        try {
            ITestService.Result resultGen = (ITestService.Result) ((Observable) resultPostGenObservable).toBlocking().first();
            LOG.debug("Result {}", resultGen);
            assertEquals("30 gen", resultGen.rootId);
            assertEquals("15 gen",resultGen.id);
        }
        catch(Exception e){
            throw e;
        }

        Object resultPutGenObservable = serviceGen.getClass().getMethod("putWithQuery", String.class,String.class).invoke(serviceGen,"30","15");
        try {
            ITestService.Result resultGen = (ITestService.Result) ((Observable) resultPutGenObservable).toBlocking().first();
            LOG.debug("Result {}", resultGen);
            assertEquals("30 putgen", resultGen.rootId);
            assertEquals("15 putgen",resultGen.id);
        }
        catch(Exception e){
            throw e;
        }

        Object resultTestingUserDoc = serviceGen.getClass().getMethod("initDocument", IUser.class).invoke(serviceGen, AuthorizeAllUser.INSTANCE);
        try {
            TestingDocument resultGen = (TestingDocument) ((Observable) resultTestingUserDoc).toBlocking().first();
            LOG.debug("Result {}", resultGen);
            assertEquals(1,resultGen.getTestExternalEltList().size());
            assertEquals(1,resultGen.getTestCplxList().size());
            assertEquals("testingdoc/1",resultGen.getBaseMeta().getKey());
            assertEquals(21L,resultGen.getBaseMeta().getCas());
            assertEquals(
                    DocumentFlag.Binary.toInteger()|
                            DocumentFlag.Compressed.toInteger()|
                            DocumentFlag.Deleted.toInteger()
                    ,
                    (long)resultGen.getBaseMeta().getEncodedFlags());
        }
        catch(Exception e){
            throw e;
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
        if(generatorResult!=null){
            generatorResult.cleanUp();
        }
    }
}
