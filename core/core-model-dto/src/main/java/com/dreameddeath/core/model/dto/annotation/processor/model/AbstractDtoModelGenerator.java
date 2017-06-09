package com.dreameddeath.core.model.dto.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ParameterizedTypeInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.model.manager.DtoModelDef;
import com.dreameddeath.core.model.dto.model.manager.DtoModelManager;
import com.dreameddeath.core.model.entity.EntityDefinitionManager;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.squareup.javapoet.*;
import org.joda.time.DateTime;

import javax.annotation.Generated;
import javax.lang.model.element.Modifier;
import java.util.*;

/**
 * Created by CEAJ8230 on 31/05/2017.
 */
public abstract class AbstractDtoModelGenerator {
    private final Set<Key> generatedModels = new HashSet<>();
    private final List<JavaFile> generatedFiles = new ArrayList<>();
    private final List<DtoModelDef> generatedDtoModelDef=new ArrayList<>();


    public AbstractDtoModelGenerator(DtoModelManager manager) {
        EntityDefinitionManager entityDefinitionManager = new EntityDefinitionManager();

        for(DtoModelDef modelDef:manager.getModelsDefs()){
            Optional<EntityDef> first = entityDefinitionManager.getEntities().stream().filter(entityDef -> entityDef.getModelId().equals(modelDef.getEntityModelId())).findFirst();
            ClassName className = ClassName.bestGuess(first.get().getClassName());
            generatedModels.add(new Key(className.packageName(),className.simpleName(),modelDef.getMode(),modelDef.getType(),modelDef.getVersion()));
        }
    }

    public ClassName generate(ClassInfo clazz, DtoInOutMode mode, String type, String version) {
        return generate(clazz,getKey(clazz, mode, type, version));
    }

    protected ClassName generate(ClassInfo clazz, Key key) {
        if (generatedModels.contains(key)) {
            return ClassName.get(key.getPackageName(),key.getClassName());
        } else {
            generatedModels.add(key);
        }

        Preconditions.checkArgument(!clazz.isBaseType(),"Cannot map base type for class ",clazz.getFullName());
        TypeSpec.Builder dtoModelBuilder;

        if(clazz.isEnum()){
            dtoModelBuilder = generateEnum(clazz,key);
        }
        else{
            dtoModelBuilder = generateClass(clazz,key);
        }
        JavaFile file = JavaFile.builder(key.getPackageName(), dtoModelBuilder.build()).build();
        generatedFiles.add(file);
        generatedDtoModelDef.add(new DtoModelDef(clazz.getFullName(),null,key.getInOutMode(),key.getType(),key.getVersion()));
        return ClassName.get(key.getPackageName(),key.getClassName());
    }

    protected TypeSpec.Builder generateClass(ClassInfo clazz,Key key) {
        final TypeSpec.Builder dtoModelBuilder = TypeSpec.classBuilder(key.getClassName());
        dtoModelBuilder.addModifiers(Modifier.PUBLIC);
        if(clazz.isAbstract()){
            dtoModelBuilder.addModifiers(Modifier.ABSTRACT);
        }
        addCommonTypeInfo(dtoModelBuilder, clazz, key);

        if (clazz.getSuperClass() != null) {
            SuperClassGenMode superClassGeneratorMode = getSuperClassGeneratorMode(clazz,clazz.getSuperClass(), key, Collections.emptyList());
            //Manage as superclass
            if (superClassGeneratorMode == SuperClassGenMode.AUTO) {
                ClassName className = generate(clazz.getSuperClass(), key.getInOutMode(),key.getType(), key.getVersion());
                dtoModelBuilder.superclass(className);
            }
            //Embed parent fields
            else if (superClassGeneratorMode == SuperClassGenMode.UNWRAP) {
                generateUnwrappedSuperClassFields(clazz, dtoModelBuilder, key, Collections.emptyList());
            }
            else if( superClassGeneratorMode == SuperClassGenMode.IGNORE){
                //Nothing to do
            }
        }
        generateFields(clazz, dtoModelBuilder, key, Collections.emptyList());
        return dtoModelBuilder;
    }

    protected TypeSpec.Builder generateEnum(ClassInfo clazz,Key key) {
        TypeSpec.Builder dtoEnumModelBuilder = TypeSpec.enumBuilder(key.getClassName());
        dtoEnumModelBuilder.addModifiers(Modifier.PUBLIC);
        addCommonTypeInfo(dtoEnumModelBuilder, clazz, key);

        for(FieldInfo fieldInfo:clazz.getDeclaredFields()){
            dtoEnumModelBuilder.addEnumConstant(fieldInfo.getName());
        }
        return dtoEnumModelBuilder;
    }


    //Used to generate the package name,className, ...
    protected abstract Key getKey(ClassInfo clazz, DtoInOutMode mode, String type, String version);

    protected abstract SuperClassGenMode getSuperClassGeneratorMode(ClassInfo clazz,ClassInfo parentClazz, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements);

    protected abstract FieldGenMode getFieldGeneratorMode(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements);

    protected abstract ClassInfo getEffectiveUnwrappedClassInfo(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements);

    protected abstract ParameterizedTypeInfo getFieldEffectiveType(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements);


    protected String getFieldEffectiveName(FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        return fieldInfo.getName();
    }


    protected UnwrappingStackElement buildFieldUnwrappingStackElement(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        return new UnwrappingStackElement(field);
    }

    protected UnwrappingStackElement buildSuperClassUnwrappingStackElement(ClassInfo clazz, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        return new UnwrappingStackElement(clazz);
    }


    protected void addCommonTypeInfo(TypeSpec.Builder typeBuilder, ClassInfo clazz, Key key) {
        typeBuilder.addAnnotation(
                AnnotationSpec.builder(Generated.class)
                        .addMember("value", "$S",this.getClass().getName())
                        .addMember("date", "$S",DateTime.now().toString())
                        .addMember("comments","$S",String.format("Generated from %s with key %s", clazz.getFullName(), key.toString()))
                        .build()
        );
    }


    private void generateUnwrappedSuperClassFields(ClassInfo clazz,TypeSpec.Builder builder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        List<UnwrappingStackElement> newUnwrappingStackElement = new ArrayList<>(unwrappingStackElements);
        newUnwrappingStackElement.add(buildSuperClassUnwrappingStackElement(clazz,key,unwrappingStackElements));
        generateFields(clazz.getSuperClass(),builder,key,unwrappingStackElements);
    }


    public void generateFields(ClassInfo clazz,TypeSpec.Builder builder,Key key, List<UnwrappingStackElement> unwrappingStackElements){
        for(FieldInfo fieldInfo:clazz.getDeclaredFields()){
            FieldGenMode mode =getFieldGeneratorMode(fieldInfo,key,unwrappingStackElements);
            Preconditions.checkNotNull(mode,"Not mode returned for %s",fieldInfo.getName());
            switch (mode){
                case FILTER:
                    break;
                case SIMPLE:
                    generateSimpleField(fieldInfo,builder,key,unwrappingStackElements);
                    break;
                case UNWRAP:
                    generateUnwrappedFields(fieldInfo,builder,key,unwrappingStackElements);
                    break;
            }
        }
    }

    private ClassName getMainFieldEffectiveClassName(FieldInfo fieldInfo, AbstractClassInfo classInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements){
        if(classInfo.isBaseType()){
            return classInfo.getClassName();
        }
        else {
            Preconditions.checkArgument(classInfo instanceof ClassInfo,"The classInfo %s cannot be converted",classInfo.getFullName());
            return generate((ClassInfo)classInfo,getKey((ClassInfo)classInfo,key.getInOutMode(),key.getType(),key.getVersion()));
        }
    }

    private void generateSimpleField(FieldInfo fieldInfo, TypeSpec.Builder builder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        String name=getFieldEffectiveName(fieldInfo,key,unwrappingStackElements);
        ParameterizedTypeInfo effectiveTypeInfo= getFieldEffectiveType(fieldInfo,key,unwrappingStackElements);

        FieldSpec.Builder fieldBuilder;
        MethodSpec.Builder setterBuilder=MethodSpec.methodBuilder("set"+ StringUtils.capitalizeFirst(name))
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID);
        MethodSpec.Builder getterBuilder=MethodSpec.methodBuilder("get"+StringUtils.capitalizeFirst(name))
                .addModifiers(Modifier.PUBLIC);

        if(effectiveTypeInfo.isAssignableTo(Collection.class)){
            ClassName effectiveCollectionContentClassName = getMainFieldEffectiveClassName(fieldInfo,effectiveTypeInfo.getMainTypeGeneric(0).getMainType(),key,unwrappingStackElements);
            TypeName type = ParameterizedTypeName.get(ClassName.get(List.class),effectiveCollectionContentClassName);
            fieldBuilder = FieldSpec.builder(type,name)
                     .initializer("new $T<>()",ArrayList.class)
                    .addModifiers(Modifier.PRIVATE,Modifier.FINAL);
            getterBuilder.returns(type)
                    .addStatement("return $T.unmodifiableList(this.$L)",ClassName.get(Collections.class),name);
            setterBuilder.addParameter(type,"newList")
                    .addStatement("this.$L.clear()",name)
                    .addStatement("this.$L.addAll(newList)",name);
        }
        else if(effectiveTypeInfo.isAssignableTo(Map.class)){
            AbstractClassInfo keyClassInfo = effectiveTypeInfo.getMainTypeGeneric(0).getMainType();
            Preconditions.checkArgument(keyClassInfo.isBaseType(),"The mapping of the map of field %s with key type %s is not possible",fieldInfo.getFullName(),keyClassInfo.getFullName());
            ClassName effectiveMapContentClassName = getMainFieldEffectiveClassName(fieldInfo,effectiveTypeInfo.getMainTypeGeneric(1).getMainType(),key,unwrappingStackElements);
            TypeName type = ParameterizedTypeName.get(ClassName.get(Map.class),keyClassInfo.getClassName(),effectiveMapContentClassName);
            fieldBuilder = FieldSpec.builder(
                    type, name)
                    .initializer("new $T<>()",HashMap.class)
                    .addModifiers(Modifier.PRIVATE,Modifier.FINAL);

            getterBuilder.returns(type)
                    .addStatement("return $T.unmodifiableMap(this.$L)",ClassName.get(Collections.class),name);
            setterBuilder.addParameter(type,"newMap")
                    .addStatement("this.$L.clear()",name)
                    .addStatement("this.$L.putAll(newMap)",name);
        }
        else{
            ClassName effectiveClassName = getMainFieldEffectiveClassName(fieldInfo,effectiveTypeInfo.getMainType(),key,unwrappingStackElements);
            fieldBuilder = FieldSpec.builder(effectiveClassName,name)
                    .initializer("null")
                    .addModifiers(Modifier.PRIVATE);
            getterBuilder.returns(effectiveClassName)
                    .addStatement("return this.$L",name);
            setterBuilder.addParameter(effectiveClassName,"newValue")
                    .addStatement("this.$L = newValue",name);
        }

        addCommonFieldInfo(name,fieldBuilder,effectiveTypeInfo,fieldInfo,key,unwrappingStackElements);
        addCommonSetterInfo(name,setterBuilder,effectiveTypeInfo,fieldInfo,key,unwrappingStackElements);
        addCommonGetterInfo(name,getterBuilder,effectiveTypeInfo,fieldInfo,key,unwrappingStackElements);

        builder.addField(fieldBuilder.build());
        builder.addMethod(getterBuilder.build());
        builder.addMethod(setterBuilder.build());
    }

    private void addCommonGetterInfo(String name, MethodSpec.Builder getterBuilder, ParameterizedTypeInfo effectiveTypeInfo, FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        getterBuilder.addAnnotation(
                AnnotationSpec.builder(JsonGetter.class)
                        .addMember("value","$S",name).build()
        );
    }

    private void addCommonSetterInfo(String name, MethodSpec.Builder setterBuilder, ParameterizedTypeInfo effectiveTypeInfo, FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        setterBuilder.addAnnotation(
                AnnotationSpec.builder(JsonSetter.class)
                        .addMember("value","$S",name).build()
        );
    }

    protected void addCommonFieldInfo(String name,FieldSpec.Builder fieldBuilder, ParameterizedTypeInfo effectiveTypeInfo, FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements){
        fieldBuilder.addAnnotation(
                AnnotationSpec.builder(JsonProperty.class)
                                .addMember("value","$S",name).build()
        );
    }

    protected void generateUnwrappedFields(FieldInfo fieldInfo, TypeSpec.Builder builder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        List<UnwrappingStackElement> newStackElements = new ArrayList<>(unwrappingStackElements.size()+1);
        newStackElements.addAll(unwrappingStackElements);
        newStackElements.add(buildFieldUnwrappingStackElement(fieldInfo,key,unwrappingStackElements));
        ClassInfo effectiveUnwrappedClassInfo = getEffectiveUnwrappedClassInfo(fieldInfo, key, unwrappingStackElements);
        Preconditions.checkArgument(!effectiveUnwrappedClassInfo.isBaseType(),"Cannot unwrap base type %s",effectiveUnwrappedClassInfo.getFullName());
        generateFields(effectiveUnwrappedClassInfo,builder,key,newStackElements);
    }

    public abstract void generateIfNeeded(ClassInfo entityClassInfo);

    public List<JavaFile> getJavaFiles() {
        return generatedFiles;
    }

    public enum FieldGenMode{
        SIMPLE,
        UNWRAP,
        FILTER;
    }

    public enum SuperClassGenMode{
        AUTO,
        UNWRAP,
        IGNORE;
    }


    protected static class Key{
        private final String type;
        private final String packageName;
        private final String className;
        private final String version;
        private final DtoInOutMode inOutMode;

        public Key(String packageName, String className, DtoInOutMode inOutMode,String type,String version) {
            this.version = version;
            this.type = type;
            this.packageName = packageName;
            this.className = className;
            this.inOutMode = inOutMode;
        }

        public String getVersion() {
            return version;
        }

        public String getType() {
            return type;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getClassName() {
            return className;
        }

        public DtoInOutMode getInOutMode() {
            return inOutMode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (!version.equals(key.version)) return false;
            if (type != null ? !type.equals(key.type) : key.type != null) return false;
            if (!packageName.equals(key.packageName)) return false;
            if (!className.equals(key.className)) return false;
            return inOutMode == key.inOutMode;
        }

        @Override
        public int hashCode() {
            int result = version.hashCode();
            result = 31 * result + (type != null ? type.hashCode() : 0);
            result = 31 * result + packageName.hashCode();
            result = 31 * result + className.hashCode();
            result = 31 * result + inOutMode.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "type='" + type + '\'' +
                    ", packageName='" + packageName + '\'' +
                    ", className='" + className + '\'' +
                    ", version='" + version + '\'' +
                    ", inOutMode=" + inOutMode +
                    '}';
        }
    }

    protected class UnwrappingStackElement {
        private final boolean isForSuperclass;
        private final ClassInfo childClassInfo;
        private final FieldInfo fieldInfo;
        private final Map<String,String> tags;

        public UnwrappingStackElement(FieldInfo fieldInfo) {
            this(fieldInfo,Collections.emptyMap());
        }

        public UnwrappingStackElement(ClassInfo childOfSuperClass) {
            this(childOfSuperClass,Collections.emptyMap());
        }


        public UnwrappingStackElement(FieldInfo fieldInfo,Map<String,String> tags) {
            this.isForSuperclass=false;
            this.fieldInfo = fieldInfo;
            this.childClassInfo=null;
            this.tags = Collections.unmodifiableMap(Maps.newHashMap(tags));
        }


        public UnwrappingStackElement(ClassInfo childOfSuperClass,Map<String,String> tags) {
            this.isForSuperclass = true;
            this.fieldInfo = null;
            this.childClassInfo = childOfSuperClass;
            this.tags = Collections.unmodifiableMap(Maps.newHashMap(tags));
        }

        public FieldInfo getFieldInfo() {
            return fieldInfo;
        }

        public String getTag(String tag){
            return tags.get(tag);
        }

        public boolean isForSuperclass() {
            return isForSuperclass;
        }

        public ClassInfo getChildClass() {
            return childClassInfo;
        }
    }
}
