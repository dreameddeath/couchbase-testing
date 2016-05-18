package com.dreameddeath.party.process.service.rest;

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.date.DateTimeServiceFactory;
import com.dreameddeath.core.date.MockDateTimeServiceImpl;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.core.user.StandardMockUserFactory;
import com.dreameddeath.couchbase.testing.daemon.DaemonWrapperForTesting;
import com.dreameddeath.infrastructure.common.CommonConfigProperties;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseDaemonPlugin;
import com.dreameddeath.infrastructure.plugin.couchbase.CouchbaseWebServerPlugin;
import com.dreameddeath.infrastructure.plugin.process.ProcessesWebServerPlugin;
import com.dreameddeath.party.model.v1.Party;
import com.dreameddeath.party.model.v1.Person;
import com.dreameddeath.party.process.model.v1.published.CreateUpdatePartyJobRequest;
import com.dreameddeath.party.process.model.v1.published.CreateUpdatePartyJobResponse;
import com.dreameddeath.party.process.model.v1.published.PersonRequest;
import com.dreameddeath.party.process.model.v1.published.TypePublished;
import com.dreameddeath.party.process.model.v1.roles.published.BillingAccountCreateUpdateRoleRequestRequest;
import com.dreameddeath.party.process.model.v1.roles.published.CreateUpdatePartyRolesJobRequest;
import com.dreameddeath.party.process.model.v1.roles.published.RoleTypePublished;
import com.dreameddeath.testing.couchbase.CouchbaseBucketFactorySimulator;
import com.dreameddeath.testing.curator.CuratorTestUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Created by Christophe Jeunesse on 17/05/2016.
 */
public class RemoteJobTests {
    private CuratorTestUtils testUtils;
    private DaemonWrapperForTesting daemonWrapper;
    private static final DateTime REFERENCE_DATE = DateTime.parse("2016-01-01T00:00:00");
    private final AtomicReference<DateTime> dateTimeRef=new AtomicReference<>(REFERENCE_DATE);



    @Before
    public void init() throws Exception{
        testUtils = new CuratorTestUtils();
        testUtils.prepare(1);
        CouchbaseBucketFactorySimulator couchbaseBucketFactory = new CouchbaseBucketFactorySimulator();

        String connectionString = testUtils.getCluster().getConnectString();

        ConfigManagerFactory.addPersistentConfigurationEntry(CommonConfigProperties.ZOOKEEPER_CLUSTER_ADDREES.getName(), connectionString);
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_DOMAIN_BUCKET_NAME.getProperty("party").getName(), "testBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("core", "abstractjob").getName(), "testCoreBucketName");
        ConfigManagerFactory.addPersistentConfigurationEntry(CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME.getProperty("core", "abstracttask").getName(), "testCoreBucketName");
        AbstractDaemon daemon = AbstractDaemon.builder()
                .withName("testing Daemon")
                .withUserFactory(new StandardMockUserFactory())
                .withPlugin(CouchbaseDaemonPlugin.builder().withBucketFactory(couchbaseBucketFactory))
                .build();

        daemon.addWebServer(RestWebServer.builder().withName("tests")
                //.withServiceDiscoveryManager(true)
                .withPlugin(CouchbaseWebServerPlugin.builder())
                .withPlugin(ProcessesWebServerPlugin.builder())
                .withDateTimeServiceFactory(new DateTimeServiceFactory(new MockDateTimeServiceImpl(MockDateTimeServiceImpl.Calculator.fixedCalculator(dateTimeRef))))
                .withApplicationContextConfig("META-INF/spring/party.test.applicationContext.xml"));

        daemonWrapper = new DaemonWrapperForTesting(daemon);
        daemonWrapper.start();
    }


    @Test
    public void test()throws Exception{
        ServiceClientFactory factory=daemonWrapper.getServiceFactoryForDomain("test");
        CouchbaseWebServerPlugin cbPlugin=daemonWrapper.getDaemon().getAdditionalWebServers().get(0).getPlugin(CouchbaseWebServerPlugin.class);
        String partyId = null;
        {
            IServiceClient client = factory.getClient("createupdatepartyjob", "1.0.0");
            CreateUpdatePartyJobRequest request = new CreateUpdatePartyJobRequest();
            request.setType(TypePublished.person);
            request.setPerson(new PersonRequest());
            request.getPerson().setFirstName("christophe");
            request.getPerson().setLastName("jeunesse");
            Response response = client.getInstance().request().post(Entity.json(request));
            assertEquals(200, response.getStatus());
            CreateUpdatePartyJobResponse createResponse = response.readEntity(CreateUpdatePartyJobResponse.class);
            assertNotNull(createResponse.getUid());

            Party party = cbPlugin.getSessionFactory().newReadOnlySession(AnonymousUser.INSTANCE).getFromUID(createResponse.getUid(), Party.class);
            assertTrue(party instanceof Person);
            assertEquals(request.getPerson().getFirstName(), ((Person) party).getFirstName());
            assertEquals(request.getPerson().getLastName(), ((Person) party).getLastName());
            partyId=party.getUid();
        }

        {
            IServiceClient clientRoles = factory.getClient("createupdatepartyrolesjob", "1.0.0");
            CreateUpdatePartyRolesJobRequest rolesJobRequest = new CreateUpdatePartyRolesJobRequest();
            BillingAccountCreateUpdateRoleRequestRequest newRequest = new BillingAccountCreateUpdateRoleRequestRequest();
            rolesJobRequest.setRoleRequests(new ArrayList<>());
            rolesJobRequest.getRoleRequests().add(newRequest);
            newRequest.setBaId("testBaid1");
            newRequest.setPartyId(partyId);
            newRequest.setTypes(Arrays.asList(RoleTypePublished.HOLDER, RoleTypePublished.PAYER));

            Response response = clientRoles.getInstance().request().post(Entity.json(rolesJobRequest));
            assertEquals(200, response.getStatus());
            Party party = cbPlugin.getSessionFactory().newReadOnlySession(AnonymousUser.INSTANCE).getFromUID(partyId, Party.class);
            assertTrue(party instanceof Person);
            assertEquals(1,party.getPartyRoles().size());
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
