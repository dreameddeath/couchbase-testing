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

package com.dreameddeath.infrastructure.daemon.config;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.annotation.ConfigPropertyDoc;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import com.dreameddeath.core.config.impl.IntConfigProperty;
import com.dreameddeath.core.config.impl.StringConfigProperty;

/**
 * Created by Christophe Jeunesse on 21/05/2015.
 */
@ConfigPropertyPackage(name="daemon",domain = "daemon",descr = "All common properties for daemon classes")
public class DaemonConfigProperties {

    @ConfigPropertyDoc(
            name="daemon.webserver.address",
            descr = "Defines the host of the webserver. If none given, it's the interface with is used @see{daemon.webserver.interface}",
            examples = {"test.toto.com"}
    )
    public static final StringConfigProperty DAEMON_WEBSERVER_ADDRESS = ConfigPropertyFactory.getStringProperty("daemon.webserver.address", (String) null);


    @ConfigPropertyDoc(
            name="daemon.webserver.interface",
            descr = "Defines the interface on which to listen to. If none given, it listens to anything",
            examples = {"inet1"}
    )
    public static final StringConfigProperty DAEMON_WEBSERVER_INTERFACE = ConfigPropertyFactory.getStringProperty("daemon.webserver.interface", (String) null);


    @ConfigPropertyDoc(
            name="daemon.webserver.port",
            descr = "defines the listening port. If not given, or the value is 0, a dynamic one is allocated",
            defaultValue = "0",
            examples = {"8080","10080"}
    )
    public static final IntConfigProperty DAEMON_WEBSERVER_PORT = ConfigPropertyFactory.getIntProperty("daemon.webserver.port", 0);


    @ConfigPropertyDoc(
            name="daemon.webserver.proxy-api.path-prefix",
            descr = "defines the api proxy default web server",
            defaultValue = "proxy-apis",
            examples = {"proxy-api","proxy"}
    )
    public static final StringConfigProperty DAEMON_WEBSERVER_PROXY_API_PATH_PREFIX = ConfigPropertyFactory.getStringProperty("daemon.webserver.proxy-api.path-prefix", "proxy-apis");


    @ConfigPropertyDoc(
            name="daemon.webserver.api.path-prefix",
            descr = "defines the api default web server",
            defaultValue = "apis",
            examples = {"apis","apis/tests"}
    )
    public static final StringConfigProperty DAEMON_WEBSERVER_API_PATH_PREFIX = ConfigPropertyFactory.getStringProperty("daemon.webserver.api.path-prefix", "apis");


    @ConfigPropertyDoc(
            name="daemon.zookeeper.register.path",
            descr = "defines the path where launched daemon are registered",
            defaultValue = "/daemons/instances",
            examples = {"/daemons/list","/daemons/actual_list"}
    )
    public static final StringConfigProperty DAEMON_REGISTER_PATH = ConfigPropertyFactory.getStringProperty("daemon.zookeeper.register.path", "/daemons/instances");

    @ConfigPropertyDoc(
            name="daemon.name",
            descr = "defines the daemon name",
            examples = {"supervisor","billing"}
    )
    public static final StringConfigProperty DAEMON_NAME = ConfigPropertyFactory.getStringProperty("daemon.name",(String)null);


    @ConfigPropertyDoc(
            name="daemon.admin.services.domain",
            descr = "defines the domain of admin services for Service Discovery Register",
            defaultValue = "admin",
            examples = {"admin"}
    )
    public static final StringConfigProperty DAEMON_ADMIN_SERVICES_DOMAIN = ConfigPropertyFactory.getStringProperty("daemon.admin.services.domain","admin");

}
