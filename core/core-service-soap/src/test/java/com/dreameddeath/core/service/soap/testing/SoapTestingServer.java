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

package com.dreameddeath.core.service.soap.testing;

import com.dreameddeath.core.service.discovery.ClientDiscoverer;
import com.dreameddeath.core.service.discovery.ProxyClientDiscoverer;
import com.dreameddeath.core.service.registrar.ClientRegistrar;
import com.dreameddeath.core.service.registrar.IEndPointDescription;
import com.dreameddeath.core.service.soap.SoapServiceDiscoverer;
import com.dreameddeath.core.service.soap.SoapServiceRegistrar;
import com.dreameddeath.core.service.soap.SoapServiceTypeHelper;
import com.dreameddeath.core.service.soap.cxf.SoapCxfClientFactory;
import com.dreameddeath.core.service.testing.LifeCycleListener;
import com.google.common.base.Preconditions;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.LifeCycle;
import org.springframework.web.context.ContextLoaderListener;

import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 07/09/2016.
 */
public class SoapTestingServer{
    public static final String DOMAIN = "services";
    protected final Server server;
    protected final UUID daemonUid = UUID.randomUUID();
    protected final UUID serverUid = UUID.randomUUID();

    private final CuratorFramework curatorClient;
    private final ServerConnector connector;
    private final SoapServiceDiscoverer soapServiceDiscoverer;
    private final SoapCxfClientFactory soapServiceClientFactory;
    private final SoapServiceRegistrar soapServiceRegistrar;
    private final ClientRegistrar soapClientRegistrar;
    private final ClientDiscoverer clientDiscoverer;
    private final ProxyClientDiscoverer proxyClientDiscoverer;

    public SoapTestingServer(String testName, CuratorFramework curatorClient) throws Exception {
        this.curatorClient = curatorClient;
        server = new Server();
        connector = new ServerConnector(server);
        server.addConnector(connector);
        ServletContextHandler contextHandler = new ServletContextHandler();
        server.setHandler(contextHandler);
        ServletHolder cxfHolder = new ServletHolder("CXF",CXFServlet.class);
        cxfHolder.setInitOrder(1);
        contextHandler.addServlet(cxfHolder, "/*");
        soapServiceDiscoverer = new SoapServiceDiscoverer(curatorClient, DOMAIN);
        soapServiceRegistrar = new SoapServiceRegistrar(curatorClient, DOMAIN);
        soapClientRegistrar = new ClientRegistrar(curatorClient, SoapServiceTypeHelper.SERVICE_TYPE, DOMAIN,daemonUid.toString(),serverUid.toString());
        soapServiceClientFactory = new SoapCxfClientFactory(soapServiceDiscoverer,soapClientRegistrar);
        clientDiscoverer = new ClientDiscoverer(curatorClient, DOMAIN,SoapServiceTypeHelper.SERVICE_TYPE);
        proxyClientDiscoverer = new ProxyClientDiscoverer(curatorClient,DOMAIN,SoapServiceTypeHelper.SERVICE_TYPE);

        server.addLifeCycleListener(new LifeCycleListener(soapServiceRegistrar, soapServiceDiscoverer));
        server.addLifeCycleListener(new LifeCycle.Listener(){
            @Override public void lifeCycleStarting(LifeCycle lifeCycle) {}
            @Override public void lifeCycleStarted(LifeCycle lifeCycle) {
                try{clientDiscoverer.start();proxyClientDiscoverer.start();}
                catch (Exception e){throw new RuntimeException(e);}
            }
            @Override public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {}
            @Override public void lifeCycleStopping(LifeCycle lifeCycle) {
                try{clientDiscoverer.stop();proxyClientDiscoverer.stop();}
                catch (Exception e){throw new RuntimeException(e);}
            }
            @Override public void lifeCycleStopped(LifeCycle lifeCycle) {}
        });
        contextHandler.setInitParameter("contextConfigLocation", "classpath:soap.applicationContext.xml");
        contextHandler.setAttribute("soapServiceRegistrar", soapServiceRegistrar);
        contextHandler.setAttribute("soapServiceDiscoverer", soapServiceDiscoverer);
        contextHandler.setAttribute("soapClientRegistrar", soapClientRegistrar);
        contextHandler.setAttribute("soapClientDiscoverer", clientDiscoverer);
        contextHandler.setAttribute("soapProxyClientDiscoverer", proxyClientDiscoverer);
        contextHandler.setAttribute("curatorClient", curatorClient);
        contextHandler.setAttribute("endPointInfo", new IEndPointDescription() {
            @Override public String daemonUid() {
                return daemonUid.toString();
            }
            @Override public String webserverUid() {
                return serverUid.toString();
            }
            @Override public int port() {
                return connector.getLocalPort();
            }
            @Override public String path() {
                return "";
            }
            @Override public String host() {
                try {return InetAddress.getLocalHost().getHostAddress();
                } catch (Exception e) {return "localhost";}
            }
            @Override public String buildInstanceUid() {
                return UUID.randomUUID().toString();
            }
        });
        contextHandler.addEventListener(new ContextLoaderListener());
    }

    public SoapCxfClientFactory getClientFactory(){
            return soapServiceClientFactory;
        }

    public void start() throws Exception{
        if(!curatorClient.getState().equals(CuratorFrameworkState.STARTED)) {
            curatorClient.start();
        }
        curatorClient.blockUntilConnected(1, TimeUnit.MINUTES);
        CountDownLatch startingWait=new CountDownLatch(1);
        AtomicInteger errorCounter = new AtomicInteger(0);
        server.addLifeCycleListener(new LifeCycle.Listener(){
            @Override public void lifeCycleStarting(LifeCycle lifeCycle) {}
            @Override public void lifeCycleStarted(LifeCycle lifeCycle) {startingWait.countDown();}
            @Override public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {
                errorCounter.incrementAndGet();
            }
            @Override public void lifeCycleStopping(LifeCycle lifeCycle) {}
            @Override public void lifeCycleStopped(LifeCycle lifeCycle) {}
        });
        server.start();
        Preconditions.checkArgument(startingWait.await(1,TimeUnit.MINUTES),"Starting failed");
        Preconditions.checkArgument(errorCounter.get()==0,"Error occurs");
    }

    public void stop()throws Exception{
        if((server!=null) && !server.isStopped()) {
            server.stop();
        }
        if(curatorClient!=null){
            curatorClient.close();
        }
    }

    public int getLocalPort(){
        return connector.getLocalPort();
    }

    public CuratorFramework getCuratorClient() {
        return curatorClient;
    }

    public UUID getDaemonUid() {
        return daemonUid;
    }

    public UUID getServerUid() {
        return serverUid;
    }

    public SoapServiceDiscoverer getServiceDiscoverer() {
        return soapServiceDiscoverer;
    }

    public ClientDiscoverer getClientDiscoverer() {
        return clientDiscoverer;
    }

}
