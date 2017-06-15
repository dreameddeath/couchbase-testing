/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.couchbase.core.process.remote.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ParameterizedTypeInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.annotation.DtoModelJsonTypeId;
import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractDtoModelGenerator;
import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractDtoModelGenerator.UnwrappingStackElement;
import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.Key;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.plugin.IDtoModelGeneratorPlugin;
import com.dreameddeath.couchbase.core.process.remote.annotation.*;
import com.google.common.collect.Lists;
import com.squareup.javapoet.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by CEAJ8230 on 12/06/2017.
 */

public class ProcessRestDtoModelGeneratorPlugin implements IDtoModelGeneratorPlugin {
    @Override
    public boolean isApplicableToKey(Key dtoModelKey) {
        return RestExpose.REST_EXPOSE_DTO_MODEL_TYPE.equals(dtoModelKey.getType());
    }

    private RestExpose[] getAnnotForSuperClass(ClassInfo classInfo){
        if(classInfo.getSuperClass()!=null){
            ClassInfo superClass = classInfo.getSuperClass();
            RestExpose[] annots = superClass.getAnnotationByType(RestExpose.class);
            if(annots!=null && annots.length>0){
                return annots;
            }
            return getAnnotForSuperClass(superClass);
        }
        return null;
    }

    @Override
    public void generateIfNeeded(AbstractDtoModelGenerator abstractDtoModelGenerator, ClassInfo entityClassInfo) {
        RestExpose[] annots = entityClassInfo.getAnnotationByType(RestExpose.class);
        if(annots==null || annots.length==0){
            annots=getAnnotForSuperClass(entityClassInfo);
        }
        if(annots!=null && annots.length>0){
            for(RestExpose annot : annots){
                if(annot.pureSubClassMode()==DtoInOutMode.NONE || annot.pureSubClassMode()==DtoInOutMode.IN||annot.pureSubClassMode()==DtoInOutMode.BOTH) {
                    abstractDtoModelGenerator.generate(entityClassInfo, DtoInOutMode.IN, RestExpose.REST_EXPOSE_DTO_MODEL_TYPE, annot.version());
                }
                if(annot.pureSubClassMode()==DtoInOutMode.NONE || annot.pureSubClassMode()==DtoInOutMode.OUT||annot.pureSubClassMode()==DtoInOutMode.BOTH) {
                    abstractDtoModelGenerator.generate(entityClassInfo, DtoInOutMode.OUT, RestExpose.REST_EXPOSE_DTO_MODEL_TYPE, annot.version());
                }
            }
        }
    }


    private RestExpose getRestExposeAnnotForKey(ClassInfo clazz, Key key, List<UnwrappingStackElement> unwrappingStackElements,boolean lookOnSuperClass) {
        if(unwrappingStackElements!=null && unwrappingStackElements.size()>0){
            for(UnwrappingStackElement element:Lists.reverse(unwrappingStackElements)){
                if(!element.isForSuperclass()){
                    RestExpose annot = getRestExposeAnnotForKey(element.getFieldInfo().getDeclaringClassInfo(),key,Collections.emptyList(),false);
                    if(annot!=null){
                        return annot;
                    }
                }
            }
        }

        RestExpose[] annots = clazz.getAnnotationByType(RestExpose.class);
        if((annots==null ||annots.length==0) && lookOnSuperClass){
            annots=getAnnotForSuperClass(clazz);
        }
        if (annots != null && annots.length > 0) {
            for (RestExpose annot : annots) {
                if (/*(annot.pureSubClassMode()==DtoInOutMode.NONE) ||
                        (annot.getInOutMode()==DtoInOutMode.BOTH) ) &&*/
                    StringUtils.isEmpty(annot.version()) || annot.version().equals(key.getVersion())) {
                    return annot;
                }
            }
        }
        return null;
    }

    @Override
    public void addTypeInfo(TypeSpec.Builder dtoModelBuilder, ClassInfo clazz, Key key) {
        RestExpose annot = getRestExposeAnnotForKey(clazz, key, Collections.emptyList(),false);
        //Preconditions.checkNotNull(annot, "Cannot find applicable RestExpose annot on class %s for key %s", clazz.getFullName(), key);
        if (annot!=null && annot.pureSubClassMode()==DtoInOutMode.NONE) {
            dtoModelBuilder.addAnnotation(
                    AnnotationSpec.builder(RemoteServiceInfo.class)
                            .addMember("domain", "$S", annot.domain())
                            .addMember("name", "$S", annot.name())
                            .addMember("version", "$S", annot.version())
                            .build()
            );

            dtoModelBuilder.addAnnotation(
                    AnnotationSpec.builder(DtoModelRestApi.class)
                            .addMember("baseClass", "$S", clazz.getFullName())
                            .addMember("rootPath", "$S", annot.rootPath())
                            .addMember("mode", "$T.$L", DtoInOutMode.class, key.getInOutMode().name())
                            .addMember("status", "$T.$L", com.dreameddeath.core.service.annotation.VersionStatus.class, annot.status().name())
                            .addMember("version", "$S", annot.version())
                            .build()
            );
        }
    }

    @Override
    public SuperClassGenMode getSuperClassGeneratorMode(ClassInfo childClass, ClassInfo parentClazz, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        RestExpose annot = getRestExposeAnnotForKey(childClass,dtoModelKey,unwrappingStackElements,false);
        if(annot!=null){
            return annot.superClassGenMode();
        }
        else{
            //If parent has rest expose, force inherit
            RestExpose parentAnnot = getRestExposeAnnotForKey(parentClazz,dtoModelKey,unwrappingStackElements,false);
            if(parentAnnot!=null){
                return SuperClassGenMode.AUTO;
            }
            else if(parentClazz.getSuperClass()!=null){
                return getSuperClassGeneratorMode(parentClazz,parentClazz.getSuperClass(),dtoModelKey,unwrappingStackElements);
            }
        }
        return RestExpose.DEFAULT_SUPERCLASS_GEN_MODE;
    }


    private Request getRequestAnnotForKey(FieldInfo clazz,Key key) {
        Request[] annots = clazz.getAnnotationByType(Request.class);
        if (annots != null && annots.length > 0) {
            for (Request annot : annots) {
                if (StringUtils.isEmpty(annot.version()) || annot.version().equals(key.getVersion())) {
                    return annot;
                }
            }
        }
        return null;
    }


    private Result getResultAnnotForKey(FieldInfo clazz,Key key) {
        Result[] annots = clazz.getAnnotationByType(Result.class);
        if (annots != null && annots.length > 0) {
            for (Result annot : annots) {
                if (StringUtils.isEmpty(annot.version()) || annot.version().equals(key.getVersion())) {
                    return annot;
                }
            }
        }
        return null;
    }

    @Override
    public FieldGenMode getFieldGeneratorMode(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        boolean isInput = dtoModelKey.getInOutMode() == DtoInOutMode.IN;
        if(isInput){
            Request annot = getRequestAnnotForKey(field,dtoModelKey);
            if(annot!=null){ return annot.mode();}
        }
        else{
            Result annot = getResultAnnotForKey(field,dtoModelKey);
            if(annot!=null){return annot.mode();}
        }
        return null;
    }

    @Override
    public FieldGenMode getUnwrappedFieldGenMode(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        boolean isInput = dtoModelKey.getInOutMode() == DtoInOutMode.IN;
        if(isInput){
            Request annot = getRequestAnnotForKey(field,dtoModelKey);
            if(annot!=null){ return annot.unwrappedDefaultMode();}
        }
        else{
            Result annot = getResultAnnotForKey(field,dtoModelKey);
            if(annot!=null){return annot.unwrappedDefaultMode();}
        }

        return FieldGenMode.INHERIT;
    }

    @Override
    public String getFieldEffectiveName(FieldInfo field, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        boolean isInput = key.getInOutMode() == DtoInOutMode.IN;
        if(isInput){
            Request annot = getRequestAnnotForKey(field,key);
            if(annot!=null && StringUtils.isNotEmpty(annot.value())){return annot.value();}
        }
        else{
            Result annot = getResultAnnotForKey(field,key);
            if(annot!=null && StringUtils.isNotEmpty(annot.value())){return annot.value();}
        }
        return null;
    }

    @Override
    public void addFieldInfo(String name, FieldSpec.Builder fieldBuilder, ParameterizedTypeInfo effectiveTypeInfo, FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
    }

    @Override
    public void addSetterInfo(String name, MethodSpec.Builder setterBuilder, ParameterizedTypeInfo effectiveTypeInfo, FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
    }

    @Override
    public void addGetterInfo(String name, MethodSpec.Builder setterBuilder, ParameterizedTypeInfo effectiveTypeInfo, FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
    }

    @Override
    public Collection<String> getSupportedAnnotations() {
        return Collections.singleton(RestExpose.class.getCanonicalName());
    }

    @Override
    public Key getKey(ClassInfo clazz, DtoInOutMode mode, String type, String version) {
        if(!RestExpose.REST_EXPOSE_DTO_MODEL_TYPE.equals(type)){
            return null;
        }
        String packageName = clazz.getPackageInfo().getName()+".published";
        String className=clazz.getSimpleName().replaceAll("\\$","");
        if(mode==DtoInOutMode.IN){
            className+="Request";
        }
        else{
            className+="Response";
        }
        return new Key(packageName,className,mode,type,version);
    }

    @Override
    public void addHierarchyBasedTypeInfo(TypeSpec.Builder typeBuilder, ClassInfo clazz, Key key, ClassName superClassDtoName) {
        RestExpose annot = getRestExposeAnnotForKey(clazz,key,null,false);

        if(superClassDtoName!=null && !clazz.isAbstract()) {
            String jsonTypeId = null;
            if(annot!=null){
                jsonTypeId = annot.jsonTypeId();
            }
            if (StringUtils.isEmpty(jsonTypeId)) {
                jsonTypeId = key.getClassName();
            }
            typeBuilder.addAnnotation(
                    AnnotationSpec.builder(DtoModelJsonTypeId.class)
                            .addMember("value", "$S", jsonTypeId)
                            .build()
            );
        }
    }

    @Override
    public FieldGenMode getFieldGeneratorModeDefaultFromClass(ClassInfo rootClassInfo, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        boolean isInput = dtoModelKey.getInOutMode() == DtoInOutMode.IN;
        RestExpose exposeAnnotForKey = getRestExposeAnnotForKey(rootClassInfo,dtoModelKey,unwrappingStackElements,true);
        if(exposeAnnotForKey!=null) {
            if (isInput) {
                return exposeAnnotForKey.defaultInputFieldMode();
            }
            else{
                return exposeAnnotForKey.defaultOutputFieldMode();
            }
        }
        return FieldGenMode.FILTER;
    }
}
