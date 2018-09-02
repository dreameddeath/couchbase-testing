package com.dreameddeath.compiling.datamodel.gen.generators;

import com.dreameddeath.compiling.datamodel.gen.model.FieldModelDef;
import com.dreameddeath.compiling.datamodel.gen.model.ModelDef;
import com.dreameddeath.core.java.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class ImplementationGenerator {
    public static final String ORIG_DATA_FIELD_NAME = "__orig";
    public static final String BUILDER_PARAM_NAME = "__builder";
    public static final String ORIG_ITEM_PARAM_NAME = "__origItem";

    TypeHelper typeHelper = new TypeHelper();

    public TypeSpec generate(ModelDef model) {
        ClassName coreClassName = typeHelper.getCoreClassName(model);
        ClassName effectiveClassName = typeHelper.getEffectiveClassName(coreClassName, TypeHelper.SubType.IMPL);
        ClassName effectiveBuilderClassName = typeHelper.getEffectiveClassName(coreClassName, TypeHelper.SubType.IMPL_BUILDER);

        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(effectiveClassName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(typeHelper.getEffectiveClassName(coreClassName, TypeHelper.SubType.INTERFACE))
                .addAnnotation(AnnotationSpec
                        .builder(ClassName.get(JsonDeserialize.class))
                        .addMember("builder","$T.class",effectiveBuilderClassName)
                        .build()
                )
                .addMethod(MethodSpec
                        .methodBuilder("toMutable")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(ParameterizedTypeName.get(effectiveBuilderClassName, WildcardTypeName.subtypeOf(effectiveBuilderClassName)))
                        .addStatement("return new $T<$T>(this)",effectiveBuilderClassName,effectiveBuilderClassName)
                        .build()
                );

        TypeSpec.Builder builderTypeSpec = TypeSpec.classBuilder(effectiveBuilderClassName)
                .addTypeVariable(TypeVariableName.get("T",effectiveBuilderClassName))
                .addSuperinterface(ParameterizedTypeName.get(typeHelper.getEffectiveClassName(coreClassName, TypeHelper.SubType.BUILDER),TypeVariableName.get("T")))
                .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
                .addAnnotation(AnnotationSpec
                        .builder(ClassName.get(JsonPOJOBuilder.class))
                        .addMember("buildMethodName","$S","create")
                        .addMember("withPrefix","$S","with")
                        .build()
                )
                .addField(FieldSpec
                        .builder(effectiveClassName, ORIG_DATA_FIELD_NAME)
                        .addModifiers(Modifier.PRIVATE)
                        .build()
                )
                .addMethod(MethodSpec
                        .methodBuilder("create")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(effectiveClassName)
                        .addStatement("return new $T(this)",effectiveClassName)
                        .build()
                );

        MethodSpec.Builder typeSpecConstructor = MethodSpec.constructorBuilder()
                .addParameter(ParameterSpec.builder(effectiveBuilderClassName, BUILDER_PARAM_NAME).build())
                .addModifiers(Modifier.PROTECTED)
        ;


        MethodSpec.Builder builderConstructor = MethodSpec.constructorBuilder()
                .addParameter(ParameterSpec.builder(effectiveClassName, ORIG_ITEM_PARAM_NAME).build())
                .addModifiers(Modifier.PUBLIC)
                ;

        Context context = new Context(typeSpec, typeSpecConstructor, builderTypeSpec, builderConstructor);


        manageParent(model, context);
        context.builderConstructor.addStatement("this.$L = $L",ORIG_DATA_FIELD_NAME, ORIG_ITEM_PARAM_NAME);

        for(FieldModelDef field:model.fields){
            generateField(context,model,field);
        }

        builderTypeSpec.addMethod(builderConstructor.build());
        typeSpec.addType(builderTypeSpec.build());
        typeSpec.addMethod(typeSpecConstructor.build());
        return typeSpec.build();
    }

    private void generateField(Context context, ModelDef model, FieldModelDef field) {
        try {
            TypeName typeName = typeHelper.getTypeName(model, field.type, TypeHelper.SubType.INTERFACE);
            String jsonFieldName = StringUtils.isEmpty(field.dbName)?field.name:field.dbName;
            String classFieldName = StringUtils.lowerCaseFirst(field.name);
            FieldSpec.Builder fieldSpec = FieldSpec.builder(typeName,classFieldName)
                    .addModifiers(Modifier.PRIVATE);

            context.builderType.addField(fieldSpec.build());

            context.type.addField(fieldSpec.build().toBuilder()
                    .addModifiers(Modifier.FINAL)
                    .build());

            context.builderType.addMethod(MethodSpec
                    .methodBuilder("with"+StringUtils.capitalizeFirst(classFieldName))
                    .addAnnotation(Override.class)
                    .addAnnotation(AnnotationSpec.builder(JsonSetter.class)
                            .addMember("value","$S",jsonFieldName)
                            .build()
                    )
                    .addParameter(ParameterSpec.builder(typeName,"newValue").build())
                    .returns(ParameterizedTypeName.get(typeHelper.getEffectiveClassName(typeHelper.getCoreClassName(model), TypeHelper.SubType.IMPL_BUILDER),TypeVariableName.get("T")))
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("$L = newValue",classFieldName)
                    .addStatement("return this")
                    .build()
            );

            context.type.addMethod(MethodSpec
                    .methodBuilder(classFieldName)
                    .addAnnotation(AnnotationSpec.builder(JsonGetter.class)
                            .addMember("value","$S",jsonFieldName)
                            .build()
                    )
                    .addAnnotation(Override.class)
                    .returns(typeName)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return $L",classFieldName)
                    .build()
            );

            CodeBlock.Builder initializer = CodeBlock.builder();

            CodeBlock initializerFromBuilder = generateInitializerFromBuilder(typeName, classFieldName, initializer);
            CodeBlock defaultInitialiser = getDefaultInitializer(field, typeName);

            context.constructor.addCode(CodeBlock.builder()
                    .beginControlFlow("if ($L.$L != null)",BUILDER_PARAM_NAME,classFieldName)
                        .add("this.$L = ",classFieldName).add(initializerFromBuilder).add(";\n")
                    .nextControlFlow("else if ($L.$L.$L != null)", BUILDER_PARAM_NAME, ORIG_DATA_FIELD_NAME,classFieldName)
                        .addStatement("this.$L = $L.$L.$L", classFieldName, BUILDER_PARAM_NAME, ORIG_DATA_FIELD_NAME,classFieldName)
                    .nextControlFlow("else")
                        .add("this.$L = ",classFieldName).add(defaultInitialiser).add(";\n")
                    .endControlFlow()
                    .build()
                    );
        }
        catch(Throwable e){
            throw new RuntimeException("Cannot generate field "+field.name+" from type "+model.fullFilename,e);
        }
    }

    private CodeBlock getDefaultInitializer(FieldModelDef field, TypeName typeName) {
        CodeBlock defaultInitialiser;
        if(StringUtils.isNotEmpty(field.defaultStr)){
            if(typeName instanceof ClassName && ((ClassName) typeName).simpleName().equals("String")){
                defaultInitialiser = CodeBlock.of("$S",field.defaultStr);
            }
            else{
                defaultInitialiser = CodeBlock.of("$L",field.defaultStr);
            }
        }
        else{
            defaultInitialiser = CodeBlock.of("null");
            if(typeName instanceof ParameterizedTypeName){
                switch (((ParameterizedTypeName) typeName).rawType.simpleName()) {
                    case "Map":
                        defaultInitialiser= CodeBlock.of("$T.emptyMap()", Collections.class);
                        break;
                    case "List":
                        defaultInitialiser= CodeBlock.of("$T.emptyList()", Collections.class);
                        break;
                    case "Set":
                        defaultInitialiser= CodeBlock.of("$T.emptySet()", Collections.class);
                        break;
                    default:
                }
            }
        }
        return defaultInitialiser;
    }

    private CodeBlock generateInitializerFromBuilder(TypeName typeName, String classFieldName, CodeBlock.Builder initializer) {
        CodeBlock initializerFromBuilder = CodeBlock.of("$L.$L", BUILDER_PARAM_NAME, classFieldName);
        if(typeName instanceof ParameterizedTypeName){
            CodeBlock preprocessing = null;
            switch (((ParameterizedTypeName) typeName).rawType.simpleName()) {
                case "Map":
                    preprocessing=CodeBlock.builder().add("$T.unmodifiableMap(new $T", Collections.class, HashMap.class).build();
                    break;
                case "List":
                    preprocessing=CodeBlock.builder().add("$T.unmodifiableList(new $T",Collections.class, ArrayList.class).build();
                    break;
                case "Set":
                    preprocessing=CodeBlock.builder().add("$T.unmodifiableSet(new $T",Collections.class, HashSet.class).build();
                    break;
                default:
            }
            if(preprocessing!=null){
                initializerFromBuilder = CodeBlock.builder()
                        .add(preprocessing)
                        .add("(")
                        .add(initializerFromBuilder)
                        .add("))")
                        .build();
            }
        }
        return initializerFromBuilder;
    }

    private void manageParent(ModelDef model, Context context) {
        if(StringUtils.isNotEmptyAfterTrim(model.parent)){
            context.type.superclass(typeHelper.getTypeName(model,model.parent, TypeHelper.SubType.IMPL));
            context.constructor.addStatement("super($L)",BUILDER_PARAM_NAME);
            context.builderType.superclass(ParameterizedTypeName.get((ClassName)typeHelper.getTypeName(model, model.parent, TypeHelper.SubType.IMPL_BUILDER), TypeVariableName.get("T")));
            context.builderConstructor.addStatement("super($L)", ORIG_ITEM_PARAM_NAME);
        }
    }


    public static class Context{
        public final MethodSpec.Builder constructor;
        public final MethodSpec.Builder builderConstructor;
        public final TypeSpec.Builder type;
        public final TypeSpec.Builder builderType;

        public Context(TypeSpec.Builder type,MethodSpec.Builder constructor, TypeSpec.Builder builderType,MethodSpec.Builder builderConstructor) {
            this.constructor = constructor;
            this.builderConstructor = builderConstructor;
            this.type = type;
            this.builderType = builderType;
        }
    }
}