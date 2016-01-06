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

package com.dreameddeath.core.transcoder.json;

import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.entity.EntityDefinitionManager;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 07/11/2014.
 */
public class CouchbaseDocumentTypeIdResolver extends TypeIdResolverBase{
    private JavaType baseType;
    private Map<String,JavaType> mapClass = new ConcurrentHashMap<>();
    private EntityDefinitionManager entityDefinitionManager = new EntityDefinitionManager();

    public  CouchbaseDocumentTypeIdResolver() {
        super(null, null);
    }

    @Override
    public void init(JavaType baseType){
        this.baseType =baseType;
    }

    @Override
    public String idFromValue(Object value){
        DocumentDef annot=value.getClass().getAnnotation(DocumentDef.class);
        if(annot!=null){
            return EntityModelId.build(annot, value.getClass()).toString();
        }
        else{
            throw new RuntimeException("Need the DocumentRef annotation on class "+ value.getClass().getName());
        }
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType){
        return idFromValue(value);
    }

    @Override
    public String idFromBaseType() {
        DocumentDef annot = baseType.getRawClass().getAnnotation(DocumentDef.class);
        if(annot!=null){
            return EntityModelId.build(annot, baseType.getRawClass()).toString();
        }
        else{
            throw new RuntimeException("Need the DocumentRef annotation on class "+ baseType.getRawClass().getName());
        }
    }

    @Override @Deprecated @SuppressWarnings("deprecation")
    public JavaType typeFromId(String id) {
        return null;
    }

    public JavaType typeFromId(final DatabindContext context, final String id) {
        return mapClass.computeIfAbsent(id,
                typeId->context.getTypeFactory().constructType(entityDefinitionManager.findClassFromVersionnedTypeId(typeId))
        );
    }

    @Override
    public JsonTypeInfo.Id getMechanism(){
        return JsonTypeInfo.Id.CUSTOM;
    }
}
