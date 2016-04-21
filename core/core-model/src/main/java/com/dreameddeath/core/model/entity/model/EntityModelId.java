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

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.PackageInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentPackageDefault;
import com.fasterxml.jackson.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dreameddeath.core.model.entity.model.EntityVersion.EMPTY_VERSION;

/**
 * Created by Christophe Jeunesse on 12/10/2015.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility= JsonAutoDetect.Visibility.NONE,fieldVisibility= JsonAutoDetect.Visibility.NONE,isGetterVisibility = JsonAutoDetect.Visibility.NONE,setterVisibility = JsonAutoDetect.Visibility.NONE,creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class EntityModelId {
    private static final Logger LOG= LoggerFactory.getLogger(EntityModelId.class);
    public static final char ENTITY_SPERATOR='/';
    public static final String ENTITY_PATTERN_STRING = "(\\w+)/(\\w+)";
    public static final Pattern ENTITY_PATTERN=Pattern.compile("^"+ENTITY_PATTERN_STRING+"$");
    public static final Pattern FULL_ENTITY_ID= Pattern.compile("^"+ENTITY_PATTERN_STRING+"/("+EntityVersion.VERSION_PATTERN_STR+")$");
    public static final EntityModelId EMPTY_MODEL_ID=new EntityModelId(null,null, EMPTY_VERSION);


    @JsonProperty("domain")
    private String domain;
    @JsonProperty("name")
    private String name;
    @JsonProperty("version")
    private EntityVersion entityVersion;

    public EntityModelId(String entity,String version){
        parseEntityString(entity);
        entityVersion = version!=null?EntityVersion.version(version):null;
    }


    @JsonCreator
    public EntityModelId(@JsonProperty("domain") String domain,@JsonProperty("name") String name,@JsonProperty("version") EntityVersion version){
        this.domain = domain;
        this.name = name;
        entityVersion = version;
    }

    public EntityModelId(String domain,String name,String version){
        this.domain = domain;
        this.name = name;
        entityVersion = version!=null?EntityVersion.version(version):null;
    }

    private static String getInfo(PackageInfo pkg,boolean isVersion){
        DocumentPackageDefault annot=pkg.getAnnotation(DocumentPackageDefault.class);
        if(annot!=null){
            String info =isVersion?annot.version():annot.domain();
            if(StringUtils.isNotEmpty(info)){
                 return info;
            }
        }
        PackageInfo parent = pkg.getParentPackage();
        String info=null;
        if(parent!=null){
            info=getInfo(parent,isVersion);
        }

        if(info==null){
            LOG.error("Cannot find {} in Package {}",isVersion?"version":"domain",pkg.getName());
        }
        return info;
    }

    private static String getInfo(DocumentEntity documentEntity, AbstractClassInfo cls, boolean isVersion) {
        String info = isVersion?documentEntity.version(): documentEntity.domain();

        if (StringUtils.isEmpty(info)) {
            PackageInfo packageInfo=cls.getPackageInfo();
            info=getInfo(packageInfo,isVersion);
        }
        if (StringUtils.isEmpty(info)) {
            StringBuilder parents = new StringBuilder();
            PackageInfo currPackageInfo = cls.getPackageInfo();
            while(currPackageInfo!=null){
                parents.append(currPackageInfo.getName());
            }
            throw new RuntimeException("Cannot get "+ (isVersion?"version":"domain")+ " of element "+cls.getImportName()+ " in parents <"+parents+">");
        }
        else{
            return info;
        }
    }


    public EntityModelId(DocumentEntity documentEntity, AbstractClassInfo classInfo) {
        this(
                getInfo(documentEntity,classInfo,false),
                StringUtils.isEmpty(documentEntity.name()) ? classInfo.getSimpleName().toLowerCase() : documentEntity.name(),
                getInfo(documentEntity,classInfo,true));
    }

    public EntityModelId(DocumentEntity documentEntity, Element elt) {
        this(documentEntity,ClassInfo.getClassInfo((TypeElement)elt));
    }

    public EntityModelId(DocumentEntity documentEntity, Class<?> clazz){
        this(documentEntity,ClassInfo.getClassInfo(clazz));
    }

    public EntityModelId(String fullString){
        parseFullEntityIdString(fullString);
    }


    public String getDomain() {
        return domain;
    }

    public String getName() {
        return name;
    }

    public EntityVersion getEntityVersion() {
        return entityVersion;
    }

    private void parseEntityString(String entityString){
        Matcher matcher = ENTITY_PATTERN.matcher(entityString);
        if(matcher.matches()){
            domain = matcher.group(1);
            name = matcher.group(2);
        }
        else{
            throw new IllegalArgumentException("The model id <"+entityString+"> hasn't the correct model id syntax");
        }
    }

    private void parseFullEntityIdString(String fullString){
        Matcher matcher = FULL_ENTITY_ID.matcher(fullString);
        if(matcher.matches()){
            domain = matcher.group(1);
            name = matcher.group(2);
            entityVersion = EntityVersion.version(matcher.group(4), matcher.group(5), matcher.group(6));
        }
        else{
            throw new IllegalArgumentException("The model id <"+fullString+"> hasn't the correct model id syntax");
        }
    }

    public void setFullEntityId(String fullEntityId){
        parseFullEntityIdString(fullEntityId);
    }

    public String toString(){
        return domain+"/"+name+((entityVersion!=null)?"/"+entityVersion.toString():"");
    }

    public String getClassUnivoqueModelId(){
        return domain+"/"+name+((entityVersion!=null)?"/"+entityVersion.getMajor().toString():"");
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityModelId modelId = (EntityModelId) o;

        if (domain != null ? !domain.equals(modelId.domain) : modelId.domain != null) return false;
        if (name != null ? !name.equals(modelId.name) : modelId.name != null) return false;
        return !(entityVersion != null ? !entityVersion.equals(modelId.entityVersion) : modelId.entityVersion != null);

    }

    @Override
    public int hashCode() {
        int result = domain != null ? domain.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (entityVersion != null ? entityVersion.hashCode() : 0);
        return result;
    }

    public static EntityModelId build(String fullIdString){
        return new EntityModelId(fullIdString);
    }

    public static EntityModelId build(DocumentEntity annot, ClassInfo classInfo){
        if(classInfo.getCurrentClass()!=null){
            return EntityModelId.build(annot,classInfo.getCurrentClass());
        }
        else{
            return EntityModelId.build(annot,classInfo.getTypeElement());
        }
    }



    public static EntityModelId build(String domain,String name,String version){
        return new EntityModelId(domain,name,version);
    }

    public static EntityModelId build(DocumentEntity annot, Element elt){
        return new EntityModelId(annot,elt);
    }

    public static EntityModelId build(DocumentEntity annot, Class clazz){
        return new EntityModelId(annot,clazz);
    }

    public static EntityModelId buildPartial(String partialId){
        return new EntityModelId(partialId,null);
    }

    public static EntityModelId buildPartial(String domain,String name){
        return new EntityModelId(domain,name,(EntityVersion)null);
    }
}
