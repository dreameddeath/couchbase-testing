package com.dreameddeath.core.curator;

import com.dreameddeath.core.exception.curator.DuplicateClusterClientException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class CuratorFrameworkFactoryTest extends Assert{
    TestingCluster _testingCluster=null;

    @Before
    public void prepare() throws Exception{
        System.setProperty("zookeeper.jmx.log4j.disable","true");
        _testingCluster = new TestingCluster(3);
        _testingCluster.start();
    }

    @Test
    public void testStandardInit() throws Exception{
        String connectionString = _testingCluster.getConnectString();
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectionString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected(1, TimeUnit.SECONDS);
        client.close();
    }

    @Test
    public void testDuplicate() throws Exception{
        String connectionString = _testingCluster.getConnectString();
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectionString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected(1, TimeUnit.SECONDS);
        String[] servers = connectionString.split(CuratorFrameworkFactory.CONNECTION_STRING_SEPARATOR);
        try{
            CuratorFramework newClient = CuratorFrameworkFactory.newClientInstance(servers[2] + "," + servers[1], new ExponentialBackoffRetry(1000, 3));
            fail("The duplicate exception hasn't been raised");
        }
        catch(DuplicateClusterClientException e){
            assertEquals(client,e.getExistingFramework());
        }
        client.close();
    }


    @Test
    public void testNameSpace() throws Exception{
        String connectionString = _testingCluster.getConnectString();
        CuratorFramework client = CuratorFrameworkFactory.newClient("prefix",connectionString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected(1, TimeUnit.SECONDS);

        final String refValue="a testing String";
        String result = client.create().forPath("/subPath",refValue.getBytes("UTF-8"));

        CuratorFramework rawClient = org.apache.curator.framework.CuratorFrameworkFactory.builder().connectString(connectionString).retryPolicy(new ExponentialBackoffRetry(1000,3)).build();
        rawClient.start();
        rawClient.blockUntilConnected(1,TimeUnit.SECONDS);
        assertEquals(refValue, new String(rawClient.getData().forPath("/prefix/subPath"),"UTF-8"));
        client.close();
        rawClient.close();
    }

    @After
    public void endTest() throws Exception{
        _testingCluster.stop();
    }



}