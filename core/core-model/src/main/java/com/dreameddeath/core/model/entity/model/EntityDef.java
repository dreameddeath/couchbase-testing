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

package com.dreameddeath.core.model.entity.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 13/10/2015.
 */
public class EntityDef {
    @JsonProperty("modelId")
    private EntityModelId modelId;
    private String className;
    private List<String> parentClasses=new ArrayList<>();

    @JsonGetter("modelId")
    public EntityModelId getModelId() {
        return modelId;
    }

    @JsonSetter("modelId")
    public void setModelId(EntityModelId modelId) {
        this.modelId = modelId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getParentClasses() {
        return parentClasses;
    }

    public void setParentClasses(List<String> parentClasses) {
        this.parentClasses = parentClasses;
    }
}
