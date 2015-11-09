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
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.dao.discovery.DaoDiscovery;
import com.dreameddeath.core.dao.model.discovery.DaoInstanceInfo;
import com.dreameddeath.core.helper.config.DaoHelperConfigProperties;
import com.dreameddeath.core.helper.service.DaoHelperServiceUtils;
import com.dreameddeath.core.json.JsonProviderFactory;
import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.core.service.utils.ServiceObjectMapperConfigurator;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentObjectMapperConfigurator;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.config.DaemonConfigProperties;
import com.dreameddeath.infrastructure.daemon.discovery.DaemonDiscovery;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.model.DaemonInfo;
import com.dreameddeath.infrastructure.daemon.model.WebServerInfo;
import com.dreameddeath.infrastructure.daemon.services.model.daemon.StatusResponse;
import com.dreameddeath.infrastructure.daemon.services.model.daemon.StatusUpdateRequest;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import com.dreameddeath.infrastructure.daemon.webserver.ProxyWebServer;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.dreameddeath.testing.couchbase.CouchbaseBucketFactorySimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 14/08/2015.
 */
public class AbstractDaemonTest extends Assert {
    private static final Logger LOG =  LoggerFactory.getLogger(AbstractDaemonTest.class);
    private CuratorTestUtils testUtils;
    private CouchbaseBucketFactorySimulator couchbaseBucketFactory;
    @Before
    public void setup() throws Exception{
        testUtils = new CuratorTestUtils();
        testUtils.prepare(1);
        couchbaseBucketFactory = new CouchbaseBucketFactorySimulator();
    }

    @Test
    public void testDaemon() throws Exception{
        final AtomicInteger nbErrors=new AtomicInteger(0);
        String connectionString = testUtils.getCluster().getConnectString();

        ConfigManagerFactory.addConfigurationEntry(CommonConfigProperties.ZOOKEEPER_CLUSTER_ADDREES.getName(), connectionString);
        ConfigManagerFactory.addConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("test", "testdoc").getName(), "testBucketName");
        final AbstractDaemon daemon=AbstractDaemon.builder()
                .withName("testing Daemon")
                .withWithCouchbase(true)
                .withUserFactory(new StandardMockUserFactory())
                .withWithCouchbaseBucketFactory(couchbaseBucketFactory)
                .build();

        daemon.addWebServer(RestWebServer.builder().withName("tests")
                .withApplicationContextConfig("applicationContext.xml")
                .withWithCouchbase(true));
        daemon.addWebServer(ProxyWebServer.builder().withPort(8080).withAddress("127.0.0.1").withName("proxy").withDiscoverPath("tests/services"));
        Thread stopping_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                DaemonDiscovery daemonDiscovery = new DaemonDiscovery(daemon.getCuratorClient());
                try {
                    daemonDiscovery.start();
                    List<DaemonInfo> daemonInfoList = daemonDiscovery.getList();
                    assertEquals(1, daemonInfoList.size());
                    assertEquals(daemon.getUuid(),daemonInfoList.get(0).getUuid());
                    assertEquals(daemon.getAdditionalWebServers().size(),daemonInfoList.get(0).getWebServerList().size());
                }
                catch(Throwable e){
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Error during daemon registered info read", e);
                }
                try{
                    TestDoc newDoc = new TestDoc();
                    newDoc.name="testInit";

                    ServiceClientFactory daoReadClientFactory = daemon.getAdminWebServer().getServiceDiscoveryManager().getClientFactory(DaoHelperConfigProperties.DAO_READ_SERVICES_DOMAIN.get());
                    ServiceClientFactory daoWriteClientFactory = daemon.getAdminWebServer().getServiceDiscoveryManager().getClientFactory(DaoHelperConfigProperties.DAO_WRITE_SERVICES_DOMAIN.get());
                    Response response = daoWriteClientFactory.getClient("dao#test#testdoc$write", "1.0.0")
                            .register(JsonProviderFactory.getProvider(CouchbaseDocumentObjectMapperConfigurator.BASE_COUCHBASE_PUBLIC))
                            .request()
                                    .post(Entity.json(newDoc));
                    TestDoc createdTestDoc = response.readEntity(new GenericType<>(TestDoc.class));
                    DaoHelperServiceUtils.finalizeFromResponse(response,createdTestDoc);
                    assertEquals(newDoc.name, createdTestDoc.name);

                    String[] keyParts = createdTestDoc.getMeta().getKey().split("/");
                    Response readDocResponse = daoReadClientFactory.getClient("dao#test#testdoc$read", "1.0.0")
                            .register(JsonProviderFactory.getProvider(CouchbaseDocumentObjectMapperConfigurator.BASE_COUCHBASE_PUBLIC))
                                    .path(keyParts[keyParts.length-1])
                                    .request()
                                    .get();

                    TestDoc readDoc = readDocResponse.readEntity(new GenericType<>(TestDoc.class));
                    DaoHelperServiceUtils.finalizeFromResponse(readDocResponse,readDoc);
                    assertEquals(createdTestDoc.name, readDoc.name);
                    assertEquals(createdTestDoc.getMeta().getKey(),readDoc.getMeta().getKey());

                }
                catch(Throwable e){
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Error during status read", e);
                }
                try{
                    DaoDiscovery discovery = new DaoDiscovery(daemon.getCuratorClient());
                    discovery.start();
                    List<DaoInstanceInfo> daos =discovery.getList();
                    assertEquals(1, daos.size());
                    DaoInstanceInfo instanceInfo = daos.get(0);
                    assertEquals(TestDoc.class.getName(), instanceInfo.getMainEntity().getClassName());
                    assertEquals(1,daos.get(0).getChildEntities().size());
                    assertEquals(TestDocEnhanced.class.getName(),daos.get(0).getChildEntities().get(0).getClassName());
                }
                catch (Throwable e){
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Error during status read", e);
                }
                try{
                    Integer response =ClientBuilder.newClient()
                            .register(JsonProviderFactory.getProvider("service"))
                            .target("http://127.0.0.1:8080")
                            .path("/proxy-apis/tests#tests#tests/1.0")
                            .request()
                            .get(Integer.class);
                    assertEquals(12L, response.longValue());

                    Integer responseQuery =ClientBuilder.newClient()
                            .register(JsonProviderFactory.getProvider("service"))
                            .target("http://127.0.0.1:8080")
                            .path("/proxy-apis/tests#tests#tests/1.0/23")
                            .queryParam("qnb","3")
                            .request()
                            .get(Integer.class);
                    assertEquals(12L+23+3,responseQuery.longValue());
                }
                catch(Throwable e){
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Error during status read", e);
                }
                try {

                    Integer response = ((RestWebServer)daemon.getAdditionalWebServers().get(0)).getServiceDiscoveryManager().getClientFactory("tests/services")
                            .getClient("tests#tests#tests", "1.0")
                            .register(JsonProviderFactory.getProvider(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR))
                            //.path("/status")
                            .request(MediaType.APPLICATION_JSON)
                            .get(Integer.class);
                    assertEquals(12L,response.longValue());
                }
                catch(Throwable e){
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Error during status read", e);
                }


                try {
                    WebServerInfo response = daemon.getAdminWebServer().getServiceDiscoveryManager().getClientFactory("admin/services")
                            .getClient("daemon#admin#status", "1.0")
                            .register(JsonProviderFactory.getProvider(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR))
                            .path("/webservers/tests")
                            .request(MediaType.APPLICATION_JSON)
                            .get(WebServerInfo.class);
                    assertEquals(AbstractWebServer.Status.STARTED,response.getStatus());
                }
                catch(Throwable e){
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Error during status read", e);
                }


                try {
                    StatusResponse response = daemon.getAdminWebServer().getServiceDiscoveryManager().getClientFactory(DaemonConfigProperties.DAEMON_ADMIN_SERVICES_DOMAIN.get())
                            .getClient("daemon#admin#status", "1.0")
                            .register(JsonProviderFactory.getProvider(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR))
                            .path("/status")
                            .request(MediaType.APPLICATION_JSON)
                            .get(StatusResponse.class);
                    assertEquals(IDaemonLifeCycle.Status.STARTED,response.getStatus());
                }
                catch(Throwable e){
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Error during status read", e);
                }
                try {
                    LOG.info("Request halting the web server");

                    StatusUpdateRequest request = new StatusUpdateRequest();
                    request.setAction(StatusUpdateRequest.Action.HALT);
                    StatusResponse response= daemon.getAdminWebServer().getServiceDiscoveryManager().getClientFactory(DaemonConfigProperties.DAEMON_ADMIN_SERVICES_DOMAIN.get())
                            .getClient("daemon#admin#status", "1.0")
                            .register(JsonProviderFactory.getProvider(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR))
                            .path("/status")
                            .request(MediaType.APPLICATION_JSON)
                            .put(Entity.json(request), StatusResponse.class);

                    assertEquals(IDaemonLifeCycle.Status.HALTED, response.getStatus());
                }
                catch(Throwable e){
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Cannot call halt", e);
                }

                try {
                    LOG.info("Request stopping the web server");
                    StatusUpdateRequest request = new StatusUpdateRequest();
                    request.setAction(StatusUpdateRequest.Action.STOP);
                    StatusResponse response= daemon.getAdminWebServer().getServiceDiscoveryManager().getClientFactory(DaemonConfigProperties.DAEMON_ADMIN_SERVICES_DOMAIN.get())
                            .getClient("daemon#admin#status", "1.0")
                            .register(JsonProviderFactory.getProvider(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR))
                            .path("/status")
                            .request(MediaType.APPLICATION_JSON)
                            .put(Entity.json(request),StatusResponse.class);

                    assertEquals(IDaemonLifeCycle.Status.STOPPING, response.getStatus());
                    return;
                }
                catch(Throwable e){
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Cannot call stop", e);
                }

                try {
                    nbErrors.incrementAndGet();
                    daemon.getDaemonLifeCycle().stop();
                }
                catch(Throwable e){
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Cannot stop", e);
                }

                nbErrors.incrementAndGet();
                fail("Shoudn't have to call stop");
            }
        });
        daemon.getDaemonLifeCycle().addLifeCycleListener(new IDaemonLifeCycle.DefaultListener(1000000) {
            @Override
            public void lifeCycleStarting(IDaemonLifeCycle lifeCycle) {
                try {
                    LOG.info("The web server is starting");
                    assertEquals(IDaemonLifeCycle.Status.STARTING, lifeCycle.getDaemon().getStatus());
                    for (AbstractWebServer server : lifeCycle.getDaemon().getAdditionalWebServers()) {
                        assertEquals(true, server.getLifeCycle().isStopped());
                    }
                    assertEquals(true, daemon.getAdminWebServer().getLifeCycle().isStarted());
                } catch (Throwable e) {
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Error during starting", e);
                }
            }

            @Override
            public void lifeCycleStarted(final IDaemonLifeCycle lifeCycle) {
                try {
                    LOG.info("The web server is started");
                    assertEquals(IDaemonLifeCycle.Status.STARTING, lifeCycle.getDaemon().getStatus());
                    for (AbstractWebServer server : lifeCycle.getDaemon().getAdditionalWebServers()) {
                        assertEquals(true, server.getLifeCycle().isStarted());
                    }
                    assertEquals(true, daemon.getAdminWebServer().getLifeCycle().isStarted());
                    stopping_thread.start();
                } catch (Throwable e) {
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Error during starting", e);
                }
                //TODO perform calls to rest API, ...
            }

            @Override
            public void lifeCycleFailure(IDaemonLifeCycle lifeCycle, Throwable exception) {
                nbErrors.getAndIncrement();
                LOG.error("!!!!! ERROR !!!!!Error with failure", exception);
            }
        });


        daemon.getDaemonLifeCycle().addLifeCycleListener(new IDaemonLifeCycle.DefaultListener(1) {
            @Override
            public void lifeCycleHalt(IDaemonLifeCycle lifeCycle) {
                try {
                    for (AbstractWebServer server : lifeCycle.getDaemon().getAdditionalWebServers()) {
                        assertEquals(true, server.getLifeCycle().isStopped());
                    }
                    assertEquals(true, daemon.getAdminWebServer().getLifeCycle().isStarted());
                    assertEquals(IDaemonLifeCycle.Status.STARTED, lifeCycle.getDaemon().getStatus());
                } catch (Throwable e) {
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Error during Halting", e);
                }
            }

            @Override
            public void lifeCycleStopping(IDaemonLifeCycle lifeCycle) {
                try {
                    for (AbstractWebServer server : lifeCycle.getDaemon().getAdditionalWebServers()) {
                        assertEquals(true, server.getLifeCycle().isStopped());
                    }
                    assertEquals(true, daemon.getAdminWebServer().getLifeCycle().isStarted());
                    assertEquals(IDaemonLifeCycle.Status.STOPPING, lifeCycle.getDaemon().getStatus());
                } catch (Throwable e) {
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Error during stopping", e);
                }
            }

            @Override
            public void lifeCycleStopped(IDaemonLifeCycle lifeCycle) {
                try {
                    for (AbstractWebServer server : lifeCycle.getDaemon().getAdditionalWebServers()) {
                        assertEquals(true, server.getLifeCycle().isStopped());
                    }
                    assertEquals(true, daemon.getAdminWebServer().getLifeCycle().isStopped());
                    assertEquals(IDaemonLifeCycle.Status.HALTED, lifeCycle.getDaemon().getStatus());
                } catch (Throwable e) {
                    nbErrors.incrementAndGet();
                    LOG.error("!!!!! ERROR !!!!!Error during stopped", e);
                }
            }
        });

        daemon.startAndJoin();
        LOG.warn("Going to stop");
        stopping_thread.join();
        assertEquals(0L, nbErrors.get());
        {
            DaemonDiscovery daemonDiscovery = new DaemonDiscovery(daemon.getCuratorClient());
            daemonDiscovery.start();
            assertEquals(0L, daemonDiscovery.getList().size());
        }

    }

    @After
    public void close() throws Exception {
        if (testUtils != null) testUtils.stop();
    }
}