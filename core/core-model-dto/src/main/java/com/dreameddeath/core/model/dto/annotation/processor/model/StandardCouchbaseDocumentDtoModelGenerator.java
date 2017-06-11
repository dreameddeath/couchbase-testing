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
import com.dreameddeath.core.model.dto.annotation.DtoModelMappingInfo;
import com.dreameddeath.core.model.dto.model.manager.DtoModelManager;
import com.dreameddeath.core.model.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
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
        if(generateList.size()>0){
            for(DtoGenerate generate:generateList){
                if(generate.buildForTypes().length>0){
                    for(DtoGenerateType type :generate.buildForTypes()){
                        if(generate.mode()!=DtoInOutMode.NONE) {
                            generate(entityClassInfo, generate.mode(), type.name(), type.version());
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
            if(dtoTypeGenerate.name().equals(type) && (StringUtils.isEmpty(dtoTypeGenerate.version()) || dtoTypeGenerate.version().equals(version))){
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
                return (ClassInfo)unwrappingStackElementList.get(0).getFieldInfo().getDeclaringClassInfo();
            }
        }
        else{
            return (ClassInfo)field.getDeclaringClassInfo();
        }
    }

    protected Optional<DtoGenerateType> getDtoType(ClassInfo classInfo, String type, String version){
        Optional<DtoGenerate> annot = getDtoGenerateAnnotation(classInfo).stream()
                .filter(dtoGenerate -> hasType(dtoGenerate,type,version))
                .findFirst();
        return annot.flatMap(dtoGenerate -> getDtoType(dtoGenerate,type,version));
    }


    @Override
    protected Key getKey(ClassInfo clazz, DtoInOutMode mode, String type, String version) {
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

    protected Optional<DtoFieldGenerateType> getDtoFieldGenerateAnnot(FieldInfo field,String type,String version){
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
            Optional<DtoFieldGenerateType> fieldGenerateType = getDtoFieldGenerateAnnot(fieldReflection.getField(),dtoModelKey.getType(),dtoModelKey.getVersion());
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
        Optional<DtoFieldGenerateType> dtoFieldGenerateAnnot = getDtoFieldGenerateAnnot(field, dtoModelKey.getType(), dtoModelKey.getVersion());
        return dtoFieldGenerateAnnot.map(DtoFieldGenerateType::unwrapDefaultFieldMode).orElse(super.getUnwrappedFieldGenMode(field, dtoModelKey, unwrappingStackElements));
    }


    @Override
    protected String getFieldEffectiveName(FieldInfo field, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        CouchbaseDocumentFieldReflection fieldReflection = getFieldReflection(field);
        Preconditions.checkNotNull(fieldReflection,"The field %s isn't of a proper type",field.getFullName());
        Optional<DtoFieldGenerateType> fieldGenerateType = getDtoFieldGenerateAnnot(fieldReflection.getField(),key.getType(),key.getVersion());
        if(fieldGenerateType.isPresent()) {
            if(StringUtils.isNotEmpty(fieldGenerateType.get().name())){
                return fieldGenerateType.get().name();
            }
        }
        return fieldReflection.getField().getName();
    }

    @Override
    protected void addCommonTypeInfo(TypeSpec.Builder typeBuilder, ClassInfo clazz, Key key) {
        CouchbaseDocumentStructureReflection reflection = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(clazz);
        super.addCommonTypeInfo(typeBuilder, clazz, key);
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
