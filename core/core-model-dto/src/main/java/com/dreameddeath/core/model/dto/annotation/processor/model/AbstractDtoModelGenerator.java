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
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.plugin.IDtoModelGeneratorPlugin;
import com.dreameddeath.core.model.dto.model.manager.DtoModelDef;
import com.dreameddeath.core.model.dto.model.manager.DtoModelManager;
import com.dreameddeath.core.model.entity.EntityDefinitionManager;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.squareup.javapoet.*;
import org.joda.time.LocalDateTime;

import javax.annotation.Generated;
import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by CEAJ8230 on 31/05/2017.
 */
public abstract class AbstractDtoModelGenerator {
    private final ServiceLoader<IDtoModelGeneratorPlugin> dtoModelGeneratorPlugins = ServiceLoader.load(IDtoModelGeneratorPlugin.class,this.getClass().getClassLoader());
    protected final List<IDtoModelGeneratorPlugin> plugins=new ArrayList<>();

    private final Set<Key> generatedModels = new HashSet<>();
    private final List<JavaFile> generatedFiles = new ArrayList<>();
    private final List<DtoModelDef> generatedModelDefs = new ArrayList<>();

    public AbstractDtoModelGenerator(DtoModelManager manager) {
        EntityDefinitionManager entityDefinitionManager = new EntityDefinitionManager();

        for(DtoModelDef modelDef:manager.getModelsDefs()){
            ClassName className = ClassName.bestGuess(modelDef.getClassName());
            generatedModels.add(new Key(className.packageName(),className.simpleName(),modelDef.getMode(),modelDef.getType(),modelDef.getVersion()));
        }
        dtoModelGeneratorPlugins.reload();
        for (IDtoModelGeneratorPlugin dtoModelGeneratorPlugin : dtoModelGeneratorPlugins) {
            plugins.add(dtoModelGeneratorPlugin);
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
        ClassName generatedClassName = ClassName.get(key.getPackageName(),key.getClassName());
        generatedModelDefs.add(new DtoModelDef(generatedClassName.reflectionName(),clazz.getFullName(),key.getInOutMode(),key.getType(),key.getVersion()));
        return generatedClassName;
    }

    protected TypeSpec.Builder generateClass(ClassInfo clazz,Key key) {
        final TypeSpec.Builder dtoModelBuilder = TypeSpec.classBuilder(key.getClassName());
        dtoModelBuilder.addModifiers(Modifier.PUBLIC);
        if(clazz.isAbstract()){
            dtoModelBuilder.addModifiers(Modifier.ABSTRACT);
        }
        ClassName dtoSuperClassName=null;
        if (clazz.getSuperClass() != null) {
            SuperClassGenMode superClassGeneratorMode = getSuperClassGeneratorMode(clazz,clazz.getSuperClass(), key, Collections.emptyList());
            //Manage as superclass
            if (superClassGeneratorMode == SuperClassGenMode.AUTO) {
                ClassName className = generate(clazz.getSuperClass(), key.getInOutMode(),key.getType(), key.getVersion());
                dtoModelBuilder.superclass(className);
                dtoSuperClassName = className;
            }
            //Embed parent fields
            else if (superClassGeneratorMode == SuperClassGenMode.UNWRAP) {
                generateUnwrappedSuperClassFields(clazz, dtoModelBuilder, key, Collections.emptyList());
            }
            else if( superClassGeneratorMode == SuperClassGenMode.IGNORE){
                //Nothing to do
            }
        }
        addCommonTypeInfo(dtoModelBuilder, clazz, key,dtoSuperClassName);
        addPluginTypeInfo(dtoModelBuilder,clazz,key);

        generateFields(clazz, dtoModelBuilder, key, Collections.emptyList());
        return dtoModelBuilder;
    }

    protected void addPluginTypeInfo(TypeSpec.Builder dtoModelBuilder, ClassInfo clazz, Key key) {
        for(IDtoModelGeneratorPlugin plugin:getApplicablePlugins(key)){
            plugin.addTypeInfo(dtoModelBuilder,clazz,key);
        }
    }

    protected TypeSpec.Builder generateEnum(ClassInfo clazz,Key key) {
        TypeSpec.Builder dtoEnumModelBuilder = TypeSpec.enumBuilder(key.getClassName());
        dtoEnumModelBuilder.addModifiers(Modifier.PUBLIC);
        addCommonTypeInfo(dtoEnumModelBuilder, clazz, key,null);

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


    protected Collection<IDtoModelGeneratorPlugin> getApplicablePlugins(Key dtoModelKey) {
        return plugins.stream().filter(plugin->plugin.isApplicableToKey(dtoModelKey)).collect(Collectors.toList());
    }


    protected String getFieldEffectiveName(FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        return fieldInfo.getName();
    }


    protected UnwrappingStackElement buildFieldUnwrappingStackElement(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        FieldGenMode unwrappedGenFieldMode = getUnwrappedFieldGenMode(field,dtoModelKey,unwrappingStackElements);
        return new UnwrappingStackElement(field,unwrappedGenFieldMode);
    }

    protected FieldGenMode getUnwrappedFieldGenMode(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        return FieldGenMode.INHERIT;
    }

    protected UnwrappingStackElement buildSuperClassUnwrappingStackElement(ClassInfo clazz, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        return new UnwrappingStackElement(clazz);
    }


    protected void addCommonTypeInfo(TypeSpec.Builder typeBuilder, ClassInfo clazz, Key key,ClassName superClassDtoName) {
        typeBuilder.addAnnotation(
                AnnotationSpec.builder(Generated.class)
                        .addMember("value", "$S",this.getClass().getName())
                        .addMember("date", "$S", LocalDateTime.now().toString())
                        .addMember("comments","$S",String.format("Generated from %s with key %s", clazz.getFullName(), key.toString()))
                        .build()
        );


        addHierarchyBasedTypeInfo(typeBuilder,clazz,key,superClassDtoName);
    }

    protected abstract void addHierarchyBasedTypeInfo(TypeSpec.Builder typeBuilder, ClassInfo clazz, Key key, ClassName superClassDtoName);


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
                    .addStatement("return this.$L",name);
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
                    .addStatement("return this.$L",name);
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
        addPluginsFieldInfo(name,fieldBuilder,effectiveTypeInfo,fieldInfo,key,unwrappingStackElements);
        addCommonSetterInfo(name,setterBuilder,effectiveTypeInfo,fieldInfo,key,unwrappingStackElements);
        addPluginsSetterInfo(name,setterBuilder,effectiveTypeInfo,fieldInfo,key,unwrappingStackElements);
        addCommonGetterInfo(name,getterBuilder,effectiveTypeInfo,fieldInfo,key,unwrappingStackElements);
        addPluginsGetterInfo(name,getterBuilder,effectiveTypeInfo,fieldInfo,key,unwrappingStackElements);
        builder.addField(fieldBuilder.build());
        builder.addMethod(getterBuilder.build());
        builder.addMethod(setterBuilder.build());
    }

    private void addPluginsSetterInfo(String name, MethodSpec.Builder setterBuilder, ParameterizedTypeInfo effectiveTypeInfo, FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        for(IDtoModelGeneratorPlugin plugin:getApplicablePlugins(key)){
            plugin.addSetterInfo(name,setterBuilder,effectiveTypeInfo,fieldInfo,key,unwrappingStackElements);
        }
    }

    private void addPluginsGetterInfo(String name, MethodSpec.Builder setterBuilder, ParameterizedTypeInfo effectiveTypeInfo, FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        for(IDtoModelGeneratorPlugin plugin:getApplicablePlugins(key)){
            plugin.addGetterInfo(name,setterBuilder,effectiveTypeInfo,fieldInfo,key,unwrappingStackElements);
        }
    }

    private void addPluginsFieldInfo(String name, FieldSpec.Builder fieldBuilder, ParameterizedTypeInfo effectiveTypeInfo, FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        for(IDtoModelGeneratorPlugin plugin:getApplicablePlugins(key)){
            plugin.addFieldInfo(name,fieldBuilder,effectiveTypeInfo,fieldInfo,key,unwrappingStackElements);
        }
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

    public List<DtoModelDef> getGeneratedModelDefs(){
        return generatedModelDefs;
    }

    public Collection<String> getSupportedAnnotationTypes() {
        Set<String> listAnnotations = Sets.newHashSet();
        for(IDtoModelGeneratorPlugin plugin:plugins){
            listAnnotations.addAll(plugin.getSupportedAnnotations());
        }
        return listAnnotations;
    }

    public class UnwrappingStackElement {
        private final boolean isForSuperclass;
        private final ClassInfo childClassInfo;
        private final FieldGenMode unwrappedFieldMode;
        private final FieldInfo fieldInfo;
        private final Map<String,String> tags;

        public UnwrappingStackElement(FieldInfo fieldInfo,FieldGenMode mode) {
            this(fieldInfo,Collections.emptyMap(),mode);
        }

        public UnwrappingStackElement(ClassInfo childOfSuperClass) {
            this(childOfSuperClass,Collections.emptyMap());
        }

        public UnwrappingStackElement(FieldInfo fieldInfo,Map<String,String> tags,FieldGenMode mode) {
            this.isForSuperclass=false;
            this.fieldInfo = fieldInfo;
            this.childClassInfo=null;
            this.unwrappedFieldMode=mode;
            this.tags = Collections.unmodifiableMap(Maps.newHashMap(tags));
        }


        public UnwrappingStackElement(ClassInfo childOfSuperClass,Map<String,String> tags) {
            this.isForSuperclass = true;
            this.fieldInfo = null;
            this.unwrappedFieldMode=FieldGenMode.INHERIT;
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

        public FieldGenMode getUnwrappedFieldMode() {
            return unwrappedFieldMode;
        }
    }
}
