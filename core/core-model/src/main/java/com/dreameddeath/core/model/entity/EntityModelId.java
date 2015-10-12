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

package com.dreameddeath.core.model.entity;

import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.annotation.DocumentDef;

import javax.lang.model.element.Element;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 12/10/2015.
 */
public class EntityModelId {
    public static final String ENTITY_PATTERN_STRING = "(\\w+)/(\\w+)";
    public static final Pattern ENTITY_PATTERN=Pattern.compile("^"+ENTITY_PATTERN_STRING+"$");
    public static final Pattern FULL_ENTITY_ID= Pattern.compile("^"+ENTITY_PATTERN_STRING+"/("+EntityVersion.VERSION_PATTERN_STR+")$");

    private String _domain;
    private String _name;
    private EntityVersion _entityVersion;

    public EntityModelId(String entity,String version){
        parseEntityString(entity);
        _entityVersion = version!=null?EntityVersion.version(version):null;
    }

    public EntityModelId(String domain,String name,EntityVersion version){
        _domain = domain;
        _name = name;
        _entityVersion = version;
    }

    public EntityModelId(String domain,String name,String version){
        _domain = domain;
        _name = name;
        _entityVersion = version!=null?EntityVersion.version(version):null;
    }

    public EntityModelId(DocumentDef documentDef,Element elt) {
        this(documentDef.domain(),StringUtils.isEmpty(documentDef.name())?elt.getSimpleName().toString():documentDef.name(),documentDef.version());
    }

    public EntityModelId(DocumentDef documentDef,Class<?> clazz){
        this(documentDef.domain(),StringUtils.isEmpty(documentDef.name())?clazz.getSimpleName():documentDef.name(),documentDef.version());
    }

    public EntityModelId(String fullString){
        parseFullEntityIdString(fullString);
    }


    public String getDomain() {
        return _domain;
    }

    public String getName() {
        return _name;
    }

    public EntityVersion getEntityVersion() {
        return _entityVersion;
    }

    private void parseEntityString(String entityString){
        Matcher matcher = ENTITY_PATTERN.matcher(entityString);
        if(matcher.matches()){
            _domain = matcher.group(1);
            _name = matcher.group(2);
        }
        else{
            throw new IllegalArgumentException("The model id <"+entityString+"> hasn't the correct model id syntax");
        }
    }

    private void parseFullEntityIdString(String fullString){
        Matcher matcher = FULL_ENTITY_ID.matcher(fullString);
        if(matcher.matches()){
            _domain = matcher.group(1);
            _name = matcher.group(2);
            _entityVersion = EntityVersion.version(matcher.group(4), matcher.group(5), matcher.group(6));
        }
        else{
            throw new IllegalArgumentException("The model id <"+fullString+"> hasn't the correct model id syntax");
        }
    }


    public void setFullEntityId(String fullEntityId){
        parseFullEntityIdString(fullEntityId);
    }

    public String toString(){
        return _domain+"/"+_name+"/"+_entityVersion.toString();
    }

    public static EntityModelId build(String fullIdString){
        return new EntityModelId(fullIdString);
    }

    public static EntityModelId build(String domain,String name,String version){
        return new EntityModelId(domain,name,version);
    }

    public static EntityModelId build(DocumentDef annot,Element elt){
        return new EntityModelId(annot,elt);
    }

    public static EntityModelId build(DocumentDef annot,Class<?> clazz){
        return new EntityModelId(annot,clazz);
    }

    public static EntityModelId buildPartial(String partialId){
        return new EntityModelId(partialId,null);
    }
}
