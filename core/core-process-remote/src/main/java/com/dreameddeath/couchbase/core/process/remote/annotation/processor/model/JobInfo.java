/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.couchbase.core.process.remote.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.MethodInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.dreameddeath.couchbase.core.process.remote.annotation.FieldFilteringMode;
import com.dreameddeath.couchbase.core.process.remote.annotation.Request;
import com.dreameddeath.couchbase.core.process.remote.annotation.Result;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 04/03/2016.
 */
public class JobInfo {
    private CouchbaseDocumentReflection reflectionInfo;
    private Map<String,RestModel> models=new HashMap<>();
    private Map<String,EnumModel> enums = new HashMap<>();
    private RestModel request;
    private RestModel response;

    public JobInfo(ClassInfo info){
        reflectionInfo = CouchbaseDocumentReflection.getReflectionFromClassInfo(info);
        String packageName =reflectionInfo.getClassInfo().getPackageInfo().getName()+".published";
        request=buildModel(reflectionInfo.getStructure(),packageName,new GenMode(FieldFilteringMode.STANDARD,true));
        response=buildModel(reflectionInfo.getStructure(),packageName,new GenMode(FieldFilteringMode.STANDARD,false));
    }

    public String getImportName(){
        return reflectionInfo.getClassInfo().getImportName();
    }

    public String getShortName(){
        return reflectionInfo.getClassInfo().getSimpleName();
    }

    public String buildGetterWithMapping(String varName,RestModel parent,RestModel.Field fieldInfo){
        String base = varName+".";
        if(parent.isRequest){
            if(fieldInfo.unwrapMode== RestModel.Field.UnwrapMode.UNWRAP_SOURCE){
                base = varName;
            }
            else {
                base += fieldInfo.getGetterName() + "()";
            }
        }
        else{
            if(fieldInfo.unwrapMode== RestModel.Field.UnwrapMode.UNWRAP_CHILD){
                CouchbaseDocumentFieldReflection parentRootField=fieldInfo.unwrappedModel.origStructInfo.getFieldByPropertyName((fieldInfo.unwrappedRootField.jobFieldName));
                base+=parentRootField.getGetterName()+((parentRootField.getGetter() instanceof MethodInfo)?"()":"")+".";
            }
            CouchbaseDocumentFieldReflection field = parent.origStructInfo.getFieldByPropertyName(fieldInfo.jobFieldName);
            base+=field.getGetterName()+((field.getGetter() instanceof MethodInfo)?"()":"");
        }

        if(fieldInfo.typeStructure == RestModel.Field.Type.COLLECTION && fieldInfo.isComplexType){
            base+=".stream().map(this::"+((fieldInfo.isEnum)?"mapEnum":parent.getMapFctName())+").collect(Collectors.toList())";
        }
        else if(fieldInfo.typeStructure== RestModel.Field.Type.MAP && fieldInfo.isComplexType){
            base+=".entrySet().stream().map(entryElt->new SimpleImmutableEntry<>(entryElt.getKey(),"+((fieldInfo.isEnum)?"mapEnum":parent.getMapFctName())+"(entryElt.getValue()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))";
        }
        else if(fieldInfo.isComplexType){
            if(fieldInfo.isEnum){
                base="mapEnum("+base+")";
            }
            else{
                if(fieldInfo.unwrapMode== RestModel.Field.UnwrapMode.UNWRAP_SOURCE){
                    if(parent.isRequest) {
                        base = fieldInfo.unwrappedModel.getMapFctName() + "(" + base + ")";
                    }
                }
                else {
                    base = fieldInfo.complexTypeModel.getMapFctName() + "(" + base + ")";
                }
            }
        }

        return base;
    }

    public String buildSetter(String varName,RestModel parent,RestModel.Field fieldInfo,String valueExpr){
        if(parent.isRequest){
            CouchbaseDocumentFieldReflection field = parent.origStructInfo.getFieldByPropertyName(fieldInfo.jobFieldName);
            if(field==null){
                throw new RuntimeException("Cannot find field "+fieldInfo.jobFieldName+" for setter in struct "+parent.origStructInfo.getName());
            }
            if(field.getSetter() instanceof MethodInfo){
                return varName+"."+field.getSetterName()+"("+valueExpr+")";
            }
            else{
                return varName+"."+field.getSetterName()+"="+valueExpr;
            }
        }
        else{
            if(fieldInfo.unwrapMode== RestModel.Field.UnwrapMode.UNWRAP_SOURCE){
                return fieldInfo.complexTypeModel.getMapFctName()+"("+varName+","+valueExpr+")";
            }
            else {
                return varName+"."+ fieldInfo.getSetterName() + "(" + valueExpr + ")";
            }
        }
    }

    public Set<String> getServiceImports(){
        Set<String> result = new TreeSet<>();
        result.add(this.getImportName());
        for(RestModel model:models.values()){
            result.addAll(model.getImports());
            result.add(model.getOrigClassImportName());
            if(!model.isUnwrapped) {
                result.add(model.getImportName());
            }
        }
        for(EnumModel enumModel:enums.values()){
            result.add(enumModel.getImportName());
            result.add(enumModel.origClassInfo.getImportName());
        }
        return result;
    }


    public Map<String,RestModel> getModels() {
        return models;
    }

    public RestModel getRequest() {
        return request;
    }

    public RestModel getResponse() {
        return response;
    }


    private  RestModel buildModel(CouchbaseDocumentStructureReflection clazz,String packageName,GenMode mode){
        RestModel resultModel = new RestModel();
        resultModel.shortName = clazz.getSimpleName().replaceAll("\\$","")
                +(!mode.forUnwrap?((mode.isRequest)?"Request":"Response"):"");
        resultModel.packageName = packageName;
        resultModel.origStructInfo=clazz;
        resultModel.origClassInfo = clazz.getClassInfo();
        resultModel.isRequest=mode.isRequest;
        resultModel.isUnwrapped=mode.forUnwrap;
        resultModel.unwrappedRootModels.addAll(mode.unwrapSourceRestModels);
        models.put(buildKey(clazz.getClassInfo(), mode), resultModel);

        for(CouchbaseDocumentFieldReflection jobField:clazz.getFields()){
            Annotation resultAnnot = jobField.getField().getAnnotation((Class<? extends Annotation>)(mode.isRequest?Request.class:Result.class));
            if(getUnwrap(resultAnnot)){
                if(jobField.isCollection() || jobField.isMap()){
                    throw new RuntimeException("The field "+jobField.getName()+" of class "+ clazz.getName()+ "Shouldn't be a map or a collection");
                }
                AbstractClassInfo classInfo = jobField.getEffectiveTypeInfo().getMainType();
                if(classInfo.isBaseType() || classInfo.isInterface() || !(classInfo instanceof ClassInfo) || !CouchbaseDocumentStructureReflection.isReflexible((ClassInfo)classInfo)){
                    throw new RuntimeException("The type <"+classInfo.getFullName()+" of field "+jobField.getName()+" of class "+ clazz.getName()+ " isn't of the right type");
                }
                RestModel subModel = buildModel(CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo)classInfo),packageName,new GenMode(mode,getMode(resultAnnot)).unwrapFor(mode,resultModel));

                RestModel.Field rootUnwrappedField = buildField(resultModel,jobField, getFieldName(resultAnnot),new GenMode(mode,getMode(resultAnnot)).unwrapFor(mode,resultModel));
                rootUnwrappedField.unwrapMode= RestModel.Field.UnwrapMode.UNWRAP_SOURCE;
                rootUnwrappedField.unwrappedModel=subModel;
                resultModel.addFieldForMapping(rootUnwrappedField);
                resultModel.addFieldsForClass(subModel.getFieldsForClass().stream().map(fld->new RestModel.Field(fld, RestModel.Field.UnwrapMode.UNWRAP_CHILD,resultModel,rootUnwrappedField)).collect(Collectors.toList()));
            }
            else if(resultAnnot!=null || mode.fieldMode==FieldFilteringMode.FULL) {
                RestModel.Field field = buildField(resultModel,jobField, getFieldName(resultAnnot),new GenMode(mode,getMode(resultAnnot)));
                resultModel.addFieldForMapping(field);
                resultModel.addFieldForClass(field);
            }
        }
        return resultModel;
    }

    private RestModel.Field buildField(RestModel parent, CouchbaseDocumentFieldReflection jobField, String annotatedName, GenMode mode){
        RestModel.Field resultField = new RestModel.Field();
        resultField.variableName = jobField.getField().getName();
        resultField.jobFieldName = jobField.getName();
        resultField.parentModel = parent;
        resultField.jsonName = StringUtils.isNotEmpty(annotatedName)?annotatedName:jobField.getName();
        if(jobField.isCollection()){
            resultField.typeStructure = RestModel.Field.Type.COLLECTION;
            resultField.typeImports.add(jobField.getEffectiveTypeInfo().getMainType().getImportName());
            buildFieldType(parent,resultField,jobField.getCollectionElementTypeInfo().getMainType(),
                            jobField.getEffectiveTypeInfo().getMainType().getSimpleName()+"<%s>",mode);
        }
        else if(jobField.isMap()){
            resultField.typeStructure = RestModel.Field.Type.MAP;
            resultField.typeImports.add(jobField.getMapKeyTypeInfo().getMainType().getImportName());
            resultField.typeImports.add(jobField.getEffectiveTypeInfo().getMainType().getImportName());
            buildFieldType(parent,resultField,jobField.getMapValueTypeInfo().getMainType(),
                            jobField.getEffectiveTypeInfo().getMainType().getSimpleName()
                            +"<"+jobField.getMapKeyTypeInfo().getMainType().getSimpleName()+
                            ",%s>",mode);

        }
        else {
            resultField.typeStructure = RestModel.Field.Type.SIMPLE;
            buildFieldType(parent,resultField,jobField.getEffectiveTypeInfo().getMainType(),"%s",mode);
        }
        return resultField;
    }

    private void buildFieldType(RestModel parent,RestModel.Field field,AbstractClassInfo classInfo,String declareTypePattern,GenMode mode){
        if(classInfo.isBaseType()){
            field.type = String.format(declareTypePattern,classInfo.getSimpleName());
            field.typeImports.add(classInfo.getImportName());
            field.isComplexType=false;
            field.isEnum=false;
        }
        else if(((ClassInfo)classInfo).isEnum()){
            if(!enums.containsKey(classInfo.getFullName())){
                buildEnum((ClassInfo)classInfo,parent.getPackageName());
            }
            EnumModel enumModel=enums.get(classInfo.getFullName());
            if(parent.isRequest()){
                enumModel.forRequest=true;
            }
            else{
                enumModel.forResponse=true;
            }

            field.type = String.format(declareTypePattern,enumModel.shortName);
            field.typeImports.add(enumModel.getImportName());
            field.isComplexType=true;
            field.isEnum=true;
        }
        else if(CouchbaseDocumentStructureReflection.isReflexible((ClassInfo)classInfo)){
            RestModel model;
            if(!models.containsKey(buildKey(classInfo,mode))){
                model=buildModel(CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo)classInfo),parent.getPackageName(),mode);
            }
            else {
                model = models.get(buildKey(classInfo,mode));
            }
            field.isComplexType=true;
            field.isEnum=false;
            field.type = String.format(declareTypePattern,model.getShortName());
            field.typeImports.add(model.getImportName());
            field.complexTypeModel=model;
            //return  isSimpleName?model.getShortName():model.getImportName();
        }
        else{
            throw new RuntimeException("Cannot manage field type "+classInfo.getFullName());
        }
    }

    private EnumModel buildEnum(ClassInfo classInfo,String packageName) {
        EnumModel result = new EnumModel();
        result.shortName = classInfo.getSimpleName().replaceAll("\\$","")+"Published";
        result.packageName = packageName;
        result.origClassInfo = classInfo;

        enums.put(classInfo.getFullName(),result);

        for(FieldInfo fieldInfo:classInfo.getDeclaredFields()){
            result.values.add(fieldInfo.getName());
        }
        return result;
    }

    private String buildKey(AbstractClassInfo clazz,GenMode mode){
        return clazz.getName()+"|"+((mode.isRequest)?"ForRequest":"ForResponse")
                +((mode.forUnwrap)?"|UnwrappedFor"+mode.unwrapSourceRestModels.stream().map(RestModel::getShortName).map(StringUtils::capitalizeFirst).collect(Collectors.joining("And")):"");
    }


    private <T extends Annotation> boolean getUnwrap(T annot){
        if(annot==null) return false;
        return (annot instanceof Request)?((Request)annot).unwrap():((Result)annot).unwrap();
    }

    private <T extends Annotation> String getFieldName(T annot){
        if(annot==null) return null;
        return (annot instanceof Request)?((Request)annot).value():((Result)annot).value();
    }

    private <T extends Annotation> FieldFilteringMode getMode(T annot){
        if(annot==null) return FieldFilteringMode.INHERIT;
        return (annot instanceof Request)?((Request)annot).mode():((Result)annot).mode();
    }

    public Map<String, EnumModel> getEnums() {
        return enums;
    }


    private static class GenMode{
        private final FieldFilteringMode fieldMode;
        private boolean isRequest;
        private boolean forUnwrap=false;
        private List<RestModel> unwrapSourceRestModels=new ArrayList<>();

        public GenMode(FieldFilteringMode mode,boolean isRequest){
            this.fieldMode = mode;
            this.isRequest = isRequest;
        }

        public GenMode(GenMode parent, FieldFilteringMode mode){
            this.isRequest=parent.isRequest;
            if(mode==FieldFilteringMode.INHERIT){
                this.fieldMode = mode;
            }
            else{
                this.fieldMode=mode;
            }
        }

        public GenMode unwrapFor(GenMode parentMode,RestModel parent) {
            this.forUnwrap=true;
            this.unwrapSourceRestModels.addAll(parentMode.unwrapSourceRestModels);
            this.unwrapSourceRestModels.add(parent);
            return this;
        }
    }

}
