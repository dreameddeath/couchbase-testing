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

package com.dreameddeath.core.query.annotation.processor;

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
import com.dreameddeath.core.query.annotation.DtoModelQueryRestApi;
import com.dreameddeath.core.query.annotation.QueryExpose;
import com.dreameddeath.core.query.annotation.QueryFieldInfo;
import com.dreameddeath.core.query.annotation.RemoteQueryInfo;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.squareup.javapoet.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by CEAJ8230 on 12/06/2017.
 */

public class QueryDtoModelGeneratorPlugin implements IDtoModelGeneratorPlugin {
    @Override
    public boolean isApplicableToKey(Key dtoModelKey) {
        return QueryExpose.REST_EXPOSE_DTO_MODEL_TYPE.equals(dtoModelKey.getType());
    }

    private QueryExpose[] getAnnotForSuperClass(ClassInfo classInfo){
        if(classInfo.getSuperClass()!=null){
            ClassInfo superClass = classInfo.getSuperClass();
            QueryExpose[] annots = superClass.getAnnotationByType(QueryExpose.class);
            if(annots!=null && annots.length>0){
                return annots;
            }
            return getAnnotForSuperClass(superClass);
        }
        return null;
    }

    @Override
    public void generateIfNeeded(AbstractDtoModelGenerator abstractDtoModelGenerator, ClassInfo entityClassInfo) {
        QueryExpose[] annots = entityClassInfo.getAnnotationByType(QueryExpose.class);
        if(annots==null || annots.length==0){
            annots=getAnnotForSuperClass(entityClassInfo);
        }
        if(annots!=null && annots.length>0){
            for(QueryExpose annot : annots){
                abstractDtoModelGenerator.generate(entityClassInfo, DtoInOutMode.OUT, QueryExpose.REST_EXPOSE_DTO_MODEL_TYPE, annot.version());
            }
        }
    }


    private QueryExpose getRestExposeAnnotForKey(ClassInfo clazz, Key key, List<UnwrappingStackElement> unwrappingStackElements,boolean lookOnSuperClass) {
        if(unwrappingStackElements!=null && unwrappingStackElements.size()>0){
            for(UnwrappingStackElement element:Lists.reverse(unwrappingStackElements)){
                if(!element.isForSuperclass()){
                    QueryExpose annot = getRestExposeAnnotForKey(element.getFieldInfo().getDeclaringClassInfo(),key,Collections.emptyList(),false);
                    if(annot!=null){
                        return annot;
                    }
                }
            }
        }

        QueryExpose[] annots = clazz.getAnnotationByType(QueryExpose.class);
        if((annots==null ||annots.length==0) && lookOnSuperClass){
            annots=getAnnotForSuperClass(clazz);
        }
        if (annots != null && annots.length > 0) {
            for (QueryExpose annot : annots) {
                if (/*(annot.isPureSubClassMode()==DtoInOutMode.NONE) ||
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
        QueryExpose annot = getRestExposeAnnotForKey(clazz, key, Collections.emptyList(),false);
        String name;
        if(annot!=null && StringUtils.isNotEmpty(annot.name())){
            name = annot.name();
        }
        else{
            name=clazz.getSimpleName();
        }
        //Preconditions.checkNotNull(annot, "Cannot find applicable RestExpose annot on class %s for key %s", clazz.getFullName(), key);
        if (annot!=null && !annot.isPureSubClassMode()) {
            dtoModelBuilder.addAnnotation(
                    AnnotationSpec.builder(RemoteQueryInfo.class)
                            .addMember("domain", "$S", annot.domain())
                            .addMember("name", "$S", name)
                            .addMember("version", "$S", annot.version())
                            .build()
            );

            dtoModelBuilder.addAnnotation(
                    AnnotationSpec.builder(DtoModelQueryRestApi.class)
                            .addMember("baseClass", "$S", clazz.getFullName())
                            .addMember("rootPath", "$S", annot.rootPath())
                            .addMember("domain", "$S", annot.domain())
                            .addMember("status", "$T.$L", VersionStatus.class, annot.status().name())
                            .addMember("version", "$S", annot.version())
                            .build()
            );
        }
    }

    @Override
    public SuperClassGenMode getSuperClassGeneratorMode(ClassInfo childClass, ClassInfo parentClazz, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        QueryExpose annot = getRestExposeAnnotForKey(childClass,dtoModelKey,unwrappingStackElements,false);
        if(annot!=null){
            return annot.superClassGenMode();
        }
        else{
            //If parent has rest expose, force inherit
            QueryExpose parentAnnot = getRestExposeAnnotForKey(parentClazz,dtoModelKey,unwrappingStackElements,false);
            if(parentAnnot!=null){
                return SuperClassGenMode.AUTO;
            }
            else if(parentClazz.getSuperClass()!=null){
                return getSuperClassGeneratorMode(parentClazz,parentClazz.getSuperClass(),dtoModelKey,unwrappingStackElements);
            }
        }
        return QueryExpose.DEFAULT_SUPERCLASS_GEN_MODE;
    }

    private QueryFieldInfo getFieldAnnotForKey(FieldInfo clazz, Key key) {
        QueryFieldInfo[] annots = clazz.getAnnotationByType(QueryFieldInfo.class);
        if (annots != null && annots.length > 0) {
            for (QueryFieldInfo annot : annots) {
                if (StringUtils.isEmpty(annot.version()) || annot.version().equals(key.getVersion())) {
                    return annot;
                }
            }
        }
        return null;
    }

    @Override
    public FieldGenMode getFieldGeneratorMode(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        Preconditions.checkArgument(dtoModelKey.getInOutMode() == DtoInOutMode.OUT,"Allowed only for output mode for query for field %s",field.getFullName());
        QueryFieldInfo annot = getFieldAnnotForKey(field,dtoModelKey);
        if(annot!=null){return annot.mode();}
        return null;
    }

    @Override
    public FieldGenMode getUnwrappedFieldGenMode(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        Preconditions.checkArgument(dtoModelKey.getInOutMode() == DtoInOutMode.OUT,"Allowed only for output mode for query for field %s",field.getFullName());
        QueryFieldInfo annot = getFieldAnnotForKey(field,dtoModelKey);
        if(annot!=null){return annot.unwrappedDefaultMode();}
        return FieldGenMode.INHERIT;
    }

    @Override
    public String getFieldEffectiveName(FieldInfo field, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        Preconditions.checkArgument(key.getInOutMode() == DtoInOutMode.OUT,"Allowed only for output mode for query for field %s",field.getFullName());
        QueryFieldInfo annot = getFieldAnnotForKey(field,key);
        if(annot!=null && StringUtils.isNotEmpty(annot.value())){return annot.value();}
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
        return Collections.singleton(QueryExpose.class.getCanonicalName());
    }

    @Override
    public Key getKey(ClassInfo clazz, DtoInOutMode mode, String type, String version) {
        if(!QueryExpose.REST_EXPOSE_DTO_MODEL_TYPE.equals(type)){
            return null;
        }
        String packageName = clazz.getPackageInfo().getName()+".published";
        String className=clazz.getSimpleName().replaceAll("\\$","");
        Preconditions.checkArgument(mode == DtoInOutMode.OUT,"Allowed only for output mode for query for clazz %s",clazz.getFullName());
        className+="Response";
        return new Key(packageName,className,mode,type,version);
    }

    @Override
    public void addHierarchyBasedTypeInfo(TypeSpec.Builder typeBuilder, ClassInfo clazz, Key key, ClassName superClassDtoName) {
        QueryExpose annot = getRestExposeAnnotForKey(clazz,key,null,false);

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
        Preconditions.checkArgument(dtoModelKey.getInOutMode() == DtoInOutMode.OUT,"Allowed only for output mode for query for field %s",rootClassInfo.getFullName());

        QueryExpose exposeAnnotForKey = getRestExposeAnnotForKey(rootClassInfo,dtoModelKey,unwrappingStackElements,true);
        if(exposeAnnotForKey!=null) {
            return exposeAnnotForKey.defaultOutputFieldMode();
        }
        return QueryExpose.DEFAULT_OUTPUT_GEN_MODE;
    }
}
