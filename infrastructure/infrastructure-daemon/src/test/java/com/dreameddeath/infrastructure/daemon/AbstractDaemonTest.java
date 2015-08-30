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

package com.dreameddeath.infrastructure.daemon;

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.service.utils.ServiceJacksonObjectMapper;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.services.model.StatusResponse;
import com.dreameddeath.infrastructure.daemon.services.model.StatusUpdateRequest;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import com.dreameddeath.infrastructure.daemon.webserver.ProxyWebServer;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 14/08/2015.
 */
public class AbstractDaemonTest extends Assert {
    private static final Logger LOG =  LoggerFactory.getLogger(AbstractDaemonTest.class);
    private CuratorTestUtils _testUtils;
    @Before
    public void setup() throws Exception{
        _testUtils = new CuratorTestUtils();
        _testUtils.prepare(1);
    }

    @Test
    public void testDaemon() throws Exception{
        final AtomicInteger nbErrors=new AtomicInteger(0);
        String connectionString = _testUtils.getCluster().getConnectString();
        ConfigManagerFactory.addConfigurationEntry(CommonConfigProperties.ZOOKEEPER_CLUSTER_ADDREES.getName(), connectionString);
        //TODO start zookeeper
        final AbstractDaemon daemon=new AbstractDaemon();

        daemon.addStandardWebServer(RestWebServer.builder().withName("tests").withApplicationContextConfig("applicationContext.xml"));
        daemon.addProxyWebServer(ProxyWebServer.builder().withPort(8080).withAddress("127.0.0.1").withName("proxy").withDiscoverPath("tests/services"));
        Thread stopping_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Integer response =ClientBuilder.newClient()
                            .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                            .target("http://127.0.0.1:8080")
                            .path("/proxy-apis/tests#tests#tests/1.0")
                            .request()
                            .get(Integer.class);
                    assertEquals(12L, response.longValue());

                    Integer responseQuery =ClientBuilder.newClient()
                            .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                            .target("http://127.0.0.1:8080")
                            .path("/proxy-apis/tests#tests#tests/1.0/23")
                            .queryParam("qnb","3")
                            .request()
                            .get(Integer.class);
                    assertEquals(12L+23+3,responseQuery.longValue());
                }
                catch(Exception e){
                    nbErrors.incrementAndGet();
                    LOG.error("Error during status read", e);
                }
                try {

                    Integer response = ((RestWebServer)daemon.getStandardWebServers().get(0)).getServiceDiscoveryManager().getClientFactory("tests/services")
                            .getClient("tests#tests#tests", "1.0")
                            .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                            //.path("/status")
                            .request(MediaType.APPLICATION_JSON)
                            .get(Integer.class);
                    assertEquals(12L,response.longValue());
                }
                catch(Exception e){
                    nbErrors.incrementAndGet();
                    LOG.error("Error during status read", e);
                }


                try {
                    StatusResponse response = daemon.getAdminWebServer().getServiceDiscoveryManager().getClientFactory("admin/services")
                            .getClient("daemon#admin#status", "1.0")
                            .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                            .path("/status")
                            .request(MediaType.APPLICATION_JSON)
                            .get(StatusResponse.class);
                    assertEquals(AbstractDaemon.Status.STARTED,response.getStatus());
                }
                catch(Exception e){
                    nbErrors.incrementAndGet();
                    LOG.error("Error during status read", e);
                }
                try {
                    LOG.info("Request halting the web server");
                    Thread.sleep(1000);
                    StatusUpdateRequest request = new StatusUpdateRequest();
                    request.setStatus(StatusUpdateRequest.Status.HALT);
                    StatusResponse response= daemon.getAdminWebServer().getServiceDiscoveryManager().getClientFactory("admin/services")
                            .getClient("daemon#admin#status", "1.0")
                            .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                            .path("/status")
                            .request(MediaType.APPLICATION_JSON)
                            .put(Entity.json(request), StatusResponse.class);

                    assertEquals(AbstractDaemon.Status.HALTED, response.getStatus());
                }
                catch(Exception e){
                    nbErrors.incrementAndGet();
                    LOG.error("Cannot call halt", e);
                }

                try {
                    LOG.info("Request stopping the web server");
                    Thread.sleep(1000);
                    StatusUpdateRequest request = new StatusUpdateRequest();
                    request.setStatus(StatusUpdateRequest.Status.STOP);
                    StatusResponse response= daemon.getAdminWebServer().getServiceDiscoveryManager().getClientFactory("admin/services")
                            .getClient("daemon#admin#status", "1.0")
                            .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                            .path("/status")
                            .request(MediaType.APPLICATION_JSON)
                            .put(Entity.json(request),StatusResponse.class);

                    assertEquals(AbstractDaemon.Status.STOPPING, response.getStatus());
                    return;
                }
                catch(Exception e){
                    nbErrors.incrementAndGet();
                    LOG.error("Cannot call stop", e);
                }

                try {
                    nbErrors.incrementAndGet();
                    daemon.getDaemonLifeCycle().stop();
                }
                catch(Exception e){
                    LOG.error("Cannot stop", e);
                }

                nbErrors.incrementAndGet();
                fail("Shoudn't have to call stop");
            }
        });
        daemon.getDaemonLifeCycle().addLifeCycleListener(new IDaemonLifeCycle.Listener() {
            @Override
            public void lifeCycleStarting(IDaemonLifeCycle lifeCycle) {
                LOG.info("The web server is starting");
                assertEquals(AbstractDaemon.Status.STARTING, lifeCycle.getDaemon().getStatus());
                for(AbstractWebServer server:lifeCycle.getDaemon().getStandardWebServers()){
                    assertEquals(true,server.getLifeCycle().isStopped());
                }
                assertEquals(true,daemon.getAdminWebServer().getLifeCycle().isStarted());
            }

            @Override
            public void lifeCycleStarted(final IDaemonLifeCycle lifeCycle) {
                LOG.info("The web server is started");
                assertEquals(AbstractDaemon.Status.STARTING, lifeCycle.getDaemon().getStatus());
                for(AbstractWebServer server:lifeCycle.getDaemon().getStandardWebServers()){
                    assertEquals(true,server.getLifeCycle().isStarted());
                }
                assertEquals(true,daemon.getAdminWebServer().getLifeCycle().isStarted());
                stopping_thread.start();
                //TODO perform calls to rest API, ...
            }

            @Override
            public void lifeCycleFailure(IDaemonLifeCycle lifeCycle, Throwable exception) {

            }

            @Override
            public void lifeCycleReload(IDaemonLifeCycle lifeCycle) {

            }

            @Override
            public void lifeCycleHalt(IDaemonLifeCycle lifeCycle) {
                for(AbstractWebServer server:lifeCycle.getDaemon().getStandardWebServers()){
                    assertEquals(true,server.getLifeCycle().isStopped());
                }
                assertEquals(true,daemon.getAdminWebServer().getLifeCycle().isStarted());
                assertEquals(AbstractDaemon.Status.STARTED,lifeCycle.getDaemon().getStatus());
            }

            @Override
            public void lifeCycleStopping(IDaemonLifeCycle lifeCycle) {
                for(AbstractWebServer server:lifeCycle.getDaemon().getStandardWebServers()){
                    assertEquals(true,server.getLifeCycle().isStopped());
                }
                assertEquals(true,daemon.getAdminWebServer().getLifeCycle().isStarted());
                assertEquals(AbstractDaemon.Status.STOPPING,lifeCycle.getDaemon().getStatus());
            }

            @Override
            public void lifeCycleStopped(IDaemonLifeCycle lifeCycle) {
                for(AbstractWebServer server:lifeCycle.getDaemon().getStandardWebServers()){
                    assertEquals(true,server.getLifeCycle().isStopped());
                }
                assertEquals(true,daemon.getAdminWebServer().getLifeCycle().isStopped());
                assertEquals(AbstractDaemon.Status.STOPPING,lifeCycle.getDaemon().getStatus());
            }
        });
        daemon.startAndJoin();
        stopping_thread.join();

        assertEquals(0L,nbErrors.get());
    }
}