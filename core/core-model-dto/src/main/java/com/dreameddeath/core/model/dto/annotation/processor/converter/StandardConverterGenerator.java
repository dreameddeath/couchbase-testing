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

package com.dreameddeath.core.model.dto.annotation.processor.converter;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.MethodInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.dto.annotation.DtoConverterForEntity;
import com.dreameddeath.core.model.dto.annotation.DtoFieldMappingInfo;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.annotation.DtoModelMappingInfo;
import com.dreameddeath.core.model.dto.converter.*;
import com.dreameddeath.core.model.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.google.common.base.Preconditions;
import com.squareup.javapoet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by CEAJ8230 on 05/06/2017.
 */
public class StandardConverterGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(StandardConverterGenerator.class);
    public static final String MAP_TO_DOC_FCT_NAME = "mapToDoc";
    private static final String MAP_TO_OUTPUT_FCT_NAME = "mapToOutput";
    public static final String UNWRAP_CONVERT_TO_DOC_FCT_NAME = "unwrappedConvertToDoc";
    private static final String UNWRAP_MAP_FROM_DOC_FCT_NAME = "unwrappedMapFromDoc";
    private static final String CONVERT_TO_OUTPUT_FCT_NAME = "convertToOutput";
    private static final String CONVERT_TO_DOC_FCT_NAME = "convertToDoc";
    private final Map<Key,ClassName> converterMap = new HashMap<>();
    private final List<JavaFile> files = new ArrayList<>();

    public void addExistingConverter(ClassInfo dtoClassName, ClassName converterClassName) {
        converterMap.put(new Key(dtoClassName),converterClassName);
    }

    private ConversionMainParams buildConversionParams(ClassInfo dtoModelClassInfo){
        try {
            DtoModelMappingInfo mappingInfoAnnot = dtoModelClassInfo.getAnnotation(DtoModelMappingInfo.class);
            if(mappingInfoAnnot!=null) {
                ClassInfo origClassInfo = (ClassInfo) ClassInfo.getClassInfo(mappingInfoAnnot.entityClassName());
                return new ConversionMainParams(mappingInfoAnnot, dtoModelClassInfo, origClassInfo);
            }
            else{
                return null;
            }
        }
        catch(ClassNotFoundException e){
            LOG.error("Cannot get data for model {}",dtoModelClassInfo.getFullName());
            throw new RuntimeException(e);
        }
    }

    public ClassName buildConverter(ClassInfo dtoModelClassInfo) {
        ConversionMainParams conversionMainParams = buildConversionParams(dtoModelClassInfo);
        if(conversionMainParams==null) return null;
        Key dtoModelKey = new Key(dtoModelClassInfo);
        if(converterMap.containsKey(dtoModelKey)){
            return converterMap.get(dtoModelKey);
        }

        try {
            String packageName = conversionMainParams.origClassInfo.getPackageInfo().getName()+".converter"+ (StringUtils.isNotEmpty(conversionMainParams.mappingInfoAnnot.type())?"."+ conversionMainParams.mappingInfoAnnot.type():"");
            String className = dtoModelClassInfo.getSimpleName()+(
                            conversionMainParams.mappingInfoAnnot.mode()== DtoInOutMode.IN?"Input"
                            : conversionMainParams.mappingInfoAnnot.mode()== DtoInOutMode.OUT?"Output"
                            :"")
                            +"V"+ conversionMainParams.mappingInfoAnnot.version().replace(".","_")
                            +"Converter";


            TypeSpec.Builder codeTypeBuilder = TypeSpec.classBuilder(className);
            codeTypeBuilder.addModifiers(Modifier.PUBLIC);
            codeTypeBuilder.addSuperinterface(IDtoFactoryAware.class);
            MethodSpec.Builder factoryAwareMethodBuilder = MethodSpec.methodBuilder("setDtoConverterFactory")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(DtoConverterFactory.class, "factory");

            Context context = new Context(codeTypeBuilder, factoryAwareMethodBuilder, conversionMainParams);

            if(conversionMainParams.mappingInfoAnnot.mode()==DtoInOutMode.IN || conversionMainParams.mappingInfoAnnot.mode()==DtoInOutMode.BOTH) {
                generateInputConverterFct(context);
            }
            if(conversionMainParams.mappingInfoAnnot.mode()==DtoInOutMode.OUT || conversionMainParams.mappingInfoAnnot.mode()==DtoInOutMode.BOTH) {
                generateOutputConverterFct(context);
            }

            for(UnwrappedContext unwrappedContext : context.unwrappedVariableMap.values()){
                if(unwrappedContext.isInput){
                    unwrappedContext.methodBuilder.addStatement("return doc");
                }
                else{
                    unwrappedContext.methodBuilder.addStatement("return output");
                }

                codeTypeBuilder.addMethod(unwrappedContext.methodBuilder.build());
            }

            codeTypeBuilder.addMethod(factoryAwareMethodBuilder.build());
            codeTypeBuilder.addAnnotation(AnnotationSpec.builder(DtoConverterForEntity.class)
                    .addMember("entityClass","$T.class", conversionMainParams.origClassInfo.getClassName())
                    .addMember("version","$S", conversionMainParams.mappingInfoAnnot.version())
                    .addMember("mode","$T.$L", ClassName.get(DtoInOutMode.class),conversionMainParams.mappingInfoAnnot.mode())
                    .addMember("type","$S", conversionMainParams.mappingInfoAnnot.type())
                    .build()
            );
            JavaFile file = JavaFile.builder(packageName,codeTypeBuilder.build()).build();
            files.add(file);
            ClassName converterClassName = ClassName.get(file.packageName,file.typeSpec.name);
            converterMap.put(new Key(dtoModelClassInfo),converterClassName);
            return converterClassName;
        }
        catch(Throwable e){
            LOG.error("Cannot generate converter for class {}",dtoModelClassInfo.getFullName());
            throw new RuntimeException(e);
        }
    }

    private void generateInputConverterFct(Context context) {
        context.codeTypeBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(IDtoInputMapper.class), context.params.origClassInfo.getClassName(), context.params.dtoModel.getClassName()));
        context.converterFieldMap.put(new MapperFieldEntry(true, context.params), CodeBlock.of("this"));
        if (!context.params.dtoModel.isAbstract()) {
            context.codeTypeBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(IDtoInputConverter.class), context.params.origClassInfo.getClassName(), context.params.dtoModel.getClassName()));

            String fctName=CONVERT_TO_DOC_FCT_NAME;

            context.codeTypeBuilder.addMethod(MethodSpec.methodBuilder(fctName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(context.params.dtoModel.getClassName(), "input")
                    .returns(context.params.origClassInfo.getClassName())
                    .addStatement("return $L(new $T(),input)",MAP_TO_DOC_FCT_NAME, context.params.origClassInfo.getClassName())
                    .build());
        }

        generateInputMapperFct(context);
    }


    private void generateOutputConverterFct(Context context) {
        context.codeTypeBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(IDtoOutputMapper.class), context.params.origClassInfo.getClassName(), context.params.dtoModel.getClassName()));
        context.converterFieldMap.put(new MapperFieldEntry(false, context.params), CodeBlock.of("this"));

        if (!context.params.dtoModel.isAbstract()) {
            context.codeTypeBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(IDtoOutputConverter.class), context.params.origClassInfo.getClassName(), context.params.dtoModel.getClassName()));
            context.codeTypeBuilder.addMethod(MethodSpec.methodBuilder(CONVERT_TO_OUTPUT_FCT_NAME)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(context.params.origClassInfo.getClassName(), "doc")
                    .returns(context.params.dtoModel.getClassName())
                    .addStatement("return $L(doc,new $T())",MAP_TO_OUTPUT_FCT_NAME, context.params.dtoModel.getClassName())
                    .build());
        }

        generateOutputMapperFct(context);
    }

    private void generateOutputMapperFct(Context context){
        MethodSpec.Builder outputMapperMethod = MethodSpec.methodBuilder(MAP_TO_OUTPUT_FCT_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(context.params.origClassInfo.getClassName(),"doc")
                .addParameter(context.params.dtoModel.getClassName(),"output")
                .returns(context.params.dtoModel.getClassName());

        ClassInfo superClass = context.params.dtoModel.getSuperClass();
        if(superClass !=null){
            ConversionMainParams conversionMainParams = buildConversionParams(superClass);
            if(conversionMainParams!=null) {
                outputMapperMethod.addCode("output = ($T)", context.params.dtoModel.getClassName());
                outputMapperMethod.addCode(getComplexTypeConverterVarName(context, new MapperFieldEntry(false, conversionMainParams), true));
                outputMapperMethod.addCode(".$L(doc,output);\n", MAP_TO_OUTPUT_FCT_NAME);
            }
        }

        buildMapperMethod(context,outputMapperMethod,false);

        outputMapperMethod.addStatement("return output");
        context.codeTypeBuilder.addMethod(outputMapperMethod.build());
    }



    private void generateInputMapperFct(Context context){
        MethodSpec.Builder inputMapperMethod = MethodSpec.methodBuilder(MAP_TO_DOC_FCT_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(context.params.origClassInfo.getClassName(),"doc")
                .addParameter(context.params.dtoModel.getClassName(),"input")
                .returns(context.params.origClassInfo.getClassName());

        ClassInfo superClass = context.params.dtoModel.getSuperClass();
        if(superClass !=null){
            ConversionMainParams conversionMainParams = buildConversionParams(superClass);
            if(conversionMainParams!=null) {
                inputMapperMethod.addCode("doc = ($T)", context.params.origClassInfo.getClassName());
                inputMapperMethod.addCode(getComplexTypeConverterVarName(context, new MapperFieldEntry(true, conversionMainParams), true));
                inputMapperMethod.addCode(".$L(doc,input);\n", MAP_TO_DOC_FCT_NAME);
            }
        }

        buildMapperMethod(context,inputMapperMethod,true);

        inputMapperMethod.addStatement("return doc");
        context.codeTypeBuilder.addMethod(inputMapperMethod.build());
    }

    private void buildMapperMethod(Context context,MethodSpec.Builder mapperMethod,boolean isInput){
        for(FieldInfo field:context.params.dtoModel.getDeclaredFields()){
            DtoFieldMappingInfo[] annots = field.getAnnotationByType(DtoFieldMappingInfo.class);
            for(DtoFieldMappingInfo annot:annots){
                boolean isApplicable = annot.mode()==DtoInOutMode.BOTH ||
                        (isInput && annot.mode()==DtoInOutMode.IN) ||
                        (!isInput && annot.mode()==DtoInOutMode.OUT);
                if(isApplicable){
                    switch (annot.mappingType()){
                        case COPY:
                            generateCopy(context,context.params.origClassInfo,annot,field,isInput,mapperMethod);
                            break;
                        case SIMPLE_MAP:
                            generateSimpleMap(context,annot,field,isInput,mapperMethod);
                            break;
                        case MVEL:
                            break;
                        case STATIC_METHOD:
                            break;
                    }
                }
            }
        }
    }

    private String getOrigStructFieldName(ClassInfo orig,DtoFieldMappingInfo annot,FieldInfo dtoFieldInfo){
        if(annot.mappingType()== DtoFieldMappingInfo.MappingRuleType.SIMPLE_MAP){
            String[] parts = annot.ruleValue().split("\\.");
            return parts[parts.length-1];
        }
        return dtoFieldInfo.getName();
    }

    private CodeBlock buildOrigStructGetter(ClassInfo orig,String fieldName,CodeBlock source){
        CodeBlock.Builder getterBuilder = CodeBlock.builder();
        getterBuilder.add(source);
        CouchbaseDocumentFieldReflection fieldByPropertyName = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(orig).getFieldByPropertyName(fieldName);
        Preconditions.checkNotNull(fieldByPropertyName,"Cannot find field %s in class %s",fieldName,orig.getFullName());
        if(fieldByPropertyName.getGetter() instanceof MethodInfo){
            getterBuilder.add(".$L()",fieldByPropertyName.getGetter().getName());
        }
        else{
            getterBuilder.add(".$L",fieldByPropertyName.getField().getName());
        }
        return getterBuilder.build();
    }



    private CodeBlock buildOrigStructSetter(FieldInfo dtoFieldInfo,ClassInfo orig,String fieldName,CodeBlock source,CodeBlock value){
        CodeBlock.Builder setterBuilder = CodeBlock.builder();
        setterBuilder.add(source);
        CouchbaseDocumentFieldReflection fieldByPropertyName = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(orig).getFieldByPropertyName(fieldName);
        Preconditions.checkNotNull(fieldByPropertyName,"Cannot find field %s in class %s from dto field %s",fieldName,orig.getFullName(),dtoFieldInfo.getFullName());
        if(fieldByPropertyName.getSetter() instanceof MethodInfo){
            setterBuilder.add(".$L(",fieldByPropertyName.getSetterName());
            setterBuilder.add(value);
            setterBuilder.add(")");
        }
        else{
            setterBuilder.add(".$L=",fieldByPropertyName.getField().getName());
            setterBuilder.add(value);
        }
        return setterBuilder.build();
    }

    private String getDtoModelGetter(DtoFieldMappingInfo annot,FieldInfo dtoFieldInfo){
        //TODO improve by using JsonGetter annotation
        return "get"+StringUtils.capitalizeFirst(dtoFieldInfo.getName());
    }

    private String getDtoModelSetter(DtoFieldMappingInfo annot,FieldInfo dtoFieldInfo){
        //TODO improve by using JsonSetter annotation
        return "set"+StringUtils.capitalizeFirst(dtoFieldInfo.getName());
    }

    private CodeBlock generateCopyWithMap(Context context,ClassInfo origClassInfo, DtoFieldMappingInfo annot, FieldInfo dtoFieldInfo, boolean isInput){
        CodeBlock.Builder copyBuilder = CodeBlock.builder();

        AbstractClassInfo mainType = dtoFieldInfo.getType().getMainType();
        CodeBlock.Builder sourceGetter=CodeBlock.builder();
        CodeBlock.Builder suffix=CodeBlock.builder();
        ClassInfo contentType;

        if(isInput) {
            sourceGetter.add("input.$L()",getDtoModelGetter(annot, dtoFieldInfo));
        }
        else {
            sourceGetter.add(
                    buildOrigStructGetter(origClassInfo,
                            getOrigStructFieldName(origClassInfo, annot, dtoFieldInfo),
                            CodeBlock.of("doc")
                    ));
        }

        if(mainType.isInstanceOf(Collection.class)){
            contentType = (ClassInfo)dtoFieldInfo.getType().getMainTypeGeneric(0).getMainType();
            copyBuilder.add(sourceGetter.build());
            copyBuilder.add(".stream()");
            if(!contentType.isBaseType()) {
                copyBuilder.add(".map(elt->");
                suffix.add("(elt))");
            }
            suffix.add(".collect($T.toList())",Collectors.class);
        }
        else if(mainType.isInstanceOf(Map.class)){
            copyBuilder.add(sourceGetter.build());
            copyBuilder.add(".entrySet().stream()");
            contentType = (ClassInfo)dtoFieldInfo.getType().getMainTypeGeneric(1).getMainType();
            if(!contentType.isBaseType()){
                copyBuilder.add(".map(entry->new $T<>(entry.getKey(),",AbstractMap.SimpleImmutableEntry.class);
                suffix.add("(entry.getValue())))");
            }
            suffix.add(".collect($T.toMap($T::getKey, $T::getValue))",Collectors.class,Map.Entry.class,Map.Entry.class);
        }
        else {
            contentType = (ClassInfo)mainType;
            if(!mainType.isBaseType()){
                suffix.add("(");
            }
            suffix.add(sourceGetter.build());
            if(!mainType.isBaseType()){
                suffix.add(")");
            }

        }

        if(!contentType.isBaseType()){
            if(contentType.isEnum()){
                copyBuilder.add(getEnumConverterFunction(context,contentType,isInput));
            }
            else{
                copyBuilder.add("$L.$L",
                        getComplexTypeConverterVarName(context,new MapperFieldEntry(isInput,buildConversionParams(contentType)),false),
                        isInput?CONVERT_TO_DOC_FCT_NAME:CONVERT_TO_OUTPUT_FCT_NAME);
            }
        }
        copyBuilder.add(suffix.build());
        return copyBuilder.build();
    }


    private CodeBlock getEnumConverterFunction(Context context,ClassInfo enumModel,boolean isInput) {
        MapperEnumEntry entry = new MapperEnumEntry(isInput,enumModel);
        return context.converterEnumMap.computeIfAbsent(entry,elt->buildEnumMapperFunction(context,elt));
    }

    private CodeBlock buildEnumMapperFunction(Context context,MapperEnumEntry mapperEnumEntry) {
        ConversionMainParams params= buildConversionParams(mapperEnumEntry.model);
        String fctName = "mapEnum"+(mapperEnumEntry.isInput?"Input":"Output");
        ClassName inputClassName=mapperEnumEntry.isInput?params.dtoModel.getClassName():params.origClassInfo.getClassName();
        ClassName outputClassName=mapperEnumEntry.isInput?params.origClassInfo.getClassName():params.dtoModel.getClassName();
        context.codeTypeBuilder.addMethod(MethodSpec.methodBuilder(fctName)
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


    private void generateCopy(Context context,ClassInfo origClassInfo, DtoFieldMappingInfo annot, FieldInfo dtoFieldInfo, boolean isInput, MethodSpec.Builder mapperMethod) {
        CodeBlock.Builder copyBuilder = CodeBlock.builder();
        CodeBlock generatedCopyMap = generateCopyWithMap(context,origClassInfo,annot,dtoFieldInfo,isInput);
        if(isInput){
            copyBuilder.add(
                    buildOrigStructSetter(dtoFieldInfo,origClassInfo,
                            getOrigStructFieldName(origClassInfo, annot, dtoFieldInfo),
                            CodeBlock.of("doc"),
                            generatedCopyMap)
            );
        }
        else{
            copyBuilder.add("output.$L(",getDtoModelSetter(annot, dtoFieldInfo));
            copyBuilder.add(generatedCopyMap);
            copyBuilder.add(")");
        }
        copyBuilder.add(";\n");
        mapperMethod.addCode(copyBuilder.build());
    }

    private void generateSimpleMap(Context context, DtoFieldMappingInfo annot, FieldInfo dtoModelfield, boolean isInput, MethodSpec.Builder mapperMethod) {
        UnwrappedContext unwrappedContext = getEffectiveUnwrappedContext(context,annot,dtoModelfield,isInput,mapperMethod);
        generateCopy(context,unwrappedContext.classInfo,annot,dtoModelfield,isInput,unwrappedContext.methodBuilder);
    }


    private UnwrappedContext getEffectiveUnwrappedContext(Context context, DtoFieldMappingInfo annot, FieldInfo field, boolean isInput, MethodSpec.Builder mapperMethod){
        String[] parts = annot.ruleValue().split("\\.");
        String currKey=(isInput?"INPUT":"OUTPUT")+".";
        UnwrappedContext unwrappedContext = new UnwrappedContext(currKey,isInput,context.params.origClassInfo,mapperMethod);
        //Go up to the n-1 element
        for(int partPos=0;partPos<(parts.length-1);++partPos){
            String currFieldName = parts[partPos];
            currKey+=currFieldName+".";
            if(!context.unwrappedVariableMap.containsKey(currKey)){

                CouchbaseDocumentStructureReflection reflection = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(unwrappedContext.classInfo);
                CouchbaseDocumentFieldReflection docField = reflection.getFieldByPropertyName(parts[partPos]);
                Preconditions.checkArgument(!docField.isCollection(),"The field %s is a collection",docField.getField().getFullName());
                Preconditions.checkArgument(!docField.isMap(),"The field %s is a map",docField.getField().getFullName());
                ClassInfo unwrappedDocClassInfo = (ClassInfo)docField.getEffectiveTypeInfo().getMainType();
                Preconditions.checkArgument(CouchbaseDocumentStructureReflection.isReflexible(unwrappedDocClassInfo),"The field %s isn't reflexible",docField.getField().getFullName());

                MethodSpec.Builder unwrappedMappingMethod;
                if(isInput) {
                    String fctName = UNWRAP_CONVERT_TO_DOC_FCT_NAME + unwrappedDocClassInfo.getSimpleName();

                    unwrappedMappingMethod = MethodSpec.methodBuilder(fctName)
                            .addModifiers(Modifier.PRIVATE)
                            .addParameter(context.params.dtoModel.getClassName(), "input")
                            .returns(unwrappedDocClassInfo.getClassName())
                            .addStatement("if(input == null) return null")
                            .addStatement("$T doc = new $T()", unwrappedDocClassInfo.getClassName(), unwrappedDocClassInfo.getClassName());

                    unwrappedContext.methodBuilder.addCode(buildOrigStructSetter(field,unwrappedContext.classInfo,parts[partPos],CodeBlock.of("doc"),CodeBlock.of("$L(input)",fctName)));
                    unwrappedContext.methodBuilder.addCode(";\n");
                }
                else{
                    String fctName = UNWRAP_MAP_FROM_DOC_FCT_NAME + unwrappedDocClassInfo.getSimpleName();
                    unwrappedMappingMethod = MethodSpec.methodBuilder(fctName)
                            .addModifiers(Modifier.PRIVATE)
                            .addParameter(unwrappedDocClassInfo.getClassName(), "doc")
                            .addParameter(context.params.dtoModel.getClassName(), "output")
                            .returns(context.params.dtoModel.getClassName())
                            .addStatement("if(doc == null) return output");

                    unwrappedContext.methodBuilder.addStatement("$L($L,output)",fctName,buildOrigStructGetter(unwrappedContext.classInfo,parts[partPos],CodeBlock.of("doc")));
                }
                UnwrappedContext newSubContext=new UnwrappedContext(currKey,isInput,unwrappedDocClassInfo,unwrappedMappingMethod);
                context.unwrappedVariableMap.put(currKey,newSubContext);
                unwrappedContext = newSubContext;
            }
            else{
                unwrappedContext = context.unwrappedVariableMap.get(currKey);
            }
        }

        return unwrappedContext;
    }


    private CodeBlock getComplexTypeConverterVarName(Context context,MapperFieldEntry fieldToMap,boolean isMapper){
        return context.converterFieldMap.computeIfAbsent(fieldToMap, entry->generateDynamicField(context,entry,isMapper));
    }

    private CodeBlock generateDynamicField(Context context,MapperFieldEntry entry,boolean isMapper){
        String suffix = isMapper?"Mapper":"Converter";
        String fieldName = StringUtils.lowerCaseFirst(entry.params.dtoModel.getSimpleName())+(entry.isInput?"Input":"Output")+suffix;
        context.codeTypeBuilder.addField(FieldSpec.builder(
                ParameterizedTypeName.get(
                        isMapper?ClassName.get(entry.isInput?IDtoInputMapper.class:IDtoOutputMapper.class):
                                ClassName.get(entry.isInput?IDtoInputConverter.class:IDtoOutputConverter.class),
                        entry.params.origClassInfo.getClassName(),entry.params.dtoModel.getClassName()),
                fieldName,Modifier.PRIVATE).build());
        //TODO manage version
        context.factoryAwareMethodBuilder.addStatement(
                "$L = factory."+(entry.isInput?"getDtoInput":"getDtoOutput")+suffix+"($T.class,$T.class)",fieldName,entry.params.origClassInfo.getClassName(),entry.params.dtoModel.getClassName());
        return CodeBlock.builder().add("$L",fieldName).build();
    }

    public List<JavaFile> getFiles() {
        return files;
    }

    private static final class ConversionMainParams {
        private final DtoModelMappingInfo mappingInfoAnnot;
        private final ClassInfo dtoModel;
        private final ClassInfo origClassInfo;

        public ConversionMainParams(DtoModelMappingInfo mappingInfoAnnot, ClassInfo dtoModel, ClassInfo origClassInfo) {
            this.mappingInfoAnnot = mappingInfoAnnot;
            this.dtoModel = dtoModel;
            this.origClassInfo = origClassInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConversionMainParams that = (ConversionMainParams) o;

            if (!mappingInfoAnnot.equals(that.mappingInfoAnnot)) return false;
            if (!dtoModel.equals(that.dtoModel)) return false;
            return origClassInfo.equals(that.origClassInfo);
        }

        @Override
        public int hashCode() {
            int result = mappingInfoAnnot.hashCode();
            result = 31 * result + dtoModel.hashCode();
            result = 31 * result + origClassInfo.hashCode();
            return result;
        }
    }

    private static final class UnwrappedContext{
        private final String key;
        private final boolean isInput;
        private final ClassInfo classInfo;
        private final MethodSpec.Builder methodBuilder;


        public UnwrappedContext(String key,boolean isInput, ClassInfo classInfo,MethodSpec.Builder methodBuilder) {
            this.key = key;
            this.isInput = isInput;
            this.classInfo = classInfo;
            this.methodBuilder=methodBuilder;
        }
    }

    private static final class Context{
        private final ConversionMainParams params;
        private final TypeSpec.Builder codeTypeBuilder;
        private final MethodSpec.Builder factoryAwareMethodBuilder;
        private final Map<MapperFieldEntry,CodeBlock> converterFieldMap=new HashMap<>();
        private final Map<String,UnwrappedContext> unwrappedVariableMap =new HashMap<>();
        private final Map<MapperEnumEntry,CodeBlock> converterEnumMap=new HashMap<>();

        public Context(TypeSpec.Builder codeTypeBuilder, MethodSpec.Builder factoryAwareMethodBuilder,
                       ConversionMainParams params)
        {
            this.codeTypeBuilder = codeTypeBuilder;
            this.factoryAwareMethodBuilder = factoryAwareMethodBuilder;
            this.params= params;
        }
    }

    private static class Key {
        private final ClassInfo dtoClassName;

        public Key(ClassInfo dtoClassName) {
            this.dtoClassName = dtoClassName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            return dtoClassName.equals(key.dtoClassName);
        }

        @Override
        public int hashCode() {
            return dtoClassName.hashCode();
        }
    }

    private final class MapperEnumEntry {
        private final boolean isInput;
                private final ClassInfo model;

        MapperEnumEntry(boolean isInput, ClassInfo model) {
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
        private final ConversionMainParams params;

        public MapperFieldEntry(boolean isInput, ConversionMainParams params) {
            this.isInput = isInput;
            this.params = params;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MapperFieldEntry that = (MapperFieldEntry) o;

            if (isInput != that.isInput) return false;
            return params.equals(that.params);
        }

        @Override
        public int hashCode() {
            int result = (isInput ? 1 : 0);
            result = 31 * result + params.hashCode();
            return result;
        }
    }

}
