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

package com.dreameddeath.infrastructure.common;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.annotation.ConfigPropertyDoc;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import com.dreameddeath.core.config.impl.IntConfigProperty;
import com.dreameddeath.core.config.impl.StringConfigProperty;
import com.dreameddeath.core.curator.CuratorConfigProperties;

/**
 * Created by Christophe Jeunesse on 12/08/2015.
 */
@ConfigPropertyPackage(name="common",domain = "infrastructure",descr = "All common properties for daemon classes")
public class CommonConfigProperties {
    @ConfigPropertyDoc(
            name="common.zookeeper.cluster.addresses",
            descr = "defines the list zookeeper servers in the cluster (defined by an address and a port)",
            defaultValue = "@Ref{CuratorConfigProperties.ZOOKEEPER_CLUSTER_ADDRESS,config}",
            examples = {"192.168.1.1:2313,zk.test.com:9123"}
    )
    public static final StringConfigProperty ZOOKEEPER_CLUSTER_ADDREES = ConfigPropertyFactory.getStringProperty("daemon.zookeeper.cluster.addresses", CuratorConfigProperties.ZOOKEEPER_CLUSTER_ADDREES);


    @ConfigPropertyDoc(
            name="common.zookeeper.retry.sleepTime",
            descr = "defines the time between two zookeeper (re)connection attempts in milliseconds",
            defaultValue = "@Ref{CuratorConfigProperties.ZOOKEEPER_CLUSTER_SLEEP_TIME,config}",
            examples = {"1000"}
    )
    public static final IntConfigProperty ZOOKEEPER_CLUSTER_SLEEP_TIME = ConfigPropertyFactory.getIntProperty("common.zookeeper.retry.sleepTime", CuratorConfigProperties.ZOOKEEPER_CLUSTER_SLEEP_TIME);

    @ConfigPropertyDoc(
            name="common.zookeeper.retry.maxRetries",
            descr = "defines the maximum number of reconnection attempts",
            defaultValue = "@Ref{CuratorConfigProperties.ZOOKEEPER_CLUSTER_MAX_RECONNECTION_ATTEMPTS,config}",
            examples = {"3","5"}
    )
    public static final IntConfigProperty ZOOKEEPER_CLUSTER_MAX_RECONNECTION_ATTEMPTS = ConfigPropertyFactory.getIntProperty("common.zookeeper.retry.maxRetries", CuratorConfigProperties.ZOOKEEPER_CLUSTER_MAX_RECONNECTION_ATTEMPTS);


    @ConfigPropertyDoc(
            name="common.zookeeper.x-discovery.services.basepath",
            descr = "Defines the base path in zookeeper for services self disovery",
            defaultValue = "x-discovery_services",
            examples = {"services"}
    )
    public static final StringConfigProperty ZOOKEEPER_XDISCOVERY_SERVICES_BASE_PATH = ConfigPropertyFactory.getStringProperty("common.zookeeper.x-discovery.services.basepath", "x-discovery_services");
}

