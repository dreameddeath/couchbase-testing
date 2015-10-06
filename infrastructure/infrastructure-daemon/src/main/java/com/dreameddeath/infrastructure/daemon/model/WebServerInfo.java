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

package com.dreameddeath.infrastructure.daemon.model;

import com.dreameddeath.infrastructure.daemon.utils.ServerConnectorUtils;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;

/**
 * Created by Christophe Jeunesse on 19/09/2015.
 */
public class WebServerInfo {
    private String name;
    private String className;
    private String address;
    private Integer port;
    private AbstractWebServer.Status status;

    public WebServerInfo(AbstractWebServer server){
        name = server.getName();
        className = server.getClass().getName();
        address = ServerConnectorUtils.getConnectorHost(server.getServerConnector());
        port = ServerConnectorUtils.getConnectorPort(server.getServerConnector());
        status = server.getStatus();
    }

    public WebServerInfo(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public AbstractWebServer.Status getStatus() {
        return status;
    }

    public void setStatus(AbstractWebServer.Status status) {
        this.status = status;
    }
}
