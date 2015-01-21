package com.dreameddeath.curator.server;

import com.dreameddeath.core.service.registrar.ServiceRegistrar;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;

import java.net.InetAddress;


public class MicroServer {


    public static void main(String args[]) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new ExponentialBackoffRetry(1000, 3));
        client.start();
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        server.addConnector(connector);
        ServletContextHandler contextHandler = new ServletContextHandler();
        server.setHandler(contextHandler);
        ServletHolder cxfHolder = new ServletHolder("CXF",CXFServlet.class);
        cxfHolder.setInitOrder(1);
        contextHandler.addServlet(cxfHolder, "/*");
        server.addLifeCycleListener(new LifeCycleListener(new ServiceRegistrar(client,"/services", InetAddress.getLocalHost().getHostAddress(),connector)));


        contextHandler.setInitParameter("contextConfigLocation", "classpath:rest.applicationContext.xml");
        contextHandler.addEventListener(new ContextLoaderListener());
        contextHandler.addEventListener(new ServiceRegistrarListener(cxfHolder));

        server.start();
        server.join();

        /*JAXRSServerFactoryBean jaxrsServerFactory =
                RuntimeDelegate.getInstance().
                        createEndpoint(new ServiceTestApplication(),
                                JAXRSServerFactoryBean.class);
        jaxrsServerFactory.setAddress("http://localhost:10000");
        jaxrsServerFactory.setProvider(new JacksonJaxbJsonProvider());
        org.apache.cxf.endpoint.Server server = jaxrsServerFactory.create();
        server.start();

        System.out.println("Server started :"+server.getEndpoint().getEndpointInfo().getService().getEndpoints());*/
        //Thread.sleep(5 * 60 * 1000);
        //System.out.println("Server stopping...");
        //server.stop();
        //System.exit(0);
    }

}