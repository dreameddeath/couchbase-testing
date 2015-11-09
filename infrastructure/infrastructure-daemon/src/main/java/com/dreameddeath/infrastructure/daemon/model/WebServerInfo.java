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

import com.dreameddeath.core.dao.model.discovery.DaoInstanceInfo;
import com.dreameddeath.core.service.model.ServiceDescription;
import com.dreameddeath.core.service.registrar.ServiceRegistrar;
import com.dreameddeath.infrastructure.daemon.utils.ServerConnectorUtils;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import com.dreameddeath.infrastructure.daemon.webserver.RestWebServer;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 19/09/2015.
 */
public class WebServerInfo {
    @JsonProperty("uid")
    private UUID uid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("className")
    private String className;
    @JsonProperty("address")
    private String address;
    @JsonProperty("port")
    private Integer port;
    @JsonProperty("status")
    private AbstractWebServer.Status status;
    @JsonProperty("daos")
    private List<DaoInstanceInfo> daoInfo=new ArrayList<>();
    @JsonProperty("services")
    private List<ServiceInstance<ServiceDescription>> services=new ArrayList<>();

    public WebServerInfo(AbstractWebServer server){
        uid = server.getUuid();
        name = server.getName();
        className = server.getClass().getName();
        address = ServerConnectorUtils.getConnectorHost(server.getServerConnector());
        port = ServerConnectorUtils.getConnectorPort(server.getServerConnector());
        status = server.getStatus();
        if(server.getCouchbaseFactories()!=null){
            daoInfo=server.getCouchbaseFactories().getDocumentDaoFactory().getRegisteredDaoInstancesInfo();
        }
        if(server instanceof RestWebServer){
            for(ServiceRegistrar registrar:((RestWebServer)server).getServiceDiscoveryManager().getRegistrars()){
                services.addAll(registrar.getServicesInstanceDescription());
            }
        }
    }

    public WebServerInfo(){

    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
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

    public List<DaoInstanceInfo> getDaoInfo() {
        return daoInfo;
    }

    public void setDaoInfo(List<DaoInstanceInfo> daoInfo) {
        this.daoInfo = daoInfo;
    }

    public List<ServiceInstance<ServiceDescription>> getServices() {
        return services;
    }

    public void setServices(List<ServiceInstance<ServiceDescription>> services) {
        this.services = services;
    }
}
