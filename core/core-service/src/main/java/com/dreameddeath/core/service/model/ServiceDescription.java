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

package com.dreameddeath.core.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.models.Swagger;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Christophe Jeunesse on 17/01/2015.
 */
public class ServiceDescription {
    @JsonProperty("domain")
    private String domain;
    @JsonProperty("version")
    private String version;
    @JsonProperty("tags")
    private Set<String> tags=new TreeSet<>();
    @JsonProperty("state")
    private String state;
    @JsonProperty("swagger")
    private Swagger swagger;

    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
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

    public Swagger getSwagger() {
        return swagger;
    }
    public void setSwagger(Swagger swagger) {
        this.swagger = swagger;
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
}
