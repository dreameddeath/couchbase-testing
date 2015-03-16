package com.dreameddeath.infrastructure.daemon;

import com.dreameddeath.core.config.PropertyFactory;
import com.dreameddeath.core.curator.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Created by CEAJ8230 on 05/02/2015.
 */
public class AbstractDaemon {
    private static CuratorFramework CURATOR_CLIENT;
    static{
        try {

            String addressProp = PropertyFactory.getStringProperty("zookeeper.cluster.addresses", null).getMandatoryValue("The zookeeper cluster address must be defined");
            int sleepTime = PropertyFactory.getIntProperty("zookeeper.retry.sleepTime", 1000).get();
            int maxRetries = PropertyFactory.getIntProperty("zookeeper.retry.maxRetries", 3).get();
            CURATOR_CLIENT = CuratorFrameworkFactory.newClient(addressProp, new ExponentialBackoffRetry(sleepTime, maxRetries));
            CURATOR_CLIENT.start();
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }





}
