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

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.dto.annotation.processor.InputDtoField;
import com.dreameddeath.core.model.dto.annotation.processor.OutputDtoField;
import com.dreameddeath.core.model.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.squareup.javapoet.ClassName;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ceaj8230 on 10/03/2017.
 */
public class DtoModelField {
    public List<ClassName> classNamesImports = new ArrayList<>();
    public List<String> typeImports = new ArrayList<>();
    public DtoModel parentModel;
    public String jobFieldName;
    public String variableName;
    public String jsonName;
    public String type;
    public Type typeStructure;
    public DtoModel complexTypeModel;
    public boolean isComplexType;
    public boolean isEnum;
    public UnwrapMode unwrapMode = UnwrapMode.NONE;
    public DtoModel unwrappedModel = null;
    public DtoModelField unwrappedRootField = null;
    public EnumModel enumModel;

    public DtoModelField() {
    }

    public DtoModelField(DtoModelField source, UnwrapMode mode, DtoModel sourceModel, DtoModelField rootField) {
        jobFieldName = source.jobFieldName;
        variableName = source.variableName;
        jsonName = source.jsonName;
        type = source.type;
        typeImports.addAll(source.typeImports);
        typeStructure = source.typeStructure;
        isComplexType = source.isComplexType;
        isEnum = source.isEnum;
        unwrapMode = mode;
        unwrappedModel = sourceModel;
        unwrappedRootField = rootField;
    }

    public String buildGetter(String variableName) {
        return variableName + "." + getGetterName() + "()";
    }

    public String getGetterName() {
        return "get" + variableName.substring(0, 1).toUpperCase() + variableName.substring(1);
    }

    public String getSetterName() {
        return "set" + variableName.substring(0, 1).toUpperCase() + variableName.substring(1);
    }

    public String getInitValue() {
        if (typeStructure == Type.COLLECTION) {
            return "= new ArrayList<>();";
        } else if (typeStructure == Type.MAP) {
            return "= new HashMap<>();";
        }
        return "";
    }

    public String getJobFieldName() {
        return jobFieldName;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getJsonName() {
        return jsonName;
    }

    public String getType() {
        return type;
    }

    public List<String> getTypeImports() {
        return typeImports;
    }

    public void setTypeStructure(Type typeStructure) {
        if (typeStructure == Type.COLLECTION) {
            typeImports.add("java.util.ArrayList");
        } else if (typeStructure == Type.MAP) {
            typeImports.add("java.util.HashMap");
        }
        this.typeStructure = typeStructure;
    }

    public enum Type {
        SIMPLE,
        COLLECTION,
        MAP
    }

    public enum UnwrapMode {
        NONE,
        UNWRAP_SOURCE,
        UNWRAP_CHILD
    }


    public static DtoModelField buildField(DtoModel parent, CouchbaseDocumentFieldReflection jobField, String annotatedName, ConverterServiceInfo.GenMode mode){
        DtoModelField resultField = new DtoModelField();
        resultField.variableName = jobField.getField().getName();
        resultField.jobFieldName = jobField.getName();
        resultField.parentModel = parent;
        resultField.jsonName = StringUtils.isNotEmpty(annotatedName)?annotatedName:jobField.getName();
        if(jobField.isCollection()){
            resultField.setTypeStructure(DtoModelField.Type.COLLECTION);
            resultField.typeImports.add(jobField.getEffectiveTypeInfo().getMainType().getImportName());
            resultField.buildFieldType(jobField.getCollectionElementTypeInfo().getMainType(),
                    jobField.getEffectiveTypeInfo().getMainType().getSimpleName()+"<%s>",mode);
        }
        else if(jobField.isMap()){
            resultField.setTypeStructure(DtoModelField.Type.MAP);
            resultField.typeImports.add(jobField.getMapKeyTypeInfo().getMainType().getImportName());
            resultField.typeImports.add(jobField.getEffectiveTypeInfo().getMainType().getImportName());
            resultField.buildFieldType(jobField.getMapValueTypeInfo().getMainType(),
                    jobField.getEffectiveTypeInfo().getMainType().getSimpleName()
                            +"<"+jobField.getMapKeyTypeInfo().getMainType().getSimpleName()+
                            ",%s>",mode);

        }
        else {
            resultField.setTypeStructure(DtoModelField.Type.SIMPLE);
            resultField.buildFieldType(jobField.getEffectiveTypeInfo().getMainType(),"%s",mode);
        }
        return resultField;
    }

    private void buildFieldType(AbstractClassInfo classInfo, String declareTypePattern, ConverterServiceInfo.GenMode mode){
        if(classInfo.isBaseType()){
            this.type = String.format(declareTypePattern,classInfo.getSimpleName());
            this.typeImports.add(classInfo.getImportName());
            this.isComplexType=false;
            this.isEnum=false;
        }
        else if(((ClassInfo)classInfo).isEnum()){
            if(!this.parentModel.context.containsEnum(classInfo.getFullName())){
                EnumModel.buildEnum(this.parentModel.context,(ClassInfo)classInfo,this.parentModel.getPackageName());
            }
            EnumModel enumModel=this.parentModel.context.getEnum(classInfo.getFullName());
            if(this.parentModel.isInput()){
                enumModel.forInput=true;
            }
            else{
                enumModel.forOutput =true;
            }

            this.type = String.format(declareTypePattern,enumModel.shortName);
            this.typeImports.add(enumModel.getImportName());
            this.isComplexType=true;
            this.enumModel = enumModel;
            this.isEnum=true;
        }
        else if(CouchbaseDocumentStructureReflection.isReflexible((ClassInfo)classInfo)){
            DtoModel model;
            model=DtoModel.getOrBuildModel(parentModel.context,CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo)classInfo),parentModel.getPackageName(),mode,parentModel.converterServiceInfo);
            this.isComplexType=true;
            this.isEnum=false;
            this.type = String.format(declareTypePattern,model.getShortName());
            this.typeImports.add(model.getImportName());
            this.complexTypeModel=model;
            //return  isSimpleName?model.getShortName():model.getImportName();
        }
        else{
            throw new RuntimeException("Cannot manage field type "+classInfo.getFullName());
        }
    }


    public static <T extends Annotation> String getFieldName(T annot){
        if(annot==null) return null;
        return (annot instanceof InputDtoField)?((InputDtoField)annot).value():((OutputDtoField)annot).value();
    }


}
