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

package com.dreameddeath.core.service;

import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.core.service.context.IGlobalContext;
import com.dreameddeath.core.service.context.IGlobalContextTranscoder;
import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.registrar.IRestEndPointDescription;
import com.dreameddeath.core.service.registrar.ServiceRegistrar;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import rx.Observable;

import java.net.InetAddress;


/**
 * Created by CEAJ8230 on 20/03/2015.
 */

public class TestServicesTest extends Assert{
    private static final Logger LOG = LoggerFactory.getLogger(TestServicesTest.class);

    private static final String BASE_PATH = "/services";
    private static Server _server;
    private static ServiceDiscoverer _serviceDiscoverer;
    private static ServerConnector _connector;

    @BeforeClass
    public static void initialise() throws Exception{
        CuratorTestUtils curatorUtils = new CuratorTestUtils();
        curatorUtils.prepare(1);
        CuratorFramework curatorClient = curatorUtils.getClient("TestServicesTest");
        TestSpringSelfConfig.setCuratorClient(curatorClient);
        _server = new Server();
        _connector = new ServerConnector(_server);
        _server.addConnector(_connector);
        ServletContextHandler contextHandler = new ServletContextHandler();
        _server.setHandler(contextHandler);
        ServletHolder cxfHolder = new ServletHolder("CXF",CXFServlet.class);
        cxfHolder.setInitOrder(1);
        contextHandler.addServlet(cxfHolder, "/*");
        TestSpringSelfConfig.setEndPointDescr(new IRestEndPointDescription() {
            @Override
            public int port() {
                return _connector.getLocalPort();
            }

            @Override
            public String path() {
                return "";
            }

            @Override
            public String host() {
                 try{
                     return InetAddress.getLocalHost().getHostAddress();
                 }
                 catch(Exception e){
                     return "localhost";
                 }
            }
        });
         _serviceDiscoverer = new ServiceDiscoverer(curatorClient, BASE_PATH);

        _server.addLifeCycleListener(new LifeCycleListener(new ServiceRegistrar(curatorClient,BASE_PATH),_serviceDiscoverer ));


        contextHandler.setInitParameter("contextConfigLocation", "classpath:rest.applicationContext.xml");
        TestServiceRestService service = new TestServiceRestService();
        TestSpringSelfConfig.registerService("test",service);
        contextHandler.addEventListener(new ContextLoaderListener());

        _server.start();

    }

    @Test
    public void testService(){
        TestServiceRestClientImpl service = new TestServiceRestClientImpl();
        service.setContextTranscoder(new IGlobalContextTranscoder() {
            @Override
            public String encode(IGlobalContext ctxt) {
                return "";
            }

            @Override
            public IGlobalContext decode(String encodedContext) {
                return null;
            }
        });

        service.setServiceClientFactory(new ServiceClientFactory(_serviceDiscoverer));

        ITestService.Input input = new ITestService.Input();
        input.id = "10";
        input.rootId = "20";
        input.otherField = DateTime.now();
        Observable<ITestService.Result> resultObservable= service.runWithRes(null, input);
        ITestService.Result result = resultObservable.toBlocking().single();

        LOG.debug("Entering",result.id);
    }

    @AfterClass
    public static void stopServer()throws Exception{
        if(_server!=null) {
            if(!_server.isStopped()){_server.stop();}
            _server.destroy();
        }
    }
}
