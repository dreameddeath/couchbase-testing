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

package com.dreameddeath.core.service.model.common;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 02/10/2015.
 */
public class ServicesListInstanceDescription {
    @JsonProperty("name")
    private String name;
    @JsonProperty("services")
    List<ServiceInstanceDescription> instanceDescriptions =new ArrayList<>();

    @JsonSetter("services")
    public void setServiceInstanceList(List<ServiceInstanceDescription> list){
        instanceDescriptions.clear();
        instanceDescriptions.addAll(list);
    }

    @JsonGetter("services")
    public List<ServiceInstanceDescription> getServiceInstanceList(){
        return Collections.unmodifiableList(instanceDescriptions);
    }

    @JsonGetter("name")
    public String getName() {
        return name;
    }

    @JsonSetter("name")
    public void setName(String name) {
        this.name = name;
    }


    public void addServiceInstance(ServiceInstanceDescription serviceDescr){
        instanceDescriptions.add(serviceDescr);
    }

}
