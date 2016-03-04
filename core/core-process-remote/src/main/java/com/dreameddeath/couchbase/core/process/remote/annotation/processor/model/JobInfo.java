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

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.MethodInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.couchbase.core.process.remote.annotation.Request;
import com.dreameddeath.couchbase.core.process.remote.annotation.Result;

/**
 * Created by Christophe Jeunesse on 04/03/2016.
 */
public class JobInfo {
    private CouchbaseDocumentReflection reflectionInfo;
    private RestModel response;
    private RestModel request;

    public JobInfo(ClassInfo info){
        reflectionInfo = CouchbaseDocumentReflection.getReflectionFromClassInfo(info);
        response = buildResponseModel();
        request = buildRequestModel();
    }

    private RestModel.Field buildField(CouchbaseDocumentFieldReflection jobField,String annotatedName){
        RestModel.Field resultField = new RestModel.Field();
        resultField.variableName = jobField.getField().getName();
        resultField.jobFieldName = jobField.getName();
        resultField.jsonName = StringUtils.isNotEmpty(annotatedName)?annotatedName:jobField.getName();
        resultField.type = jobField.getEffectiveTypeInfo().getMainType().getSimpleName();
        resultField.typeImport = jobField.getEffectiveTypeInfo().getMainType().getImportName();
        return resultField;
    }

    public RestModel getResponse() {
        return response;
    }

    public RestModel getRequest() {
        return request;
    }

    public String getImportName(){
        return reflectionInfo.getClassInfo().getImportName();
    }

    public String getShortName(){
        return reflectionInfo.getClassInfo().getSimpleName();
    }

    private RestModel buildResponseModel(){
        RestModel resultModel = new RestModel();
        resultModel.shortName = reflectionInfo.getSimpleName()+"Response";
        resultModel.packageName = reflectionInfo.getClassInfo().getPackageInfo().getName()+".published";

        for(CouchbaseDocumentFieldReflection jobField:reflectionInfo.getStructure().getFields()){
            Result resultAnnot = jobField.getField().getAnnotation(Result.class);
            if(resultAnnot!=null){
               resultModel.addField(buildField(jobField,resultAnnot.value()));
            }
        }
        return resultModel;
    }

    private RestModel buildRequestModel(){
        RestModel requestModel = new RestModel();
        requestModel.shortName = reflectionInfo.getSimpleName()+"Request";
        requestModel.packageName = reflectionInfo.getClassInfo().getPackageInfo().getName()+".published";

        for(CouchbaseDocumentFieldReflection jobField:reflectionInfo.getStructure().getFields()){
            Request requestAnnot = jobField.getField().getAnnotation(Request.class);
            if(requestAnnot!=null){
                requestModel.addField(buildField(jobField,requestAnnot.value()));
            }
        }
        return requestModel;
    }

    public String buildGetter(String varName,RestModel.Field fieldInfo){
        CouchbaseDocumentFieldReflection field = reflectionInfo.getStructure().getFieldByPropertyName(fieldInfo.jobFieldName);
        return varName+"."+field.getGetterName()+((field.getGetter() instanceof MethodInfo)?"()":"");
    }

    public String buildSetter(RestModel.Field fieldInfo,String valueExpr){
        CouchbaseDocumentFieldReflection field = reflectionInfo.getStructure().getFieldByPropertyName(fieldInfo.jobFieldName);
        if(field.getSetter() instanceof MethodInfo){
            return field.getSetterName()+"("+valueExpr+")";
        }
        else{
            return field.getSetterName()+"="+valueExpr;
        }
    }
}
