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

import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.lifecycle.IDaemonLifeCycle;
import com.dreameddeath.infrastructure.daemon.utils.ServerConnectorUtils;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 16/09/2015.
 */
public class DaemonInfo {
    @JsonProperty("uuid")
    private UUID uuid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("className")
    private String className;
    @JsonProperty("status")
    private IDaemonLifeCycle.Status status;
    @JsonProperty("address")
    private String address;
    @JsonProperty("ip")
    private String ip;
    @JsonProperty("port")
    private String port;
    @JsonProperty("adminRestUrl")
    private String adminRestUrl =null;
    @JsonProperty("webServerList")
    private List<String> webServerList =new ArrayList<>();


    public DaemonInfo(AbstractDaemon daemon){
        uuid = daemon.getUuid();
        name = daemon.getName();
        className = daemon.getClass().getName();
        status = daemon.getStatus();
        address = ServerConnectorUtils.getConnectorHost(daemon.getAdminWebServer().getServerConnector());
        port = ServerConnectorUtils.getConnectorPortString(daemon.getAdminWebServer().getServerConnector());
        for(AbstractWebServer server : daemon.getAdditionalWebServers()){
            webServerList.add(server.getName());
        }
    }

    public DaemonInfo(){}

    public List<String> getWebServerList() {
        return webServerList;
    }

    public void setWebServerList(List<String> webServerList) {
        this.webServerList = webServerList;
    }

    public String getAdminRestUrl() {
        return adminRestUrl;
    }

    public void setAdminRestUrl(String adminRestUrl) {
        this.adminRestUrl = adminRestUrl;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public IDaemonLifeCycle.Status getStatus() {
        return status;
    }

    public void setStatus(IDaemonLifeCycle.Status status) {
        this.status = status;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
