package com.dreameddeath.core.model.v2.meta;

import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public final class VersionMetaInfo {
    private final EntityModelId dbModelId;
    private final EntityModelId currModelId;

    @JsonCreator
    public VersionMetaInfo(EntityModelId modelId){
        this.dbModelId = modelId;
        this.currModelId = modelId;
    }

    public VersionMetaInfo(Class<?> clazz, EntityModelId dbVersion){
        this.dbModelId = dbVersion;
        this.currModelId = EntityDef.build(CouchbaseDocumentStructureReflection.getReflectionFromClass(clazz)).getModelId();
    }

    public VersionMetaInfo(Class<?> clazz){
       this.currModelId = EntityDef.build(CouchbaseDocumentStructureReflection.getReflectionFromClass(clazz)).getModelId();
       this.dbModelId = null;
    }

    public final EntityModelId dbModelId(){
        return dbModelId;
    }

    @JsonValue
    public final EntityModelId modelId(){
        return currModelId;
    }

}
