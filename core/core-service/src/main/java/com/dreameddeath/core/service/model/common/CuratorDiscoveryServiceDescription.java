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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Christophe Jeunesse on 17/01/2015.
 */
//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,include = JsonTypeInfo.As.PROPERTY,property = "@c")
public abstract class CuratorDiscoveryServiceDescription<TSPEC> {
    @JsonProperty("domain")
    private String domain;
    @JsonProperty("name")
    private String name;
    @JsonProperty("version")
    private String version;
    @JsonProperty("tags")
    private Set<String> tags=new TreeSet<>();
    @JsonProperty("state")
    private String state;
    @JsonProperty("protocols")
    private Set<IEndPointDescription.Protocol> protocols=new TreeSet<>();
    @JsonProperty("jsonProvider")
    private String jsonProvider;
    @JsonProperty("spec")
    private TSPEC spec;

    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }


    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void setTags(Collection<String> tags) {
        this.tags.clear();tags.addAll(tags);
    }

    public void addTag(String tag){
        this.tags.add(tag);
    }

    public Set<IEndPointDescription.Protocol> getProtocols() {
        return protocols;
    }

    public void setProtocols(Set<IEndPointDescription.Protocol> protocols) {
        this.protocols.clear();
        this.protocols.addAll(protocols);
    }

    public void addProtocol(IEndPointDescription.Protocol protocol){
        this.protocols.add(protocol);
    }

    public String getJsonProvider() {
        return jsonProvider;
    }

    public void setJsonProvider(String jsonProvider) {
        this.jsonProvider = jsonProvider;
    }

    public TSPEC getSpec() {
        return spec;
    }

    public void setSpec(TSPEC spec) {
        this.spec = spec;
    }

    @JsonProperty
    public abstract String getServiceType();
    @JsonProperty
    public abstract void setServiceType(String serviceType);
}
