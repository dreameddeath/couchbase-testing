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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Christophe Jeunesse on 20/10/2015.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility= JsonAutoDetect.Visibility.NONE,fieldVisibility= JsonAutoDetect.Visibility.NONE,isGetterVisibility = JsonAutoDetect.Visibility.NONE,setterVisibility = JsonAutoDetect.Visibility.NONE,creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VersionUpgradeDef {
    @JsonProperty("class")
    private String className;
    @JsonProperty("method")
    private String methodName;
    @JsonProperty("entityModel")
    private EntityModelId model;
    @JsonProperty("sourceEntity")
    private EntityDef sourceEntity;
    @JsonProperty("targetEntity")
    private EntityDef targetEntity;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public EntityModelId getModel() {
        return model;
    }

    public void setModel(EntityModelId model) {
        this.model = model;
    }

    public EntityDef getSourceEntity() {
        return sourceEntity;
    }

    public void setSourceEntity(EntityDef sourceEntity) {
        this.sourceEntity = sourceEntity;
    }

    public EntityDef getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(EntityDef targetEntity) {
        this.targetEntity = targetEntity;
    }
}
