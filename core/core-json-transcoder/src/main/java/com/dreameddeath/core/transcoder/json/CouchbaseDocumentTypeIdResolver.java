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
import com.dreameddeath.core.model.entity.EntityModelId;
import com.dreameddeath.core.model.upgrade.VersionUpgradeManager;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import java.util.HashMap;
import java.util.Map;
/**
 * Created by Christophe Jeunesse on 07/11/2014.
 */
public class CouchbaseDocumentTypeIdResolver extends TypeIdResolverBase{
    private JavaType _baseType;
    private Map<String,JavaType> _mapClass = new HashMap<>();
    private VersionUpgradeManager _versionUpgradeManager;

    public  CouchbaseDocumentTypeIdResolver() {
        super(null, null);
    }

    @Override
    public void init(JavaType baseType){
        _baseType =baseType;
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
        DocumentDef annot = _baseType.getRawClass().getAnnotation(DocumentDef.class);
        if(annot!=null){
            return EntityModelId.build(annot, _baseType.getRawClass()).toString();
        }
        else{
            throw new RuntimeException("Need the DocumentRef annotation on class "+ _baseType.getRawClass().getName());
        }
    }

    @Override @Deprecated @SuppressWarnings("deprecation")
    public JavaType typeFromId(String id) {
        return null;
    }

    public JavaType typeFromId(DatabindContext context, String id) {
        if(!_mapClass.containsKey(id)) {
            if (_versionUpgradeManager == null){
                _versionUpgradeManager = (VersionUpgradeManager)context.getConfig().getAttributes().getAttribute(VersionUpgradeManager.class);
                if(_versionUpgradeManager==null){
                    _versionUpgradeManager=new VersionUpgradeManager();
                }
            }
            _mapClass.put(id, context.getTypeFactory().constructType(_versionUpgradeManager.findClassFromVersionnedTypeId(id)));
        }
        return _mapClass.get(id);
    }

    @Override
    public JsonTypeInfo.Id getMechanism(){
        return JsonTypeInfo.Id.CUSTOM;
    }
}
