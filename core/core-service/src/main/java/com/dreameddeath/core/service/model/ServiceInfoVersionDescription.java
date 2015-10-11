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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 02/10/2015.
 */
public class ServiceInfoVersionDescription {
    @JsonProperty("fullName")
    private String fullName;
    @JsonProperty("swagger")
    private Swagger swagger;
    @JsonProperty("state")
    private String state;
    @JsonProperty("instances")
    private List<ServiceInfoVersionInstanceDescription> instances = new ArrayList<>();

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Swagger getSwagger() {
        return swagger;
    }

    public void setSwagger(Swagger swagger) {
        this.swagger = swagger;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<ServiceInfoVersionInstanceDescription> getInstances() {
        return Collections.unmodifiableList(instances);
    }

    public void setInstances(List<ServiceInfoVersionInstanceDescription> instances) {
        this.instances.clear();
        this.instances.addAll(instances);
    }

    public void addInstance(ServiceInfoVersionInstanceDescription instance) {
        instances.add(instance);
    }
}
