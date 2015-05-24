package com.dreameddeath.core.curator;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.annotation.ConfigPropertyDoc;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import com.dreameddeath.core.config.impl.IntConfigProperty;
import com.dreameddeath.core.config.impl.StringConfigProperty;

/**
 * Created by CEAJ8230 on 21/05/2015.
 */
@ConfigPropertyPackage(name="curator",domain = "core",descr = "All common properties for curator/zookeeper core classes")
public class CuratorConfigProperties {
    @ConfigPropertyDoc(
            name="curator.zookeeper.cluster.addresses",
            descr = "defines the list zookeeper servers in the cluster (defined by an address and a port)",
            examples = {"192.168.1.1:2313,zk.test.com:9123"}
    )
    public static final StringConfigProperty ZOOKEEPER_CLUSTER_ADDREES = ConfigPropertyFactory.getStringProperty("curator.zookeeper.cluster.addresses", (String)null);

    @ConfigPropertyDoc(
            name="curator.zookeeper.retry.sleepTime",
            descr = "defines the time between two zookeeper (re)connection attempts in milliseconds",
            defaultValue = "1000",
            examples = {"1000"}
    )
    public static final IntConfigProperty ZOOKEEPER_CLUSTER_SLEEP_TIME = ConfigPropertyFactory.getIntProperty("curator.zookeeper.retry.sleepTime", 1000);

    @ConfigPropertyDoc(
            name="curator.zookeeper.retry.maxRetries",
            descr = "defines the maximum number of reconnection attempts",
            defaultValue = "3",
            examples = {"3","5"}
    )
    public static final IntConfigProperty ZOOKEEPER_CLUSTER_MAX_RECONNECTION_ATTEMPTS = ConfigPropertyFactory.getIntProperty("curator.zookeeper.retry.maxRetries", 3);


    @ConfigPropertyDoc(
            name="curator.session.timeout",
            descr = "defines the timeout of the zookeeper connection in ms",
            defaultValue = "@Ref{curator-default-session-timeout,system}",
            examples = {"30000"}
    )
    public static final IntConfigProperty CURATOR_SESSION_TIMEOUT =
            ConfigPropertyFactory.getIntProperty("curator.session.timeout", Integer.getInteger("curator-default-session-timeout", '\uea60').intValue());

    @ConfigPropertyDoc(
            name="curator.connection.timeout",
            descr = "defines the timeout of the zookeeper connection in ms",
            defaultValue = "@Ref{curator-default-connection-timeout,system}",
            examples = {"30000"}
    )
    public static final IntConfigProperty CURATOR_CONNECTION_TIMEOUT =
            ConfigPropertyFactory.getIntProperty("curator.connection.timeout", Integer.getInteger("curator-default-connection-timeout", 15000).intValue());

}
