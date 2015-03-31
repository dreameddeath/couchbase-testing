package com.dreameddeath.core.service;

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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;

import java.net.InetAddress;


/**
 * Created by CEAJ8230 on 20/03/2015.
 */
public class TestServicesTest extends Assert{
    private static final Logger LOG = LoggerFactory.getLogger(TestServicesTest.class);

    private final static int ENDPOINT_PORT = 0;
    private final static String ENDPOINT_HOSTNAME = "localhost";
    private final static String ENDPOINT_ADDRESS = String.format("http://%s:%d",ENDPOINT_HOSTNAME,ENDPOINT_PORT);
    public static final String BASE_PATH = "/services";
    private static Server _server;

    @BeforeClass
    public static void initialise() throws Exception{
        CuratorTestUtils curatorUtils = new CuratorTestUtils();
        curatorUtils.prepare(1);
        CuratorFramework curatorClient = curatorUtils.getClient("TestServicesTest");
        TestSpringSelfConfig.setCuratorClient(curatorClient);
        _server = new Server();
        final ServerConnector connector = new ServerConnector(_server);
        _server.addConnector(connector);
        ServletContextHandler contextHandler = new ServletContextHandler();
        _server.setHandler(contextHandler);
        ServletHolder cxfHolder = new ServletHolder("CXF",CXFServlet.class);
        cxfHolder.setInitOrder(1);
        contextHandler.addServlet(cxfHolder, "/*");
        TestSpringSelfConfig.setEndPointDescr(new IRestEndPointDescription() {
            @Override
            public int port() {
                return connector.getLocalPort();
            }

            @Override
            public String path() {
                return "services/";
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
        ServiceDiscoverer serviceDiscoverer = new ServiceDiscoverer(curatorClient, BASE_PATH);

        _server.addLifeCycleListener(new LifeCycleListener(new ServiceRegistrar(curatorClient,
                BASE_PATH
                ),serviceDiscoverer ));


        contextHandler.setInitParameter("contextConfigLocation", "classpath:rest.applicationContext.xml");
        TestServiceRestService service = new TestServiceRestService();
        //service.setEndPoint(endPointDescription);
        TestSpringSelfConfig.registerService("test",service);
        contextHandler.addEventListener(new ContextLoaderListener());
        //contextHandler.addEventListener(new ServiceRegistrarListener(cxfHolder));

        _server.start();

        //server.join();

        /*JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(TestServiceRestService.class);
        List<Object> providers = new ArrayList<>();
        sf.setProviders(providers);
        TestServiceRestService service = new TestServiceRestService();
        service.setEndPoint(new IRestEndPointDescription() {
            @Override
            public int port() {
                return ENDPOINT_PORT;
            }

            @Override
            public String path() {
                return "";
            }

            @Override
            public String host() {
                return ENDPOINT_HOSTNAME;
            }
        });

        sf.setResourceProvider(TestServiceRestService.class,new SingletonResourceProvider(service,true));
        sf.setAddress(ENDPOINT_ADDRESS);
        _server = sf.create();*/

    }

    @Test
    public void testService(){
        LOG.debug("Entering");
    }

    @AfterClass
    public static void stopServer()throws Exception{
        if(_server!=null) {
            if(!_server.isStopped()){_server.stop();}
            _server.destroy();
        }
    }
}
