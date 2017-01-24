/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.party.process.service;

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.date.DateTimeServiceFactory;
import com.dreameddeath.core.date.MockDateTimeServiceImpl;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.dreameddeath.couchbase.testing.daemon.DaemonWrapperForTesting;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseDaemonPlugin;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseWebServerPlugin;
import com.dreameddeath.infrastructure.plugin.notification.NotificationWebServerPlugin;
import com.dreameddeath.infrastructure.plugin.process.ProcessesWebServerPlugin;
import com.dreameddeath.party.DomainConstants;
import com.dreameddeath.party.model.v1.Party;
import com.dreameddeath.party.model.v1.Person;
import com.dreameddeath.party.process.model.v1.CreateUpdatePartyJob;
import com.dreameddeath.party.process.model.v1.party.CreateUpdatePartyRequest;
import com.dreameddeath.testing.couchbase.CouchbaseBucketFactorySimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class CreateUpdatePartyJobProcessingServiceTest {
    private CuratorTestUtils testUtils;
    private DaemonWrapperForTesting daemonWrapper;
    private static final DateTime REFERENCE_DATE = DateTime.parse("2016-01-01T00:00:00");

    private final AtomicReference<DateTime> dateTimeRef=new AtomicReference<>(REFERENCE_DATE);

    @Before
    public void setup() throws Exception{
        testUtils = new CuratorTestUtils();
        testUtils.prepare(1);
        CouchbaseBucketFactorySimulator couchbaseBucketFactory = new CouchbaseBucketFactorySimulator();

        String connectionString = testUtils.getCluster().getConnectString();

        ConfigManagerFactory.addPersistentConfigurationEntry(CommonConfigProperties.ZOOKEEPER_CLUSTER_ADDREES.getName(), connectionString);
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_DOMAIN_BUCKET_NAME.getProperty("party").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("core", "abstractjob").getName(), "testCoreBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("core", "abstracttask").getName(), "testCoreBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("core", "notification").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("core", "event").getName(), "testBucketName");

        AbstractDaemon daemon = AbstractDaemon.builder()
                .withName("testing Daemon")
                .withUserFactory(new StandardMockUserFactory())
                .withPlugin(CouchbaseDaemonPlugin.builder().withBucketFactory(couchbaseBucketFactory))
                .build();

        daemon.addWebServer(RestWebServer.builder().withName("tests")
                //.withServiceDiscoveryManager(true)
                .withPlugin(CouchbaseWebServerPlugin.builder())
                .withPlugin(NotificationWebServerPlugin.builder())
                .withPlugin(ProcessesWebServerPlugin.builder())
                .withDateTimeServiceFactory(new DateTimeServiceFactory(new MockDateTimeServiceImpl(MockDateTimeServiceImpl.Calculator.fixedCalculator(dateTimeRef))))
                .withApplicationContextConfig("META-INF/spring/webserver.party.processes.applicationContext.xml"));

        daemonWrapper = new DaemonWrapperForTesting(daemon);
        daemonWrapper.start();
    }

    @Test
    public void JobTest() throws Exception{
        ProcessesWebServerPlugin plugin=daemonWrapper.getDaemon().getAdditionalWebServers().get(0).getPlugin(ProcessesWebServerPlugin.class);
        CouchbaseWebServerPlugin cbPlugin=daemonWrapper.getDaemon().getAdditionalWebServers().get(0).getPlugin(CouchbaseWebServerPlugin.class);

        IJobExecutorClient<CreateUpdatePartyJob> executorClient = plugin.getExecutorClientFactory().buildJobClient(CreateUpdatePartyJob.class);

        CreateUpdatePartyJob createUpdatePartyJob = new CreateUpdatePartyJob();
        CreateUpdatePartyRequest request = new CreateUpdatePartyRequest();
        createUpdatePartyJob.setRequest(request);
        request.type = CreateUpdatePartyRequest.Type.person;
        request.person = new CreateUpdatePartyRequest.Person();
        request.person.firstName = "christophe";
        request.person.lastName = "jeunesse";

        JobContext<CreateUpdatePartyJob> createJobJobContext =executorClient.executeJob(createUpdatePartyJob, AnonymousUser.INSTANCE).blockingGet();

        ICouchbaseSession controlSession = cbPlugin.getSessionFactory().newReadOnlySession(DomainConstants.PARTY_DOMAIN,null);
        CreateUpdatePartyJob inDbJob = controlSession.toBlocking().blockingGet(createJobJobContext.getInternalJob().getBaseMeta().getKey(),CreateUpdatePartyJob.class);
        assertEquals(inDbJob.getStateInfo().getState(), ProcessState.State.DONE);
        CreateUpdatePartyJob.CreatePartyTask inDbTask = controlSession.toBlocking().blockingGet(createUpdatePartyJob.getBaseMeta().getKey()+"/task/1",CreateUpdatePartyJob.CreatePartyTask.class);
        Party inDbParty = controlSession.toBlocking().blockingGet(inDbTask.getDocKey(),Party.class);
        assertEquals(inDbParty.getClass(),Person.class);
        if(inDbParty instanceof Person){
            Person inDbPerson = (Person)inDbParty;
            assertEquals(inDbPerson.getFirstName(), createUpdatePartyJob.getRequest().person.firstName);
            assertEquals(inDbPerson.getLastName(), createUpdatePartyJob.getRequest().person.lastName);
        }
    }

    @After
    public void close() throws Exception{
        if(daemonWrapper!=null){
            daemonWrapper.stop();
        }
        if(testUtils!=null){
            testUtils.stop();
        }
    }
}