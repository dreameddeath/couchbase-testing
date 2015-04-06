/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.testing.curator;

import com.dreameddeath.core.curator.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;

import java.util.concurrent.*;

/**
 * Created by CEAJ8230 on 21/03/2015.
 */
public class CuratorTestUtils {
    private static int TIMEOUT_DURATION =5;
    private TestingCluster _testingCluster=null;
    private Future<TestingCluster> _pendingCluster=null;

    public void prepare(final int nbServers) throws Exception{
        System.setProperty("zookeeper.jmx.log4j.disable","true");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        //TODO manage real cluster connection (depending on env Variables or configuration parameters)
        Future<TestingCluster> future = executor.submit(new Callable<TestingCluster>() {
            @Override
            public TestingCluster call() {
                try {
                    TestingCluster cluster= new TestingCluster(nbServers);
                    cluster.start();
                    return cluster;
                }
                catch(Exception e){
                    return null;
                }
            }
        });
        _testingCluster = future.get(1, TimeUnit.MINUTES);
        executor.shutdownNow();
    }

    public TestingCluster getCluster() throws Exception{
        if(_testingCluster==null){
            _testingCluster = _pendingCluster.get(1,TimeUnit.MINUTES);
        }
        return _testingCluster;
    }

    public CuratorFramework getClient(String nameSpacePrefix) throws Exception{
        String connectionString = getCluster().getConnectString();
        CuratorFramework client = CuratorFrameworkFactory.newClient(nameSpacePrefix, connectionString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected(TIMEOUT_DURATION, TimeUnit.SECONDS);
        return client;
    }


}