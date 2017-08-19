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

import com.dreameddeath.compile.tools.annotation.processor.reflection.*;
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
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

import static com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode.FILTER;

/**
 * Created by christophe jeunesse on 31/05/2017.
 */
public abstract class AbstractDtoModelGenerator {
    private final ServiceLoader<IDtoModelGeneratorPlugin> dtoModelGeneratorPlugins = ServiceLoader.load(IDtoModelGeneratorPlugin.class, this.getClass().getClassLoader());
    protected final List<IDtoModelGeneratorPlugin> plugins = new ArrayList<>();

    private final Set<Key> generatedModels = new HashSet<>();
    private final List<JavaFile> generatedFiles = new ArrayList<>();
    private final List<DtoModelDef> generatedModelDefs = new ArrayList<>();

    public AbstractDtoModelGenerator(DtoModelManager manager) {
        EntityDefinitionManager entityDefinitionManager = new EntityDefinitionManager();

        for (DtoModelDef modelDef : manager.getModelsDefs()) {
            ClassName className = ClassName.bestGuess(modelDef.getClassName());
            generatedModels.add(new Key(className.packageName(), className.simpleName(), modelDef.getMode(), modelDef.getType(), modelDef.getVersion()));
        }
        dtoModelGeneratorPlugins.reload();
        for (IDtoModelGeneratorPlugin dtoModelGeneratorPlugin : dtoModelGeneratorPlugins) {
            plugins.add(dtoModelGeneratorPlugin);
        }
    }

    public ClassName generate(ClassInfo clazz, DtoInOutMode mode, String type, String version) {
        return generate(clazz, getKey(clazz, mode, type, version));
    }

    protected ClassName generate(ClassInfo clazz, Key key) {
        if (generatedModels.contains(key)) {
            return ClassName.get(key.getPackageName(), key.getClassName());
        } else {
            generatedModels.add(key);
        }

        Preconditions.checkArgument(!clazz.isBaseType(), "Cannot map base type for class ", clazz.getFullName());
        TypeSpec.Builder dtoModelBuilder;

        if (clazz.isEnum()) {
            dtoModelBuilder = generateEnum(clazz, key);
        } else {
            dtoModelBuilder = generateClass(clazz, key);
        }
        JavaFile file = JavaFile.builder(key.getPackageName(), dtoModelBuilder.build()).build();
        generatedFiles.add(file);
        ClassName generatedClassName = ClassName.get(key.getPackageName(), key.getClassName());
        generatedModelDefs.add(new DtoModelDef(generatedClassName.reflectionName(), clazz.getFullName(), key.getInOutMode(), key.getType(), key.getVersion()));
        return generatedClassName;
    }

    protected TypeSpec.Builder generateClass(ClassInfo origClass, Key key) {
        final TypeSpec.Builder dtoModelBuilder = TypeSpec.classBuilder(key.getClassName());
        dtoModelBuilder.addModifiers(Modifier.PUBLIC);
        if (origClass.isAbstract()) {
            dtoModelBuilder.addModifiers(Modifier.ABSTRACT);
        }
        ClassName dtoSuperClassName = null;
        if (origClass.getSuperClass() != null) {
            SuperClassGenMode superClassGeneratorMode = getSuperClassGeneratorMode(origClass, origClass.getSuperClass(), key, Collections.emptyList());
            //Manage as superclass
            if (superClassGeneratorMode == SuperClassGenMode.AUTO) {
                ClassName className = generate(origClass.getSuperClass(), key.getInOutMode(), key.getType(), key.getVersion());
                dtoModelBuilder.superclass(className);
                dtoSuperClassName = className;
            }
            //Embed parent fields
            else if (superClassGeneratorMode == SuperClassGenMode.UNWRAP) {
                generateUnwrappedSuperClassFields(origClass, dtoModelBuilder, key, Collections.emptyList());
            } else if (superClassGeneratorMode == SuperClassGenMode.IGNORE) {
                //Nothing to do
            }
        }
        addCommonTypeInfo(dtoModelBuilder, origClass, key, dtoSuperClassName);
        addPluginTypeInfo(dtoModelBuilder, origClass, key, dtoSuperClassName);

        generateFields(origClass, dtoModelBuilder, key, Collections.emptyList());
        return dtoModelBuilder;
    }

    protected void addPluginTypeInfo(TypeSpec.Builder dtoModelBuilder, ClassInfo clazz, Key key, ClassName dtoSuperClassName) {
        for (IDtoModelGeneratorPlugin plugin : getApplicablePlugins(key)) {
            plugin.addTypeInfo(dtoModelBuilder, clazz, key, dtoSuperClassName);
        }
    }

    protected TypeSpec.Builder generateEnum(ClassInfo clazz, Key key) {
        TypeSpec.Builder dtoEnumModelBuilder = TypeSpec.enumBuilder(key.getClassName());
        dtoEnumModelBuilder.addModifiers(Modifier.PUBLIC);
        addCommonTypeInfo(dtoEnumModelBuilder, clazz, key, null);

        for (FieldInfo fieldInfo : clazz.getDeclaredFields()) {
            dtoEnumModelBuilder.addEnumConstant(fieldInfo.getName());
        }
        return dtoEnumModelBuilder;
    }


    //Used to generate the package name,className, ...
    protected abstract Key getKey(ClassInfo clazz, DtoInOutMode mode, String type, String version);

    protected abstract SuperClassGenMode getSuperClassGeneratorMode(ClassInfo clazz, ClassInfo parentClazz, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements);

    protected abstract FieldGenMode getFieldGeneratorMode(FieldInfo field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements);

    protected abstract FieldGenMode getFieldGeneratorMode(MethodInfo methodInfo, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements);


    protected abstract ClassInfo getEffectiveUnwrappedClassInfo(SourceInfoForField field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements);

    protected abstract ParameterizedTypeInfo getFieldEffectiveType(SourceInfoForField field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements);


    protected Collection<IDtoModelGeneratorPlugin> getApplicablePlugins(Key dtoModelKey) {
        return plugins.stream().filter(plugin -> plugin.isApplicableToKey(dtoModelKey)).collect(Collectors.toList());
    }


    protected String getFieldEffectiveName(FieldInfo fieldInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        return fieldInfo.getName();
    }

    protected String getMethodEffectiveName(MethodInfo methodInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        if(methodInfo.getName().startsWith("get") || methodInfo.getName().startsWith("set")){
            return StringUtils.lowerCaseFirst(methodInfo.getName().substring(3));
        }
        return methodInfo.getName();
    }



    protected UnwrappingStackElement buildFieldUnwrappingStackElement(SourceInfoForField field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        FieldGenMode unwrappedGenFieldMode = getUnwrappedFieldGenMode(field, dtoModelKey, unwrappingStackElements);
        return new UnwrappingStackElement(field, unwrappedGenFieldMode);
    }

    protected FieldGenMode getUnwrappedFieldGenMode(SourceInfoForField field, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        return FieldGenMode.INHERIT;
    }

    protected UnwrappingStackElement buildSuperClassUnwrappingStackElement(ClassInfo clazz, Key dtoModelKey, List<UnwrappingStackElement> unwrappingStackElements) {
        return new UnwrappingStackElement(clazz);
    }


    protected void addCommonTypeInfo(TypeSpec.Builder typeBuilder, ClassInfo origClass, Key key, ClassName superClassDtoName) {
        typeBuilder.addAnnotation(
                AnnotationSpec.builder(Generated.class)
                        .addMember("value", "$S", this.getClass().getName())
                        .addMember("date", "$S", LocalDateTime.now().toString())
                        .addMember("comments", "$S", String.format("Generated from %s with key %s", origClass.getFullName(), key.toString()))
                        .build()
        );


        addHierarchyBasedTypeInfo(typeBuilder, origClass, key, superClassDtoName);
    }

    protected abstract void addHierarchyBasedTypeInfo(TypeSpec.Builder typeBuilder, ClassInfo origClass, Key key, ClassName superClassDtoName);


    private void generateUnwrappedSuperClassFields(ClassInfo clazz, TypeSpec.Builder builder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        List<UnwrappingStackElement> newUnwrappingStackElement = new ArrayList<>(unwrappingStackElements);
        newUnwrappingStackElement.add(buildSuperClassUnwrappingStackElement(clazz, key, unwrappingStackElements));
        generateFields(clazz.getSuperClass(), builder, key, unwrappingStackElements);
    }


    public void generateFields(ClassInfo clazz, TypeSpec.Builder builder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        Map<String,SourceInfoForField.Builder> sourceInfoFieldBuilders=new HashMap<>();
        for (FieldInfo fieldInfo : clazz.getDeclaredFields()) {
            final FieldGenMode mode = getFieldGeneratorMode(fieldInfo, key, unwrappingStackElements);
            Preconditions.checkNotNull(mode, "Not mode returned for %s", fieldInfo.getFullName());

            if(mode!= FILTER){
                String name = getFieldEffectiveName(fieldInfo, key, unwrappingStackElements);
                sourceInfoFieldBuilders.computeIfAbsent(name, newName-> SourceInfoForField.builder(newName,mode))
                    .withField(fieldInfo);
            }
        }

        for (MethodInfo methodInfo : clazz.getDeclaredMethods()) {
            FieldGenMode mode = getFieldGeneratorMode(methodInfo, key, unwrappingStackElements);
            Preconditions.checkNotNull(mode, "Not mode returned for %s", methodInfo.getFullName());
            if(mode!=FILTER){
                Preconditions.checkArgument(methodInfo.getMethodParameters().size()<=1, "Method %s must be a getter or setter", methodInfo);
                String name = getMethodEffectiveName(methodInfo, key, unwrappingStackElements);
                SourceInfoForField.Builder fieldInfoBuilder = sourceInfoFieldBuilders.computeIfAbsent(name, newName -> SourceInfoForField.builder(newName, mode));
                if(methodInfo.getMethodParameters().size()==1) {
                    fieldInfoBuilder.withSetter(methodInfo);
                }
                else{
                    fieldInfoBuilder.withGetter(methodInfo);
                }
            }
        }
        List<SourceInfoForField> fieldsToGenerate= new ArrayList<>();
        for(SourceInfoForField.Builder sourceInfoBuilder:sourceInfoFieldBuilders.values()) {
            ParameterizedTypeInfo effectiveTypeInfo = getFieldEffectiveType(sourceInfoBuilder.create(), key, unwrappingStackElements);
            sourceInfoBuilder.withEffectiveType(effectiveTypeInfo);
            fieldsToGenerate.add(sourceInfoBuilder.create());
        }

        for (SourceInfoForField fieldInfo: fieldsToGenerate) {
            switch (fieldInfo.mode){
                case SIMPLE:
                    generateSimpleField(fieldInfo,builder,key,unwrappingStackElements);
                    break;
                case UNWRAP:
                    generateUnwrappedFields(fieldInfo,builder,key,unwrappingStackElements);
                    break;
            }
        }
    }

    private ClassName getMainFieldEffectiveClassName(SourceInfoForField fieldInfo, AbstractClassInfo classInfo, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        if (classInfo.isBaseType()) {
            return classInfo.getClassName();
        } else {
            Preconditions.checkArgument(classInfo instanceof ClassInfo, "The classInfo %s cannot be converted", classInfo.getFullName());
            return generate((ClassInfo) classInfo, getKey((ClassInfo) classInfo, key.getInOutMode(), key.getType(), key.getVersion()));
        }
    }

    private void generateSimpleField(SourceInfoForField fieldInfo, TypeSpec.Builder builder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        FieldSpec.Builder fieldBuilder;
        MethodSpec.Builder setterBuilder = MethodSpec.methodBuilder("set" + StringUtils.capitalizeFirst(fieldInfo.name))
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID);
        MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder("get" + StringUtils.capitalizeFirst(fieldInfo.name))
                .addModifiers(Modifier.PUBLIC);

        if (fieldInfo.effectiveType.isAssignableTo(Collection.class)) {
            ClassName effectiveCollectionContentClassName = getMainFieldEffectiveClassName(fieldInfo, fieldInfo.effectiveType.getMainTypeGeneric(0).getMainType(), key, unwrappingStackElements);
            TypeName type = ParameterizedTypeName.get(ClassName.get(List.class), effectiveCollectionContentClassName);
            fieldBuilder = FieldSpec.builder(type, fieldInfo.name)
                    .initializer("new $T<>()", ArrayList.class)
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL);
            getterBuilder.returns(type)
                    .addStatement("return this.$L", fieldInfo.name);
            setterBuilder.addParameter(type, "newList")
                    .addStatement("this.$L.clear()", fieldInfo.name)
                    .addStatement("this.$L.addAll(newList)", fieldInfo.name);
        } else if (fieldInfo.effectiveType.isAssignableTo(Map.class)) {
            AbstractClassInfo keyClassInfo = fieldInfo.effectiveType.getMainTypeGeneric(0).getMainType();
            Preconditions.checkArgument(keyClassInfo.isBaseType(), "The mapping of the map of field %s with key type %s is not possible", fieldInfo.getFullName(), keyClassInfo.getFullName());
            ClassName effectiveMapContentClassName = getMainFieldEffectiveClassName(fieldInfo, fieldInfo.effectiveType.getMainTypeGeneric(1).getMainType(), key, unwrappingStackElements);
            TypeName type = ParameterizedTypeName.get(ClassName.get(Map.class), keyClassInfo.getClassName(), effectiveMapContentClassName);
            fieldBuilder = FieldSpec.builder(
                    type, fieldInfo.name)
                    .initializer("new $T<>()", HashMap.class)
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL);

            getterBuilder.returns(type)
                    .addStatement("return this.$L", fieldInfo.name);
            setterBuilder.addParameter(type, "newMap")
                    .addStatement("this.$L.clear()", fieldInfo.name)
                    .addStatement("this.$L.putAll(newMap)", fieldInfo.name);
        } else {
            ClassName effectiveClassName = getMainFieldEffectiveClassName(fieldInfo, fieldInfo.effectiveType.getMainType(), key, unwrappingStackElements);
            fieldBuilder = FieldSpec.builder(effectiveClassName, fieldInfo.name)
                    .initializer("null")
                    .addModifiers(Modifier.PRIVATE);
            getterBuilder.returns(effectiveClassName)
                    .addStatement("return this.$L", fieldInfo.name);
            setterBuilder.addParameter(effectiveClassName, "newValue")
                    .addStatement("this.$L = newValue", fieldInfo.name);
        }

        addCommonFieldInfo(fieldInfo, fieldBuilder, key, unwrappingStackElements);
        addPluginsFieldInfo(fieldInfo, fieldBuilder, key, unwrappingStackElements);
        addCommonSetterInfo(fieldInfo, setterBuilder, key, unwrappingStackElements);
        addPluginsSetterInfo(fieldInfo, setterBuilder, key, unwrappingStackElements);
        addCommonGetterInfo(fieldInfo, getterBuilder, key, unwrappingStackElements);
        addPluginsGetterInfo(fieldInfo, getterBuilder, key, unwrappingStackElements);
        builder.addField(fieldBuilder.build());
        builder.addMethod(getterBuilder.build());
        builder.addMethod(setterBuilder.build());
    }

    private void addPluginsSetterInfo(SourceInfoForField fieldInfo, MethodSpec.Builder setterBuilder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        for (IDtoModelGeneratorPlugin plugin : getApplicablePlugins(key)) {
            plugin.addSetterInfo(fieldInfo, setterBuilder, key, unwrappingStackElements);
        }
    }

    private void addPluginsGetterInfo(SourceInfoForField fieldInfo, MethodSpec.Builder setterBuilder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        for (IDtoModelGeneratorPlugin plugin : getApplicablePlugins(key)) {
            plugin.addGetterInfo(fieldInfo, setterBuilder, key, unwrappingStackElements);
        }
    }

    private void addPluginsFieldInfo(SourceInfoForField fieldInfo, FieldSpec.Builder fieldBuilder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        for (IDtoModelGeneratorPlugin plugin : getApplicablePlugins(key)) {
            plugin.addFieldInfo(fieldInfo, fieldBuilder, key, unwrappingStackElements);
        }
    }

    private void addCommonGetterInfo(SourceInfoForField fieldInfo, MethodSpec.Builder getterBuilder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        getterBuilder.addAnnotation(
                AnnotationSpec.builder(JsonGetter.class)
                        .addMember("value", "$S", fieldInfo.name).build()
        );
    }

    private void addCommonSetterInfo(SourceInfoForField fieldInfo, MethodSpec.Builder setterBuilder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        setterBuilder.addAnnotation(
                AnnotationSpec.builder(JsonSetter.class)
                        .addMember("value", "$S", fieldInfo.name).build()
        );
    }

    protected void addCommonFieldInfo(SourceInfoForField fieldInfo, FieldSpec.Builder fieldBuilder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        fieldBuilder.addAnnotation(
                AnnotationSpec.builder(JsonProperty.class)
                        .addMember("value", "$S", fieldInfo.name).build()
        );
    }

    protected void generateUnwrappedFields(SourceInfoForField fieldInfo, TypeSpec.Builder builder, Key key, List<UnwrappingStackElement> unwrappingStackElements) {
        List<UnwrappingStackElement> newStackElements = new ArrayList<>(unwrappingStackElements.size() + 1);
        newStackElements.addAll(unwrappingStackElements);
        newStackElements.add(buildFieldUnwrappingStackElement(fieldInfo, key, unwrappingStackElements));
        ClassInfo effectiveUnwrappedClassInfo = getEffectiveUnwrappedClassInfo(fieldInfo, key, unwrappingStackElements);
        Preconditions.checkArgument(!effectiveUnwrappedClassInfo.isBaseType(), "Cannot unwrap base type %s", effectiveUnwrappedClassInfo.getFullName());
        generateFields(effectiveUnwrappedClassInfo, builder, key, newStackElements);
    }

    public abstract void generateIfNeeded(ClassInfo entityClassInfo);

    public List<JavaFile> getJavaFiles() {
        return generatedFiles;
    }

    public List<DtoModelDef> getGeneratedModelDefs() {
        return generatedModelDefs;
    }

    public Collection<String> getSupportedAnnotationTypes() {
        Set<String> listAnnotations = Sets.newHashSet();
        for (IDtoModelGeneratorPlugin plugin : plugins) {
            listAnnotations.addAll(plugin.getSupportedAnnotations());
        }
        return listAnnotations;
    }

    public class UnwrappingStackElement {
        private final boolean isForSuperclass;
        private final ClassInfo childClassInfo;
        private final FieldGenMode unwrappedFieldMode;
        private final SourceInfoForField fieldInfo;
        private final Map<String, String> tags;

        public UnwrappingStackElement(SourceInfoForField fieldInfo, FieldGenMode mode) {
            this(fieldInfo, Collections.emptyMap(), mode);
        }

        public UnwrappingStackElement(SourceInfoForField fieldInfo, Map<String, String> tags, FieldGenMode mode) {
            this.isForSuperclass = false;
            this.fieldInfo = fieldInfo;
            this.childClassInfo = null;
            this.unwrappedFieldMode = mode;
            this.tags = Collections.unmodifiableMap(Maps.newHashMap(tags));
        }

        public UnwrappingStackElement(ClassInfo childOfSuperClass) {
            this(childOfSuperClass, Collections.emptyMap());
        }

        public UnwrappingStackElement(ClassInfo childOfSuperClass, Map<String, String> tags) {
            this.isForSuperclass = true;
            this.fieldInfo = null;
            this.unwrappedFieldMode = FieldGenMode.INHERIT;
            this.childClassInfo = childOfSuperClass;
            this.tags = Collections.unmodifiableMap(Maps.newHashMap(tags));
        }

        public SourceInfoForField getFieldInfo() {
            return fieldInfo;
        }

        public String getTag(String tag) {
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


    public static class SourceInfoForField {
        private final String name;
        private final FieldGenMode mode;
        private final MethodInfo getter;
        private final MethodInfo setter;
        private final ParameterizedTypeInfo effectiveType;
        private final FieldInfo fieldInfo;
        private final Map<String, String> tags = new HashMap<>();
        private final ClassInfo declaringClass;

        private SourceInfoForField(Builder builder) {
            this.name = builder.name;
            this.mode = builder.mode;
            this.getter = builder.getter;
            this.setter = builder.setter;
            this.fieldInfo = builder.field;
            this.effectiveType = builder.effectiveType;
            Preconditions.checkArgument(fieldInfo!=null || (getter!=null && setter!=null),"Either field or getter /setter must be given for source field %s",getFullName());
            if(fieldInfo!=null){
                this.declaringClass=fieldInfo.getDeclaringClassInfo();
            }
            else{
                Preconditions.checkArgument(getter.getDeclaringClassInfo() == setter.getDeclaringClassInfo(),"Setter and getter must have the same declaring class for source field %s",getFullName());
                Preconditions.checkArgument(getter.getDeclaringClassInfo() instanceof ClassInfo,"Setter and getter must have a concrete class for field %s",getFullName());
                this.declaringClass=(ClassInfo)getter.getDeclaringClassInfo();
            }
        }

        public String getName() {
            return name;
        }

        public MethodInfo getGetter() {
            return getter;
        }

        public MethodInfo getSetter() {
            return setter;
        }

        public ClassInfo getDeclaringClassInfo(){
            return declaringClass;
        }

        public <T extends Annotation> List<T> getAnnotations(Class<T> tClass){
            List<T> annot=new ArrayList<>();
            if(fieldInfo!=null){
                T[] annots = fieldInfo.getAnnotationByType(tClass);
                if(annots!=null) {
                    annot.addAll(Arrays.asList(annots));
                }
            }
            if(getter!=null){
                T[] annots = getter.getAnnotationByType(tClass);
                if(annots!=null) {
                    annot.addAll(Arrays.asList(annots));
                }
            }
            if(setter!=null){
                T[] annots = setter.getAnnotationByType(tClass);
                if(annots!=null) {
                    annot.addAll(Arrays.asList(annots));
                }
            }
            return annot;
        }

        public <T extends Annotation> T getFirstAnnotation(Class<T> tClass){
            T annot=null;
            if(fieldInfo!=null){
                annot=fieldInfo.getAnnotation(tClass);
            }
            if(getter!=null){
                annot=getter.getAnnotation(tClass);
            }
            if(setter!=null){
                annot=setter.getAnnotation(tClass);
            }

            return annot;
        }


        public Map<String, String> getTags() {
            return tags;
        }

        public static Builder builder(String name,FieldGenMode mode){
            return new Builder(name,mode);
        }

        public String getFullName() {
            return name+
                    ((fieldInfo!=null)?"/f:<"+fieldInfo.getFullName()+">":"")+
                    ((getter!=null)?"/g:<"+getter.getFullName()+">":"")+
                    ((setter!=null)?"/s:<"+setter.getFullName()+">":"")
                    ;
        }

        public ParameterizedTypeInfo getEffectiveTypeInfo() {
            return effectiveType;
        }

        public boolean hasSourceField() {
            return fieldInfo!=null;
        }

        public FieldInfo getField() {
            return fieldInfo;
        }

        public static class Builder{
            private final FieldGenMode mode;
            private final String name;
            private FieldInfo field;
            private MethodInfo setter;
            private MethodInfo getter;
            private ParameterizedTypeInfo effectiveType;

            public Builder(String name,FieldGenMode mode) {
                this.mode = mode;
                this.name = name;
            }

            public Builder withField(FieldInfo field) {
                Preconditions.checkState(this.field==null,"Cannot set twice the field twice for field %s (existing %s, new %s) ",name,this.field,field.getFullName());
                this.field = field;
                return this;
            }

            public Builder withSetter(MethodInfo setter) {
                Preconditions.checkState(this.setter==null,"Cannot set twice the setter twice for field %s (existing %s, new %s) ",name,this.setter,setter.getFullName());
                this.setter = setter;
                return this;
            }

            public Builder withGetter(MethodInfo getter) {
                Preconditions.checkState(this.getter==null,"Cannot set twice the getter twice for field %s (existing %s, new %s) ",name,this.getter,getter.getFullName());
                this.getter = getter;
                return this;
            }

            public Builder withEffectiveType(ParameterizedTypeInfo effectiveType) {
                this.effectiveType = effectiveType;
                return this;
            }

            public SourceInfoForField create() {
                return new SourceInfoForField(this);
            }
        }
    }
}