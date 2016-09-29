/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.service.model.common;

import com.dreameddeath.core.service.registrar.IEndPointDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableSet;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.Set;

/**
 * Created by Christophe Jeunesse on 04/03/2015.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS,include = JsonTypeInfo.As.PROPERTY,property = "@c")
public abstract class ServiceInstanceDescription<TSPEC>{
    @JsonProperty("name")
    private String name;
    @JsonProperty("address")
    private String address;
    @JsonProperty("version")
    private String version;
    @JsonProperty("port")
    private Integer port;
    @JsonProperty("sslPort")
    private Integer sslPort;
    @JsonProperty("protocols")
    private Set<IEndPointDescription.Protocol> protocols;
    @JsonProperty("uid")
    private String uid;
    @JsonProperty("state")
    private String state;
    @JsonProperty("spec")
    private TSPEC spec;

    public ServiceInstanceDescription(ServiceInstance<? extends CuratorDiscoveryServiceDescription<TSPEC>> instance){
        name =instance.getName();
        address = instance.getAddress();
        uid = instance.getId();
        version = instance.getPayload().getVersion();
        state = instance.getPayload().getState();
        port=instance.getPort();
        spec =instance.getPayload().getSpec();
        sslPort = instance.getSslPort();
        setProtocols(instance.getPayload().getProtocols());
    }

    public ServiceInstanceDescription(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public TSPEC getSpec() {
        return spec;
    }

    public void setSpec(TSPEC spec) {
        this.spec = spec;
    }

    public Integer getSslPort() {
        return sslPort;
    }

    public void setSslPort(Integer sslPort) {
        this.sslPort = sslPort;
    }

    public Set<IEndPointDescription.Protocol> getProtocols() {
        return protocols;
    }

    public void setProtocols(Set<IEndPointDescription.Protocol> protocols) {
        this.protocols = ImmutableSet.copyOf(protocols);
    }

    @JsonProperty
    public abstract String getServiceType();
    @JsonProperty
    public abstract void setServiceType(String type);

}

