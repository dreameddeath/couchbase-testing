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

package com.dreameddeath.testing.kafka;

import com.dreameddeath.testing.curator.CuratorTestUtils;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;

import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Christophe Jeunesse on 18/05/2015.
 */
public class KafkaTesting {
    private static KafkaConfig getKafkaConfig(final String zkConnectString) {
        //scala.collection.Iterator<Properties> propsI = TestUtils.createBrokerConfigs(1,zkConnectString,true).iterator();
        //assert propsI.hasNext();
        //Properties props = propsI.next();
        Properties props = new Properties();
        props.put("zookeeper.connect", zkConnectString);
        return new KafkaConfig(props);
    }


    private Future<KafkaServerStartable> futureServer;
    private KafkaServerStartable server;


    public KafkaTesting(final CuratorTestUtils curatorTestUtils){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        futureServer = executor.submit(new Callable<KafkaServerStartable>() {
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
        if(server==null){
            server = futureServer.get();
        }
        return server;
    }


    public int getKafkaPort() throws Exception{
        return getServer().serverConfig().port();
    }

    public void stop() throws Exception {
        getServer().shutdown();
    }
}
