/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.dreameddeath.core.model.dto.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.MethodInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.dto.annotation.DtoConverterForEntity;
import com.dreameddeath.core.model.dto.annotation.DtoGenerate;
import com.dreameddeath.core.model.dto.annotation.processor.ConverterGeneratorContext;
import com.dreameddeath.core.model.dto.annotation.processor.FieldFilteringMode;
import com.dreameddeath.core.model.dto.converter.*;
import com.dreameddeath.core.model.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 04/03/2016.
 */
public class ConverterServiceInfo {
    private final JavaFile.Builder javaFileBuilder;
    private final TypeSpec.Builder codeTypeBuilder;
    private final MethodSpec.Builder factoryAwareMethodBuilder;
    private final Map<MapperFieldEntry,CodeBlock> converterFieldMap=new HashMap<>();
    private final Map<MapperFieldEntry,CodeBlock> unwrappedFctMap=new HashMap<>();
    private final Map<MapperEnumEntry,CodeBlock> converterEnumMap=new HashMap<>();

    public JavaFile getJavaFile() {
        return javaFileBuilder.build();
    }

    private final class MapperEnumEntry {
        private final boolean isInput;
        private final EnumModel model;

        MapperEnumEntry(boolean isInput, EnumModel model) {
            this.isInput = isInput;
            this.model = model;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MapperEnumEntry that = (MapperEnumEntry) o;

            if (isInput != that.isInput) return false;
            return model.equals(that.model);
        }

        @Override
        public int hashCode() {
            int result = (isInput ? 1 : 0);
            result = 31 * result + model.hashCode();
            return result;
        }
    }

    private final class MapperFieldEntry{
        private final boolean isInput;
        private final DtoModel model;

        public MapperFieldEntry(boolean isInput, DtoModel model) {
            this.isInput = isInput;
            this.model = model;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MapperFieldEntry that = (MapperFieldEntry) o;

            if (isInput != that.isInput) return false;
            return model.equals(that.model);
        }

        @Override
        public int hashCode() {
            int result = (isInput ? 1 : 0);
            result = 31 * result + model.hashCode();
            return result;
        }
    }

    public ConverterServiceInfo(ClassInfo info, ConverterGeneratorContext context, DtoGenerate dtoGenerateAnnot, String version){
        CouchbaseDocumentReflection reflectionInfo = CouchbaseDocumentReflection.getReflectionFromClassInfo(info);

        String servicePackageName;
        if(dtoGenerateAnnot !=null && StringUtils.isNotEmpty(dtoGenerateAnnot.targetConverterPackageName())) {
            servicePackageName = dtoGenerateAnnot.targetConverterPackageName();
        }
        else{
            servicePackageName = reflectionInfo.getClassInfo().getPackageInfo().getName().replace(".model.",".converter.");
        }
        ConverterServiceReferenceInfo serviceReferenceInfo = new ConverterServiceReferenceInfo(servicePackageName, (reflectionInfo.getClassInfo().getSimpleName() + "Converter").replace("$", ""), version);

        String dtoPackageName;
        if(dtoGenerateAnnot !=null && StringUtils.isNotEmpty(dtoGenerateAnnot.targetModelPackageName())){
            dtoPackageName = dtoGenerateAnnot.targetModelPackageName();
        }
        else{
            dtoPackageName = reflectionInfo.getClassInfo().getPackageInfo().getName()+".published";
        }

        codeTypeBuilder = TypeSpec.classBuilder(serviceReferenceInfo.getClassName());
        codeTypeBuilder.addModifiers(Modifier.PUBLIC);
        codeTypeBuilder.addSuperinterface(IDtoFactoryAware.class);
        factoryAwareMethodBuilder = MethodSpec.methodBuilder("setDtoConverterFactory")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(DtoConverterFactory.class,"factory");

        if(dtoGenerateAnnot !=null && dtoGenerateAnnot.buildInput()) {
            DtoModel input = DtoModel.getOrBuildModel(context, reflectionInfo.getStructure(), dtoPackageName, new GenMode(FieldFilteringMode.STANDARD, true), serviceReferenceInfo);
            generateInputConverterFct(input);
        }

        if(dtoGenerateAnnot ==null || dtoGenerateAnnot.buildOutput()) {
            DtoModel output = DtoModel.getOrBuildModel(context, reflectionInfo.getStructure(), dtoPackageName, new GenMode(FieldFilteringMode.STANDARD, false), serviceReferenceInfo);
            generateOutputConverterFct(output);
        }
        codeTypeBuilder.addMethod(factoryAwareMethodBuilder.build());
        codeTypeBuilder.addAnnotation(AnnotationSpec.builder(DtoConverterForEntity.class)
                .addMember("entityClass","$T.class",info.getClassName())
                .addMember("version","$S",version).build()
        );
        javaFileBuilder = JavaFile.builder(serviceReferenceInfo.getPackageName(),codeTypeBuilder.build());
    }

    private String generateOutputConverterFct(DtoModel model) {
        codeTypeBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(IDtoOutputMapper.class),model.getOrigClassInfo().getClassName(),model.getClassName()));
        converterFieldMap.put(new MapperFieldEntry(false,model),CodeBlock.of("this"));
        
        if(!model.isAbstract()){
            codeTypeBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(IDtoOutputConverter.class),model.getOrigClassInfo().getClassName(),model.getClassName()));
            codeTypeBuilder.addMethod(MethodSpec.methodBuilder("convertToOutput")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(model.origClassInfo.getClassName(),"doc")
                    .returns(model.getClassName())
                    .addStatement("return mapToOutput(doc,new $T())",model.getClassName())
                    .build());
        }
        
        generateOutputMapperFct(model);
        return "convertToOutput";
    }
    
    private String generateOutputMapperFct(DtoModel model){
        String fctName="mapToOutput";
        MethodSpec.Builder outputMapperMethod = MethodSpec.methodBuilder(fctName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(model.origClassInfo.getClassName(),"doc")
                .addParameter(model.isUnwrapped()?model.getUnwrappedSourceModel().getClassName():model.getClassName(),"output")
                .returns(model.isUnwrapped()?model.getUnwrappedSourceModel().getClassName():model.getClassName());

        if(model.hasParentModel()){
            outputMapperMethod.addCode("output = ($T)", model.getClassName());
            outputMapperMethod.addCode(generateDynamicField(new MapperFieldEntry(false,model.getParentModel()),true));
            outputMapperMethod.addCode(".mapToOutput(doc,output);\n");
        }

        for(DtoModelField field:model.getFieldsForMapping()){
            outputMapperMethod.addCode(buildSetterCodeBlock("output",model,field,buildGetterCodeBlockWithMapping("doc",model,field)));
            outputMapperMethod.addCode(CodeBlock.of(";\n"));
        }

        outputMapperMethod.addStatement("return output");
        codeTypeBuilder.addMethod(outputMapperMethod.build());
        return fctName;
    }

    private CodeBlock generateDynamicField(MapperFieldEntry entry,boolean isMapper){
        String suffix = isMapper?"Mapper":"Converter";
        String fieldName = StringUtils.lowerCaseFirst(entry.model.getShortName())+(entry.isInput?"Input":"Output")+suffix;
        codeTypeBuilder.addField(FieldSpec.builder(
                ParameterizedTypeName.get(
                        isMapper?ClassName.get(entry.isInput?IDtoInputMapper.class:IDtoOutputMapper.class):
                                ClassName.get(entry.isInput?IDtoInputConverter.class:IDtoOutputConverter.class),
                        entry.model.getOrigClassInfo().getClassName(),entry.model.getClassName()),
                fieldName,Modifier.PRIVATE).build());
        //TODO manage version
        factoryAwareMethodBuilder.addStatement(
                "$L = factory."+(entry.isInput?"getDtoInput":"getDtoOutput")+suffix+"($T.class,$T.class)",fieldName,entry.model.origClassInfo.getClassName(),entry.model.getClassName());
        return CodeBlock.builder().add("$L",fieldName).build();
    }

    private String generateInputConverterFct(DtoModel model){
        if(!model.isUnwrapped()) {
            codeTypeBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(IDtoInputMapper.class), model.getOrigClassInfo().getClassName(), model.getClassName()));
            converterFieldMap.put(new MapperFieldEntry(true, model), CodeBlock.of("this"));
            if (!model.isAbstract()) {
                codeTypeBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(IDtoInputConverter.class), model.getOrigClassInfo().getClassName(), model.getClassName()));
            }
        }

        String fctName="convertToDoc";
        if(model.isUnwrapped()){
            fctName+=model.origClassInfo.getSimpleName();
        }

        codeTypeBuilder.addMethod(MethodSpec.methodBuilder(fctName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(model.isUnwrapped()?model.getUnwrappedSourceModel().getClassName():model.getClassName(), "input")
                .returns(model.origClassInfo.getClassName())
                .addStatement("return mapToDoc(new $T(),input)", model.origClassInfo.getClassName())
                .build());

        generateInputMapperFct(model);

        return fctName;
    }

    private void generateInputMapperFct(DtoModel model){
        MethodSpec.Builder inputMapperMethod = MethodSpec.methodBuilder("mapToDoc")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(model.origClassInfo.getClassName(),"doc")
                .addParameter(model.isUnwrapped()?model.getUnwrappedSourceModel().getClassName():model.getClassName(),"input")
                .returns(model.origClassInfo.getClassName());

        if(model.hasParentModel()){
            inputMapperMethod.addCode("doc = ($T)", model.origClassInfo.getClassName());
            inputMapperMethod.addCode(generateDynamicField(new MapperFieldEntry(true,model.getParentModel()),true));
            inputMapperMethod.addCode("mapToDoc(doc,input);\n");
        }

        for(DtoModelField field:model.getFieldsForMapping()){
            inputMapperMethod.addCode(buildSetterCodeBlock("doc",model,field,buildGetterCodeBlockWithMapping("input",model,field)));
            inputMapperMethod.addCode(CodeBlock.of(";\n"));
        }
        inputMapperMethod.addStatement("return doc");
        codeTypeBuilder.addMethod(inputMapperMethod.build());
    }

    private CodeBlock buildGetterCodeBlockWithMapping(String varName, DtoModel parent, DtoModelField fieldInfo){
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.add("$L",varName);
        if(parent.isInput){
            if(fieldInfo.unwrapMode!= DtoModelField.UnwrapMode.UNWRAP_SOURCE){
                codeBlock.add("." + fieldInfo.getGetterName() + "()");
            }
        }
        else{
            codeBlock.add(".");
            if(fieldInfo.unwrapMode== DtoModelField.UnwrapMode.UNWRAP_CHILD) {
                CouchbaseDocumentFieldReflection parentRootField = fieldInfo.unwrappedModel.origStructInfo.getFieldByPropertyName((fieldInfo.unwrappedRootField.jobFieldName));
                codeBlock.add(parentRootField.getGetterName() + ((parentRootField.getGetter() instanceof MethodInfo) ? "()" : "") + ".");
            }
            CouchbaseDocumentFieldReflection field = parent.origStructInfo.getFieldByPropertyName(fieldInfo.jobFieldName);
            codeBlock.add(field.getGetterName()+((field.getGetter() instanceof MethodInfo)?"()":""));
        }

        if(fieldInfo.isComplexType){
            if(fieldInfo.typeStructure == DtoModelField.Type.COLLECTION){
                codeBlock.add(".stream().map(");
                if(fieldInfo.isEnum){
                    codeBlock.add("this::").add(getEnumConverterFunction(fieldInfo,parent.isInput));
                }
                else{
                    codeBlock.add("arrayElt->").add(getComplexTypeConverterFctCall(fieldInfo.complexTypeModel,parent.isInput)).add("(arrayElt)");
                }
                codeBlock.add(").collect($T.toList())",Collectors.class);
            }
            else if(fieldInfo.typeStructure== DtoModelField.Type.MAP){
                codeBlock.add(".entrySet().stream().map(entryElt->new $T<>(entryElt.getKey(),", AbstractMap.SimpleImmutableEntry.class);
                if(fieldInfo.isEnum){
                    codeBlock.add("this.").add(getEnumConverterFunction(fieldInfo,parent.isInput));
                }
                else{
                    codeBlock.add("arrayElt->").add(getComplexTypeConverterFctCall(fieldInfo.complexTypeModel,parent.isInput));
                }
                codeBlock.add("(entryElt.getValue()))).collect($T.toMap($T::getKey, $T::getValue))",Collectors.class,Map.Entry.class,Map.Entry.class);
            }
            else{
                CodeBlock.Builder origGetter = codeBlock;
                if(fieldInfo.isEnum){
                    codeBlock = CodeBlock.builder();
                    codeBlock = codeBlock.add(getEnumConverterFunction(fieldInfo,parent.isInput));
                }
                else{
                    if(fieldInfo.unwrapMode== DtoModelField.UnwrapMode.UNWRAP_SOURCE){
                        if(parent.isInput) {
                            codeBlock = CodeBlock.builder()
                                .add(getComplexTypeConverterFctCall(fieldInfo.unwrappedModel,parent.isInput));
                        }
                    }
                    else{
                        codeBlock = CodeBlock.builder()
                                    .add(getComplexTypeConverterFctCall(fieldInfo.complexTypeModel,parent.isInput));
                    }
                }
                if(codeBlock!=origGetter) {
                    codeBlock.add("(").add(origGetter.build()).add(")");
                }
            }
        }
        
        return codeBlock.build();
    }

    private CodeBlock buildSetterCodeBlock(String varName, DtoModel parent, DtoModelField fieldInfo, CodeBlock value){
        CodeBlock.Builder builder = CodeBlock.builder();
        if(parent.isInput){
            CouchbaseDocumentFieldReflection field = parent.origStructInfo.getFieldByPropertyName(fieldInfo.jobFieldName);
            if(field==null){
                throw new RuntimeException("Cannot find field "+fieldInfo.jobFieldName+" for setter in struct "+parent.origStructInfo.getName());
            }
            builder.add("$L."+field.getSetterName(),varName);
            if(field.getSetter() instanceof MethodInfo){
                builder.add("(").add(value).add(")");
            }
            else{
                builder.add("=").add(value);
            }
        }
        else {
            if (fieldInfo.unwrapMode == DtoModelField.UnwrapMode.UNWRAP_SOURCE) {
                return CodeBlock.builder().add(getComplexTypeConverterFctCall(fieldInfo.complexTypeModel,false)).add("(").add(value).add(",$L)",varName).build();
            } else {
                return CodeBlock.builder().add("$L.$L(",varName,fieldInfo.getSetterName())
                        .add(value).add(")").build();
            }
        }
        return builder.build();
    }

    private CodeBlock getEnumConverterFunction(DtoModelField fieldInfo,boolean isInput) {
        MapperEnumEntry entry = new MapperEnumEntry(isInput,fieldInfo.enumModel);
        return converterEnumMap.computeIfAbsent(entry,this::buildEnumMapperFunction);
    }

    private CodeBlock buildEnumMapperFunction(MapperEnumEntry mapperEnumEntry) {
        String fctName = "mapEnum"+(mapperEnumEntry.isInput?"Input":"Output");
        ClassName inputClassName=mapperEnumEntry.isInput?mapperEnumEntry.model.getClassName():mapperEnumEntry.model.origClassInfo.getClassName();
        ClassName outputClassName=mapperEnumEntry.isInput?mapperEnumEntry.model.origClassInfo.getClassName():mapperEnumEntry.model.getClassName();
        codeTypeBuilder.addMethod(MethodSpec.methodBuilder(fctName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(inputClassName, "source")
                .returns(outputClassName)
                .beginControlFlow("if(source==null)")
                .addStatement("return null")
                .endControlFlow()
                .addStatement("$T sourceValue = source.toString()",String.class)
                .beginControlFlow("for($T value:$T.values())",outputClassName,outputClassName)
                .beginControlFlow("if(value.toString().equals(sourceValue))")
                .addStatement("return value")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return null")
                .build());

        return CodeBlock.of("mapEnum"+(mapperEnumEntry.isInput?"Input":"Output"));
    }

    private CodeBlock getComplexTypeConverterFctCall(DtoModel model,boolean isInput){
        if(model.isUnwrapped()){
            return converterFieldMap.computeIfAbsent(new MapperFieldEntry(isInput, model), this::generateUnwrappedFct);
        }
        else{
            return CodeBlock.builder()
                    .add(getComplexTypeConverterVarName(model, isInput))
                    .add(isInput ? ".convertToDoc" : ".convertToOutput")
                    .build();
        }
    }

    private CodeBlock getComplexTypeConverterVarName(DtoModel model,boolean isInput){
        return converterFieldMap.computeIfAbsent(new MapperFieldEntry(isInput, model), entry->generateDynamicField(entry,false));
    }

    private CodeBlock generateUnwrappedFct(MapperFieldEntry mapperFieldEntry) {
        if(mapperFieldEntry.isInput){
            return unwrappedFctMap.computeIfAbsent(mapperFieldEntry,entry->CodeBlock.of(generateInputConverterFct(mapperFieldEntry.model)));
        }
        else {
            return unwrappedFctMap.computeIfAbsent(mapperFieldEntry,entry->CodeBlock.of(generateOutputMapperFct(mapperFieldEntry.model)));
        }
    }

    public static class GenMode{
        private final FieldFilteringMode fieldMode;
        private boolean isRoot=false;
        private boolean isInput;
        private boolean forUnwrap=false;
        private List<DtoModel> unwrapSourceDtoModels=new ArrayList<>();
        private boolean isForChild=false;

        GenMode(FieldFilteringMode mode, boolean isInput){
            this.fieldMode = mode;
            this.isInput = isInput;
            this.isRoot = true;
        }

        GenMode(GenMode parent, FieldFilteringMode mode){
            this.isInput=parent.isInput;
            if(mode==FieldFilteringMode.INHERIT){
                this.fieldMode = parent.fieldMode;
            }
            else{
                this.fieldMode=mode;
            }
        }

        GenMode unwrapFor(GenMode parentMode, DtoModel parent) {
            this.forUnwrap=true;
            this.unwrapSourceDtoModels.addAll(parentMode.unwrapSourceDtoModels);
            this.unwrapSourceDtoModels.add(parent);
            return this;
        }

        GenMode childFor(DtoModel model){
            this.isForChild=true;
            return this;
        }

        FieldFilteringMode getFieldMode() {
            return fieldMode;
        }

        boolean isRoot() {
            return isRoot;
        }

        boolean isInput() {
            return isInput;
        }

        boolean isForUnwrap() {
            return forUnwrap;
        }

        List<DtoModel> getUnwrapSourceDtoModels() {
            return unwrapSourceDtoModels;
        }

        boolean isForChild() {
            return isForChild;
        }

    }
}
