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
