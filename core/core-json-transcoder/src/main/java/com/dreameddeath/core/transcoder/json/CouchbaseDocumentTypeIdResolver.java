/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.transcoder.json;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.upgrade.Utils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
/**
 * Created by ceaj8230 on 07/11/2014.
 */
public class CouchbaseDocumentTypeIdResolver extends TypeIdResolverBase{
    private static final String VERSION="\\d+\\.\\d+\\.\\d+";
    private static final String ID="(\\w+)/(\\w+)/("+VERSION+")";
    private static final String FORMAT="%s/%s/%s";
    private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION);
    private static final Pattern ID_PATTERN = Pattern.compile(ID);

    private JavaType _baseType;
    private String _domain;
    private Map<String,JavaType> _mapClass = new HashMap<>();


    public  CouchbaseDocumentTypeIdResolver() {
        super(null, null);
    }

    @Override
    public void init(JavaType baseType){
        _baseType =baseType;
        DocumentDef annot =_baseType.getRawClass().getAnnotation(DocumentDef.class);
        if(annot!=null){
            _domain = annot.domain();
        }
    }

    @Override
    public String idFromValue(Object value){
        DocumentDef annot=value.getClass().getAnnotation(DocumentDef.class);
        if(annot!=null){
            return Utils.buildVersionnedTypeId(annot, value.getClass());
        }
        else{
            throw new RuntimeException("Need the DocumentRef annotation on class "+ value.getClass().getName());
        }
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType){
        return idFromValue(value);
    }

    @Override @Deprecated
    public String idFromBaseType() {
        DocumentDef annot = _baseType.getRawClass().getAnnotation(DocumentDef.class);
        if(annot!=null){
            return Utils.buildVersionnedTypeId(annot, _baseType.getRawClass());
        }
        else{
            throw new RuntimeException("Need the DocumentRef annotation on class "+ _baseType.getRawClass().getName());
        }
    }

    @Override
    public JavaType typeFromId(String id) {
        return null;
    }

    public JavaType typeFromId(DatabindContext context, String id) {
        if(!_mapClass.containsKey(id)){
            _mapClass.put(id, context.getTypeFactory().constructType(Utils.findClassFromVersionnedTypeId(id)));
        }
        return _mapClass.get(id);
    }

    @Override
    public JsonTypeInfo.Id getMechanism(){
        return JsonTypeInfo.Id.CUSTOM;
    }
}
