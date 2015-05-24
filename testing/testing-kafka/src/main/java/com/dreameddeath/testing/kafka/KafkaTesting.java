package com.dreameddeath.testing.kafka;

import com.dreameddeath.testing.curator.CuratorTestUtils;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import kafka.utils.TestUtils;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by CEAJ8230 on 18/05/2015.
 */
public class KafkaTesting {
    private static KafkaConfig getKafkaConfig(final String zkConnectString) {
        scala.collection.Iterator<Properties> propsI = TestUtils.createBrokerConfigs(1,true).iterator();
        assert propsI.hasNext();
        Properties props = propsI.next();
        props.put("zookeeper.connect", zkConnectString);
        return new KafkaConfig(props);
    }


    private Future<KafkaServerStartable> _futureServer;
    private KafkaServerStartable _server;


    public KafkaTesting(final CuratorTestUtils curatorTestUtils){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        _futureServer = executor.submit(new Callable<KafkaServerStartable>() {
            @Override
            public KafkaServerStartable call() throws Exception {
                KafkaConfig config = getKafkaConfig(curatorTestUtils.getCluster().getConnectString());
                KafkaServerStartable server = new KafkaServerStartable(config);
                server.startup();
                return server;
            }
        });
    }

    synchronized private KafkaServerStartable getServer() throws Exception{
        if(_server==null){
            _server = _futureServer.get();
        }
        return _server;
    }


    public int getKafkaPort() throws Exception{
        return getServer().serverConfig().port();
    }

    public void stop() throws Exception {
        getServer().shutdown();
    }
}
