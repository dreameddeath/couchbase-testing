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

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 13/10/2015.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility= JsonAutoDetect.Visibility.NONE,fieldVisibility= JsonAutoDetect.Visibility.NONE,isGetterVisibility = JsonAutoDetect.Visibility.NONE,setterVisibility = JsonAutoDetect.Visibility.NONE,creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class EntityDef {
    @JsonProperty("modelId")
    private EntityModelId modelId;
    @JsonProperty("className")
    private String className;
    @JsonProperty("parents")
    private List<EntityModelId> parentEntities=new ArrayList<>();

    public EntityModelId getModelId() {
        return modelId;
    }

    public void setModelId(EntityModelId modelId) {
        this.modelId = modelId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<EntityModelId> getParentEntities() {
        return Collections.unmodifiableList(parentEntities);
    }

    public void setParentEntities(List<EntityModelId> parentClasses) {
        this.parentEntities.clear();
        this.parentEntities.addAll(parentClasses);
    }

    public static EntityDef build(CouchbaseDocumentStructureReflection documentDef){
        EntityDef result = new EntityDef();
        if(documentDef.getClassInfo().getTypeElement()!=null) {
            result.setModelId(EntityModelId.build(documentDef.getClassInfo().getAnnotation(DocumentEntity.class), documentDef.getClassInfo().getTypeElement()));
        }
        else{
            result.setModelId(EntityModelId.build(documentDef.getClassInfo().getAnnotation(DocumentEntity.class), documentDef.getClassInfo().getCurrentClass()));
        }
        result.setClassName(documentDef.getClassInfo().getFullName());
        CouchbaseDocumentStructureReflection currDocReflection = documentDef;
        while(currDocReflection.getSuperclassReflexion()!=null){
            currDocReflection = currDocReflection.getSuperclassReflexion();
            if(currDocReflection.getEntityModelId()!=null && !currDocReflection.getEntityModelId().equals(EntityModelId.EMPTY_MODEL_ID)) {
                result.parentEntities.add(currDocReflection.getEntityModelId());
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityDef entityDef = (EntityDef) o;

        return modelId.equals(entityDef.modelId);
    }

    @Override
    public int hashCode() {
        return modelId.hashCode();
    }
}
