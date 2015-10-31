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

package com.dreameddeath.core.dao.model.discovery;

import com.couchbase.client.deps.com.fasterxml.jackson.annotation.JsonIgnore;
import com.couchbase.client.deps.com.fasterxml.jackson.annotation.JsonProperty;
import com.dreameddeath.core.curator.model.IRegisterable;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.model.entity.EntityDefinitionManager;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 28/10/2015.
 */
public class DaoInstanceInfo implements IRegisterable {
    @JsonProperty("uuid")
    private UUID uuid;
    @JsonProperty("className")
    private String className;
    @JsonProperty("mainEntity")
    private EntityDef mainEntity;
    @JsonProperty("childEntities")
    private List<EntityDef> childEntities= new ArrayList<>();
    @JsonProperty("bucketName")
    private String bucketName;
    @JsonProperty("readOnly")
    private boolean readOnly;
    /*@JsonProperty("views")
    private List<ViewInfo> viewInfoList;
    @JsonProperty("counters")
    private List<CounterInfo> counterInfoList;*/


    public DaoInstanceInfo(){}

    public DaoInstanceInfo(CouchbaseDocumentDao dao){
        this.uuid= dao.getUuid();
        this.className = dao.getClass().getName();
        this.bucketName=dao.getClient().getBucketName();
        this.readOnly=dao.isReadOnly();

        CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(dao.getBaseClass());

        this.mainEntity = EntityDef.build(structureReflection);
        for(EntityDef entity: new EntityDefinitionManager().getEntities()){
            if(entity.getParentEntities().contains(this.mainEntity)){
                this.childEntities.add(entity);
            }
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public EntityDef getMainEntity() {
        return mainEntity;
    }

    public void setMainEntity(EntityDef mainEntity) {
        this.mainEntity = mainEntity;
    }

    public List<EntityDef> getChildEntities() {
        return Collections.unmodifiableList(childEntities);
    }

    public void setChildEntities(List<EntityDef> childEntities) {
        this.childEntities.clear();
        this.childEntities.addAll(childEntities);
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override @JsonIgnore
    public String getUid() {
        return uuid.toString();
    }
}
