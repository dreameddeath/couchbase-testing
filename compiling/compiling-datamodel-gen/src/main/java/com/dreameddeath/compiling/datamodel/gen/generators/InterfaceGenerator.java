package com.dreameddeath.compiling.datamodel.gen.generators;

import com.dreameddeath.compiling.datamodel.gen.model.FieldModelDef;
import com.dreameddeath.compiling.datamodel.gen.model.ModelDef;
import com.dreameddeath.core.java.utils.StringUtils;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;

public class InterfaceGenerator {
    TypeHelper typeHelper = new TypeHelper();

    public TypeSpec generate(ModelDef model){
        ClassName coreClassName = typeHelper.getCoreClassName(model);
        ClassName effectiveClassName = typeHelper.getEffectiveClassName(coreClassName, TypeHelper.SubType.INTERFACE);
        ClassName effectiveBuilderClassName = typeHelper.getEffectiveClassName(coreClassName, TypeHelper.SubType.INTERFACE).nestedClass("Builder");
        TypeSpec.Builder typeSpec = TypeSpec.interfaceBuilder(effectiveClassName)
                .addModifiers(Modifier.PUBLIC);
        TypeSpec.Builder builderTypeSpec = TypeSpec.interfaceBuilder(effectiveBuilderClassName)
                .addTypeVariable(TypeVariableName.get("T",effectiveBuilderClassName))
                .addModifiers(Modifier.PUBLIC,Modifier.STATIC);
        MethodSpec.Builder toMutable = MethodSpec.methodBuilder("toMutable")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(ParameterizedTypeName.get(effectiveBuilderClassName, WildcardTypeName.subtypeOf(effectiveBuilderClassName)));

        MethodSpec.Builder createType = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC,Modifier.ABSTRACT)
                .returns(effectiveClassName);

        if(!model.flags.contains(ModelDef.Flag.ABSTRACT)){
            builderTypeSpec.addMethod(MethodSpec.methodBuilder("newInstance")
                    .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
                    .returns(ParameterizedTypeName.get(effectiveBuilderClassName, WildcardTypeName.subtypeOf(effectiveBuilderClassName)))
                    .addStatement("return new $T()",typeHelper.getEffectiveClassName(coreClassName, TypeHelper.SubType.IMPL_BUILDER))
                    .build()
            );
        }
        if(StringUtils.isNotEmptyAfterTrim(model.parent)){
            typeSpec.addSuperinterface(typeHelper.getTypeName(model,model.parent, TypeHelper.SubType.INTERFACE));
            ClassName typeName = (ClassName)typeHelper.getTypeName(model, model.parent, TypeHelper.SubType.BUILDER);
            builderTypeSpec.addSuperinterface(
                    ParameterizedTypeName.get(typeName,TypeVariableName.get("T")));
            toMutable.addAnnotation(Override.class);
            createType.addAnnotation(Override.class);
        }

        for(FieldModelDef fieldDef:model.fields){
            generateField(model, coreClassName, typeSpec, builderTypeSpec, fieldDef);
        }

        typeSpec.addMethod(toMutable.build());
        builderTypeSpec.addMethod(createType.build());

        typeSpec.addType(builderTypeSpec.build());
        return typeSpec.build();
    }

    private void generateField(ModelDef model, ClassName coreClassName, TypeSpec.Builder typeSpec, TypeSpec.Builder builderTypeSpec, FieldModelDef fieldDef) {
        try {
            TypeName typeName = typeHelper.getTypeName(model, fieldDef.type, TypeHelper.SubType.INTERFACE);
            typeSpec.addMethod(
                    MethodSpec
                            .methodBuilder(StringUtils.lowerCaseFirst(fieldDef.name))
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .returns(typeName)
                            .build()
            );

            builderTypeSpec.addMethod(
                    MethodSpec.methodBuilder("with"+StringUtils.capitalizeFirst(fieldDef.name))
                            .addModifiers(Modifier.PUBLIC,Modifier.ABSTRACT)
                            .returns(ParameterizedTypeName.get(typeHelper.getEffectiveClassName(coreClassName, TypeHelper.SubType.BUILDER),TypeVariableName.get("T")))
                            .addParameter(ParameterSpec.builder(typeName,"value").build())
                    .build()
            );
        }
        catch (Throwable e){
            throw new RuntimeException("Cannot manage field "+fieldDef.name+" from model "+model.fullFilename,e);
        }
    }
}
