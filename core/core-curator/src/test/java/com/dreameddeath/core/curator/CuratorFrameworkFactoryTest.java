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

package com.dreameddeath.core.curator;

import com.dreameddeath.core.curator.exception.DuplicateClusterClientException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.*;

public class CuratorFrameworkFactoryTest extends Assert{
    private static int TIMEOUT_DURATION =5;
    TestingCluster testingCluster=null;

    @Before
    public void prepare() throws Exception{
        System.setProperty("zookeeper.jmx.log4j.disable","true");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<TestingCluster> future = executor.submit(new Callable<TestingCluster>() {
            @Override
            public TestingCluster call() {
                try {
                    TestingCluster cluster= new TestingCluster(3);
                    cluster.start();
                    return cluster;
                }
                catch(Exception e){
                    return null;
                }
            }
        });
        testingCluster = future.get(1,TimeUnit.MINUTES);
        executor.shutdownNow();
    }

    @Test
    public void testStandardInit() throws Exception{
        String connectionString = testingCluster.getConnectString();
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectionString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected(TIMEOUT_DURATION, TimeUnit.SECONDS);
        client.close();
    }

    @Test
    public void testDuplicate() throws Exception{
        String connectionString = testingCluster.getConnectString();
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectionString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected(TIMEOUT_DURATION, TimeUnit.SECONDS);
        String[] servers = connectionString.split(CuratorFrameworkFactory.CONNECTION_STRING_SEPARATOR);
        try{
            CuratorFrameworkFactory.newClientInstance(servers[2] + "," + servers[1], new ExponentialBackoffRetry(1000, 3));
            fail("The duplicate exception hasn't been raised");
        }
        catch(DuplicateClusterClientException e){
            assertEquals(client,e.getExistingFramework());
        }
        client.close();
    }


    @Test
    public void testNameSpace() throws Exception{
        String connectionString = testingCluster.getConnectString();
        CuratorFramework client = CuratorFrameworkFactory.newClient("prefix",connectionString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected(TIMEOUT_DURATION, TimeUnit.SECONDS);

        final String refValue="a testing String";
        client.create().forPath("/subPath",refValue.getBytes("UTF-8"));

        CuratorFramework rawClient = org.apache.curator.framework.CuratorFrameworkFactory.builder().connectString(connectionString).retryPolicy(new ExponentialBackoffRetry(1000,3)).build();
        rawClient.start();
        rawClient.blockUntilConnected(TIMEOUT_DURATION,TimeUnit.SECONDS);
        assertEquals(refValue, new String(rawClient.getData().forPath("/prefix/subPath"),"UTF-8"));
        client.close();
        rawClient.close();
    }

    @After
    public void endTest() throws Exception{
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    testingCluster.close();
                    return true;
                }
                catch(Exception e){
                    return false;
                }
            }
        });
        future.get(1,TimeUnit.MINUTES);
        executor.shutdownNow();
    }



}