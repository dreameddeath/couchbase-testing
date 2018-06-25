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

package com.dreameddeath.testing.curator;

import com.dreameddeath.core.curator.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 21/03/2015.
 */
public class CuratorTestUtils {
    private static final Logger LOG= LoggerFactory.getLogger(CuratorTestUtils.class);
    private static int TIMEOUT_DURATION =5;
    private ExecutorService executor;
    private volatile TestingCluster testingCluster=null;
    private Future<TestingCluster> pendingCluster=null;
    private volatile CuratorFramework client=null;

    public CuratorTestUtils prepare(final int nbServers) throws Exception{
        System.setProperty("zookeeper.jmx.log4j.disable","true");
        executor = Executors.newSingleThreadExecutor();
        pendingCluster = executor.submit(() -> {
            try {
                TestingCluster newCluster= new TestingCluster(nbServers);
                newCluster.start();
                LOG.info("Cluster <{}> started",newCluster.getConnectString());
                testingCluster=newCluster;
                return newCluster;
            }
            catch(Exception e){
                LOG.error("Cluster startup failure",e);
                return null;
            }
        });
        Thread.sleep(10);
        return this;
    }

    public TestingCluster getCluster() throws Exception{
        if(testingCluster==null){
            TestingCluster buildCluster=pendingCluster.get(1,TimeUnit.MINUTES);
            executor.shutdownNow();
            return buildCluster;
        }
        if(!executor.isShutdown()) executor.shutdownNow();
        return testingCluster;
    }

    public CuratorFramework getClient(String nameSpacePrefix) throws Exception{
        if(client==null) {
            synchronized (this){
                if(client==null){
                    String connectionString = getCluster().getConnectString();
                    CuratorFramework client = CuratorFrameworkFactory.newClient(nameSpacePrefix, connectionString, new ExponentialBackoffRetry(1000, 3));
                    client.start();
                    client.blockUntilConnected(TIMEOUT_DURATION, TimeUnit.SECONDS);
                    this.client=client;
                }
            }
        }
        return client;
    }

    public CuratorTestUtils stop() throws Exception{
        if(client!=null && client.getState().equals(CuratorFrameworkState.STARTED)){
            LOG.info("Closing client of test utils {}",client.getZookeeperClient().getCurrentConnectionString());
            client.close();
        }
        while(!executor.isShutdown()){
            executor.shutdownNow();
            executor.awaitTermination(1,TimeUnit.SECONDS);
        }
        if(pendingCluster!=null && !pendingCluster.isDone()){
            pendingCluster.cancel(true);
        }
        if(testingCluster!=null){
            testingCluster.stop();
            testingCluster.close();
        }
        return this;
    }
}
