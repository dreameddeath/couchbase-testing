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

package com.dreameddeath.core.model.dto.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ParameterizedTypeInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.dto.annotation.DtoFieldMappingInfo;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.annotation.DtoModelJsonTypeId;
import com.dreameddeath.core.model.dto.annotation.DtoModelMappingInfo;
import com.dreameddeath.core.model.dto.annotation.processor.model.plugin.IDtoModelGeneratorPlugin;
import com.dreameddeath.core.model.dto.json.DtoModelTypeIdResolver;
import com.dreameddeath.core.model.dto.model.manager.DtoModelManager;
import com.dreameddeath.core.model.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by CEAJ8230 on 01/06/2017.
 */
public class StandardCouchbaseDocumentDtoModelGenerator extends AbstractDtoModelGenerator {

    public StandardCouchbaseDocumentDtoModelGenerator(DtoModelManager manager) {
        super(manager);
    }

    @Override
    public void generateIfNeeded(ClassInfo entityClassInfo) {
        List<DtoGenerate> generateList = getDtoGenerateAnnotation(entityClassInfo);

        for (IDtoModelGeneratorPlugin plugin:plugins) {
            plugin.generateIfNeeded(this,entityClassInfo);
        }

        if(generateList.size()>0){
            for(DtoGenerate generate:generateList){
                if(generate.buildForTypes().length>0){
                    for(DtoGenerateType type :generate.buildForTypes()){
                        if(generate.mode()!=DtoInOutMode.NONE) {
                            generate(entityClassInfo, generate.mode(), type.typeName(), type.version());
                        }
                    }
                }
                else{
                    if(generate.mode()!=DtoInOutMode.NONE) {
                        generate(entityClassInfo, generate.mode(), DtoGenerateType.DEFAULT_TYPE_NAME, DtoGenerateType.DEFAULT_VERSION);
                    }
                }
            }
        }
        else{
            if(DtoGenerate.DEFAULT_MODE!=DtoInOutMode.NONE) {
                generate(entityClassInfo, DtoGenerate.DEFAULT_MODE, DtoGenerateType.DEFAULT_TYPE_NAME, DtoGenerateType.DEFAULT_VERSION);
            }
        }
    }

    private List<DtoGenerate> getDtoGenerateAnnotation(ClassInfo entityClassInfo) {
        List<DtoGenerate> annotationLists = new ArrayList<>();
        DtoGenerate[] annotations = entityClassInfo.getAnnotationByType(DtoGenerate.class);
        if(annotations!=null) {
            annotationLists.addAll(Arrays.asList(annotations));
        }
        if(entityClassInfo.getSuperClass()!=null){
            annotationLists.addAll(getDtoGenerateAnnotation(entityClassInfo.getSuperClass()));
        }
        if(annotationLists.size()==0){
            if(entityClassInfo.getEnclosingClass()!=null && entityClassInfo.getEnclosingClass() instanceof ClassInfo){
                annotationLists.addAll(getDtoGenerateAnnotation((ClassInfo)entityClassInfo.getEnclosingClass()));
            }
        }
        return annotationLists;
    }

    protected boolean hasType(DtoGenerate annot,String type, String version){
        return getDtoType(annot,type,version)!=null;
    }

    protected Optional<DtoGenerateType> getDtoType(DtoGenerate annot, String type, String version){
        for(DtoGenerateType dtoTypeGenerate : annot.buildForTypes()){
            if(dtoTypeGenerate.typeName().equals(type) && (StringUtils.isEmpty(dtoTypeGenerate.version()) || dtoTypeGenerate.version().equals(version))){
                return Optional.of(dtoTypeGenerate);
            }
        }
        return Optional.empty();
    }

    protected ClassInfo getRootClassInfo(FieldInfo field,List<UnwrappingStackElement> unwrappingStackElementList){
        if(unwrappingStackElementList.size()>0){
            if(unwrappingStackElementList.get(0).isForSuperclass()){
                return unwrappingStackElementList.get(0).getChildClass();
            }
            else{
                return unwrappingStackElementList.get(0).getFieldInfo().getDeclaringClassInfo();
            }
        }
        else{
            return field.getDeclaringClassInfo();
        }
    }

    private Optional<DtoGenerate> getDtoGenerate(ClassInfo classInfo, String type, String version){
        return getDtoGenerateAnnotation(classInfo).stream()
                .filter(dtoGenerate -> hasType(dtoGenerate,type,version))
                .findFirst();
    }

    private Optional<DtoGenerateType> getDtoType(ClassInfo classInfo, String type, String version){
        return getDtoGenerate(classInfo,type,version)
                .flatMap(dtoGenerate -> getDtoType(dtoGenerate,type,version));
    }

    @Override
    protected Key getKey(ClassInfo clazz, DtoInOutMode mode, String type, String version) {
        for(IDtoModelGeneratorPlugin plugin:plugins){
            Key key = plugin.getKey(clazz,mode,type,version);
            if(key!=null){
                return key;
            }
        }

        Optional<DtoGenerate> annot = getDtoGenerateAnnotation(clazz).stream()
                .filter(dtoGenerate -> hasType(dtoGenerate,type,version))
                .findFirst();
        String packageName="";
        String className="";
        if(annot.isPresent()){
            packageName=annot.get().targetModelPackageName();
            className=annot.get().targetModelClassName();
        }
        if(StringUtils.isEmpty(packageName)){
            packageName = clazz.getPackageInfo().getName()+".published";
        }
        if(StringUtils.isEmpty(className)){
            className=clazz.getSimpleName().replaceAll("\\$","");
            if(mode==DtoInOutMode.IN){
                className+="Input";
            }
            else if(mode==DtoInOutMode.OUT){
                className+="Output";
            }
        }
        return new Key(packageName,className,mode,type,version);
    }

    @Override
    protected SuperClassGenMode getSuperClassGeneratorMode(ClassInfo childClass,ClassInfo parentClazz, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        if(unwrappingStackElements.size()>0){
            if(unwrappingStackElements.get(0).isForSuperclass()){
                return SuperClassGenMode.UNWRAP;
            }
            return SuperClassGenMode.IGNORE;
        }

        if(CouchbaseDocumentStructureReflection.isReflexible(parentClazz) && (!parentClazz.equals(ClassInfo.getClassInfo(CouchbaseDocument.class)) && !parentClazz.equals(ClassInfo.getClassInfo(CouchbaseDocumentElement.class)))){
            for(IDtoModelGeneratorPlugin plugin:getApplicablePlugins(dtoModelKey)){
                SuperClassGenMode mode = plugin.getSuperClassGeneratorMode(childClass,parentClazz,dtoModelKey,unwrappingStackElements);
                if(mode!=null){
                    return mode;
                }
            }
            return getDtoType(parentClazz, dtoModelKey.getType(),dtoModelKey.getVersion()).map(DtoGenerateType::superClassGenMode).orElse(DtoGenerateType.DEFAULT_SUPERCLASS_GENMODE);
        }
        else{
            return SuperClassGenMode.IGNORE;
        }
    }


    protected CouchbaseDocumentFieldReflection getFieldReflection(FieldInfo field){
        Preconditions.checkArgument(CouchbaseDocumentStructureReflection.isReflexible((ClassInfo)field.getDeclaringClassInfo()),"The class %s isn't an couchbase element/entity",field.getDeclaringClassInfo().getFullName());
        CouchbaseDocumentStructureReflection couchbaseDocumentStructureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo)field.getDeclaringClassInfo());
        return couchbaseDocumentStructureReflection.getDeclaredFieldByName(field.getName());
    }

    private Optional<DtoFieldGenerateType> getDtoFieldGenerateAnnot(FieldInfo field,DtoInOutMode mode,String type,String version){
        DtoFieldGenerate fieldAnnot = field.getAnnotation(DtoFieldGenerate.class);
        if(fieldAnnot==null){
            return Optional.empty();
        }
        for(DtoFieldGenerateType dtoFieldGenerateType:fieldAnnot.buildForTypes()){
            if(dtoFieldGenerateType.type().equals(type) && (StringUtils.isEmpty(dtoFieldGenerateType.version()) || dtoFieldGenerateType.version().equals(version))){
                return Optional.of(dtoFieldGenerateType);
            }
        }
        return Optional.empty();
    }

    @Override
    protected FieldGenMode getFieldGeneratorMode(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        CouchbaseDocumentFieldReflection fieldReflection = getFieldReflection(field);
        if(fieldReflection!=null){
            for(IDtoModelGeneratorPlugin plugin:getApplicablePlugins(dtoModelKey)){
                FieldGenMode mode = plugin.getFieldGeneratorMode(field,dtoModelKey,unwrappingStackElements);
                if(mode!=null){
                    return mode;
                }
            }

            Optional<DtoFieldGenerateType> fieldGenerateType = getDtoFieldGenerateAnnot(fieldReflection.getField(),dtoModelKey.getInOutMode(),dtoModelKey.getType(),dtoModelKey.getVersion());
            if(fieldGenerateType.isPresent()){
                if(dtoModelKey.getInOutMode()==DtoInOutMode.IN){
                    return fieldGenerateType.get().inputFieldMode();
                }
                else if(dtoModelKey.getInOutMode()==DtoInOutMode.OUT){
                    return fieldGenerateType.get().outputFieldMode();
                }
                else if(dtoModelKey.getInOutMode()==DtoInOutMode.BOTH){
                    Preconditions.checkArgument(
                            fieldGenerateType.get().inputFieldMode()==fieldGenerateType.get().outputFieldMode(),
                            "The mode %s/%s are inconsistent for class %s",
                            fieldGenerateType.get().inputFieldMode(),fieldGenerateType.get().outputFieldMode(),field.getFullName());
                    return fieldGenerateType.get().inputFieldMode();
                }
            }
            else{
                for(UnwrappingStackElement unwrappingStackElement:Lists.reverse(unwrappingStackElements)){
                    if(unwrappingStackElement.getUnwrappedFieldMode()!=FieldGenMode.INHERIT){
                        return unwrappingStackElement.getUnwrappedFieldMode();
                    }
                }
                ClassInfo rootClassInfo = getRootClassInfo(field,unwrappingStackElements);

                for(IDtoModelGeneratorPlugin plugin:getApplicablePlugins(dtoModelKey)){
                    FieldGenMode mode = plugin.getFieldGeneratorModeDefaultFromClass(rootClassInfo,dtoModelKey,unwrappingStackElements);
                    if(mode!=null){
                        return mode;
                    }
                }

                Optional<DtoGenerateType> dtoGenerate =  getDtoType(rootClassInfo,dtoModelKey.getType(),dtoModelKey.getVersion());
                if(dtoGenerate.isPresent()){
                    if(dtoModelKey.getInOutMode()==DtoInOutMode.IN){
                        return dtoGenerate.get().defaultInputFieldMode();
                    }
                    else if(dtoModelKey.getInOutMode()==DtoInOutMode.OUT){
                        return dtoGenerate.get().defaultOutputFieldMode();
                    }
                    else if(dtoModelKey.getInOutMode()==DtoInOutMode.BOTH){
                        Preconditions.checkArgument(dtoGenerate.get().defaultInputFieldMode()==dtoGenerate.get().defaultOutputFieldMode(),"The mode %s/%s are inconsistent for class %s",dtoGenerate.get().defaultInputFieldMode(),dtoGenerate.get().defaultOutputFieldMode(),field.getFullName());
                        return dtoGenerate.get().defaultOutputFieldMode();
                    }
                }
                else{
                    return DtoGenerateType.DEFAULT_FIELD_MODE;
                }
            }
        }
        return FieldGenMode.FILTER;
    }

    @Override
    protected ClassInfo getEffectiveUnwrappedClassInfo(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        CouchbaseDocumentFieldReflection fieldReflection = getFieldReflection(field);
        AbstractClassInfo classInfo = fieldReflection.getEffectiveTypeInfo().getMainType();
        Preconditions.checkArgument(classInfo instanceof ClassInfo,"The effective type of field %s isn't a class",field.getFullName());
        Preconditions.checkArgument(CouchbaseDocumentStructureReflection.isReflexible((ClassInfo)classInfo),"The effective type of field %s isn't a couchbase item",field.getFullName());
        return (ClassInfo)classInfo;
    }

    @Override
    protected ParameterizedTypeInfo getFieldEffectiveType(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        CouchbaseDocumentFieldReflection fieldReflection = getFieldReflection(field);
        return fieldReflection.getEffectiveTypeInfo();
    }

    @Override
    protected FieldGenMode getUnwrappedFieldGenMode(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        for(IDtoModelGeneratorPlugin plugin:getApplicablePlugins(dtoModelKey)){
            FieldGenMode unwrappedFieldMode = plugin.getUnwrappedFieldGenMode(field, dtoModelKey,unwrappingStackElements);
            if(unwrappedFieldMode!=null){
                return unwrappedFieldMode;
            }
        }
        Optional<DtoFieldGenerateType> dtoFieldGenerateAnnot = getDtoFieldGenerateAnnot(field, dtoModelKey.getInOutMode(),dtoModelKey.getType(), dtoModelKey.getVersion());
        return dtoFieldGenerateAnnot.map(DtoFieldGenerateType::unwrapDefaultFieldMode).orElse(super.getUnwrappedFieldGenMode(field, dtoModelKey, unwrappingStackElements));
    }


    @Override
    protected String getFieldEffectiveName(FieldInfo field, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        CouchbaseDocumentFieldReflection fieldReflection = getFieldReflection(field);
        Preconditions.checkNotNull(fieldReflection,"The field %s isn't of a proper type",field.getFullName());
        for(IDtoModelGeneratorPlugin plugin:getApplicablePlugins(key)){
            String name = plugin.getFieldEffectiveName(field,key,unwrappingStackElements);
            if(StringUtils.isNotEmpty(name)){
                return name;
            }
        }
        Optional<DtoFieldGenerateType> fieldGenerateType = getDtoFieldGenerateAnnot(fieldReflection.getField(),key.getInOutMode(),key.getType(),key.getVersion());
        if(fieldGenerateType.isPresent()) {
            if(StringUtils.isNotEmpty(fieldGenerateType.get().name())){
                return fieldGenerateType.get().name();
            }
        }
        return fieldReflection.getField().getName();
    }

    @Override
    protected void addHierarchyBasedTypeInfo(TypeSpec.Builder typeBuilder, ClassInfo clazz, Key key, ClassName superClassDtoName) {
        for(IDtoModelGeneratorPlugin plugin : getApplicablePlugins(key)){
            plugin.addHierarchyBasedTypeInfo(typeBuilder,clazz,key,superClassDtoName);
        }

        if(superClassDtoName!=null && !clazz.isAbstract()) {
            Optional<DtoGenerate> dtoGenerateAnnot = getDtoGenerateAnnotation(clazz).stream()
                    .filter(dtoGenerate -> hasType(dtoGenerate,key.getType(), key.getVersion()))
                    .filter(dtoGenerate -> (key.getInOutMode()==DtoInOutMode.BOTH) || (dtoGenerate.mode()==key.getInOutMode()))
                    .findFirst();
            Optional<DtoGenerateType> dtoGenerateType = getDtoType(clazz, key.getType(), key.getVersion());
            if (dtoGenerateAnnot.isPresent()) {
                //Optional<DtoGenerate> dtoGenerate = getDtoGenerate(clazz, key.getType(), key.getVersion());
                String jsonTypeId=null;
                if(dtoGenerateType.isPresent()) {
                    jsonTypeId =dtoGenerateType.get().jsonTypeId();
                }
                if (StringUtils.isEmpty(jsonTypeId)) {
                    jsonTypeId = dtoGenerateAnnot.get().jsonTypeId();
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
        //Manage root class
        else if(clazz.isAbstract() && superClassDtoName==null){
            typeBuilder.addAnnotation(
                    AnnotationSpec.builder(JsonTypeInfo.class)
                        .addMember("use","$T.$L",JsonTypeInfo.Id.class,JsonTypeInfo.Id.CUSTOM.name())
                        .addMember("include","$T.$L",JsonTypeInfo.As.class,JsonTypeInfo.As.PROPERTY.name())
                        .addMember("property","$S","@t")
                        //.addMember("visible","$L","true")
                    .build()
            );


            typeBuilder.addAnnotation(
                    AnnotationSpec.builder(JsonTypeIdResolver.class)
                            .addMember("value", "$T.class", DtoModelTypeIdResolver.class)
                            .build()
            );
        }
    }

    @Override
    protected void addCommonTypeInfo(TypeSpec.Builder typeBuilder, ClassInfo clazz, Key key,ClassName superClassDtoName) {
        super.addCommonTypeInfo(typeBuilder, clazz, key, superClassDtoName);
        CouchbaseDocumentStructureReflection reflection = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(clazz);
        typeBuilder.addAnnotation(
                AnnotationSpec.builder(DtoModelMappingInfo.class)
                .addMember("entityModelId","$S",reflection.getEntityModelId().toString())
                .addMember("entityClassName","$S",reflection.getClassInfo().getFullName())
                .addMember("mode","$T.$L", ClassName.get(DtoInOutMode.class),key.getInOutMode())
                .addMember("type","$S",StringUtils.isNotEmpty(key.getType())?key.getType():"")
                .addMember("version","$S",key.getVersion())
                .build()
        );
    }

    @Override
    protected void addCommonFieldInfo(String name, FieldSpec.Builder fieldBuilder, ParameterizedTypeInfo effectiveTypeInfo, FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        super.addCommonFieldInfo(name, fieldBuilder, effectiveTypeInfo, fieldInfo, key, unwrappingStackElements);

        StringBuilder sb = new StringBuilder();
        for(UnwrappingStackElement unwrappingStackElement:unwrappingStackElements){
            if(!unwrappingStackElement.isForSuperclass()){
                CouchbaseDocumentFieldReflection fieldReflection = getFieldReflection(unwrappingStackElement.getFieldInfo());
                sb.append(fieldReflection.getName()).append(".");
            }
        }
        sb.append(getFieldReflection(fieldInfo).getName());

        fieldBuilder.addAnnotation(
                AnnotationSpec.builder(DtoFieldMappingInfo.class)
                        .addMember("mappingType","$T.$L", ClassName.get(DtoFieldMappingInfo.MappingRuleType.class), DtoFieldMappingInfo.MappingRuleType.SIMPLE_MAP)
                        .addMember("mode","$T.$L", ClassName.get(DtoInOutMode.class), key.getInOutMode())
                        .addMember("ruleValue","$S",sb.toString())
                        .build()
        );
    }
}
