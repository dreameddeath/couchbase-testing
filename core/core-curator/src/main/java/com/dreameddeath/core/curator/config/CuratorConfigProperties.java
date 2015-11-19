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

package com.dreameddeath.core.curator.config;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.ConfigPropertyWithTemplateName;
import com.dreameddeath.core.config.annotation.ConfigPropertyDoc;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import com.dreameddeath.core.config.impl.IntConfigProperty;
import com.dreameddeath.core.config.impl.StringConfigProperty;

/**
 * Created by Christophe Jeunesse on 21/05/2015.
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


    @ConfigPropertyDoc(
            name="curator.config.shared.root.path",
            descr = "defines the root path for all shared configuration entries",
            defaultValue = "/config/shared",
            examples = {"/config/shared"}
    )
    public static final StringConfigProperty CURATOR_SHARED_CONFIG_ROOT_PATH =
            ConfigPropertyFactory.getStringProperty("curator.config.shared.root.path","/config/shared");


    @ConfigPropertyDoc(
            name="curator.config.shared.{name}.descr",
            descr = "defines the description of the shared config entry"
    )
    public static final ConfigPropertyWithTemplateName<String,StringConfigProperty> CURATOR_SHARED_CONFIG_DESCR_FOR_NAME =
            ConfigPropertyFactory.getTemplateNameConfigProperty(StringConfigProperty.class,"curator.config.shared.{name}.descr",(String)null);

    @ConfigPropertyDoc(
            name="curator.config.shared.{name}.path",
            descr = "defines the relative path of the shared config entry",
            examples = {"/toto"}
    )
    public static final ConfigPropertyWithTemplateName<String,StringConfigProperty> CURATOR_SHARED_CONFIG_PATH_FOR_NAME =
            ConfigPropertyFactory.getTemplateNameConfigProperty(StringConfigProperty.class,"curator.config.shared.{name}.path",(String)null);


}
