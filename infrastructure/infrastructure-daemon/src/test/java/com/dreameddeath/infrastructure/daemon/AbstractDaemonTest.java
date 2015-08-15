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
import com.dreameddeath.testing.curator.CuratorTestUtils;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
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
        String connectionString = _testUtils.getCluster().getConnectString();
        ConfigManagerFactory.addConfigurationEntry(CommonConfigProperties.ZOOKEEPER_CLUSTER_ADDREES.getName(),connectionString);
        //TODO start zookeeper
        final AbstractDaemon daemon=new AbstractDaemon();
        Thread stopping_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    StatusResponse response = daemon.getServiceDiscoveryManager().getClientFactory("admin/services")
                            .getClient("daemon#admin#status", "1.0")
                            .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                            .path("/status")
                            .request(MediaType.APPLICATION_JSON)
                            .get(StatusResponse.class);
                    assertEquals(AbstractDaemon.Status.STARTED,response.getStatus());
                }
                catch(Exception e){
                    LOG.error("Error during status read", e);
                }
                try {
                    LOG.info("Request stopping the web server");
                    Thread.sleep(1000);
                    StatusUpdateRequest request = new StatusUpdateRequest();
                    request.setStatus(StatusUpdateRequest.Status.STOP);
                    StatusResponse response= daemon.getServiceDiscoveryManager().getClientFactory("admin/services")
                            .getClient("daemon#admin#status", "1.0")
                            .register(new JacksonJsonProvider(ServiceJacksonObjectMapper.getInstance()))
                            .path("/status")
                            .request(MediaType.APPLICATION_JSON)
                            .put(Entity.json(request),StatusResponse.class);

                    //assertEquals(AbstractDaemon.Status.STOPPED, response.getStatus());
                    return;
                }
                catch(Exception e){
                    LOG.error("Cannot call stop", e);
                }

                try {
                    daemon.getDaemonLifeCycle().stop();
                }
                catch(Exception e){
                    LOG.error("Cannot stop", e);
                }
                fail("Shoudn't have to call stop");
            }
        });
        daemon.getDaemonLifeCycle().addLifeCycleListener(new IDaemonLifeCycle.Listener() {
            @Override
            public void lifeCycleStarting(IDaemonLifeCycle lifeCycle) {
                LOG.info("The web server is starting");
                assertEquals(AbstractDaemon.Status.STARTING, lifeCycle.getDaemon().getStatus());
            }

            @Override
            public void lifeCycleStarted(final IDaemonLifeCycle lifeCycle) {
                LOG.info("The web server is started");
                assertEquals(AbstractDaemon.Status.STARTING, lifeCycle.getDaemon().getStatus());
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
            public void lifeCycleStopping(IDaemonLifeCycle lifeCycle) {
                assertEquals(AbstractDaemon.Status.STOPPING,lifeCycle.getDaemon().getStatus());
            }

            @Override
            public void lifeCycleStopped(IDaemonLifeCycle lifeCycle) {
                assertEquals(AbstractDaemon.Status.STOPPING,lifeCycle.getDaemon().getStatus());
            }
        });
        daemon.startAndJoin();
        stopping_thread.join();
    }
}