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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 02/10/2015.
 */
public class ServiceInfoDescription {
    @JsonProperty("name")
    private String name;
    @JsonProperty("versions")
    private Map<String,ServiceInfoVersionDescription> serviceVersionInfoDescriptionMap=new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, ServiceInfoVersionDescription> getServiceVersionInfoDescriptionMap() {
        return Collections.unmodifiableMap(serviceVersionInfoDescriptionMap);
    }

    public void setServiceVersionInfoDescriptionMap(Map<String, ServiceInfoVersionDescription> serviceVersionInfoDescriptionMap) {
        this.serviceVersionInfoDescriptionMap.clear();
        this.serviceVersionInfoDescriptionMap.putAll(serviceVersionInfoDescriptionMap);
    }

    public ServiceInfoVersionDescription addIfNeededServiceVersionInfoDescriptionMap(String version, ServiceInfoVersionDescription serviceVersionInfoDescription) {
        return serviceVersionInfoDescriptionMap.putIfAbsent(version, serviceVersionInfoDescription);
    }
}
