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
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.dreameddeath.couchbase.core.process.remote.annotation.RemoteServiceInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 04/03/2016.
 */
public class RestModel {
    private Map<String,Field> stringFieldForMapping = new HashMap<>();
    private Map<String,Field> stringFieldForClassMap = new HashMap<>();

    public CouchbaseDocumentStructureReflection origStructInfo;
    public ClassInfo origClassInfo;
    public String shortName;
    public String packageName;
    public boolean isRequest;
    public boolean isUnwrapped;
    public List<RestModel> unwrappedRootModels=new ArrayList<>();
    public boolean isRoot;
    public boolean hasRemoteInfo=false;
    public String remoteDomain;
    public String remoteName;
    public String remoteVersion;
    public List<RestModel> listChildClasses=new ArrayList<>();
    public RestModel parentModel=null;

    public String getShortName() {
        return shortName;
    }

    public String getUnwrappedSourceShortName(){ return unwrappedRootModels.get(0).shortName;}

    public String getPackageName() {
        return packageName;
    }

    public String getImportName(){
        return packageName+"."+shortName;
    }

    public Set<String> getImports(){
        Set<String> result = new HashSet<>();
        if(hasRemoteInfo){
            result.add(ClassInfo.getClassInfo(RemoteServiceInfo.class).getImportName());
        }
        if(hasChildClasses()){
            result.add(ClassInfo.getClassInfo(JsonSubTypes.class).getImportName());
            result.add(ClassInfo.getClassInfo(JsonTypeInfo.class).getImportName());
            for(RestModel childModel:listChildClasses){
                result.add(childModel.getImportName());
            }
        }

        if(hasParentModel()){
            result.add(parentModel.getImportName());
        }
        for(Field field: stringFieldForClassMap.values()){
            result.addAll(field.typeImports);
        }
        return result;
    }

    public Collection<Field> getFieldsForMapping(){
        return stringFieldForMapping.values();
    }
    public Collection<Field> getFieldsForClass(){
        return stringFieldForClassMap.values();
    }

    public void addFieldForClass(Field field){
        stringFieldForClassMap.put(field.jobFieldName,field);
    }

    public void addFieldsForClass(Collection<Field> fields) {
        for(Field field:fields){
            if(!stringFieldForClassMap.containsKey(field.jobFieldName)){
                addFieldForClass(field);
            }
        }
    }


    public void addFieldForMapping(Field field){
        stringFieldForMapping.put(field.jobFieldName,field);
    }

    public void addFieldsForMapping(Collection<Field> fields) {
        for(Field field:fields){
            if(!stringFieldForMapping.containsKey(field.jobFieldName)){
                addFieldForMapping(field);
            }
        }
    }

    public String getMapFctName(){
        return isRequest?
                "mapFromRequest"+
                        (isUnwrapped?"For"+unwrappedRootModels.stream().map(RestModel::getShortName).map(StringUtils::capitalizeFirst).collect(Collectors.joining("And"))
                                :"")
                :"mapToResponse";
    }

    public boolean hasChildClasses(){
        return (listChildClasses!=null) && (listChildClasses.size()>0);
    }

    public List<RestModel> getListChildClasses() {
        return listChildClasses;
    }

    public String getOrigClassSimpleName(){
        return origClassInfo.getSimpleName();
    }

    public String getOrigClassImportName(){
        return origClassInfo.getImportName();
    }

    public boolean isRequest() {
        return isRequest;
    }

    public boolean isUnwrapped() {
        return isUnwrapped;
    }


    public boolean hasRemoteInfo() {
        return hasRemoteInfo;
    }

    public String getRemoteDomain() {
        return remoteDomain;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public String getRemoteVersion() {
        return remoteVersion;
    }

    public String getSubClassTypeName(){
        return origStructInfo.getEntityModelId().getName();
    }

    public List<RestModel> getFirstLevelChilds(){
        //final EntityDef currentEntityDef = EntityDef.build(this.origStructInfo);
        return listChildClasses.stream()
                .filter(
                        elt->
                                elt.origStructInfo.getSuperclassReflexion().getClassInfo().equals(this.origStructInfo.getClassInfo())
                )
                .collect(Collectors.toList());
    }

    public boolean hasParentModel(){
        return parentModel!=null;
    }

    public String getParentModelShortName(){
        return parentModel.getShortName();
    }

    public boolean isAbstract(){
        return origClassInfo.isAbstract();
    }

    public static class Field{
        public List<String> typeImports = new ArrayList<>();
        public RestModel parentModel;
        public String jobFieldName;
        public String variableName;
        public String jsonName;
        public String type;
        public Type typeStructure;
        public RestModel complexTypeModel;
        public boolean isComplexType;
        public boolean isEnum;
        public UnwrapMode unwrapMode =UnwrapMode.NONE;
        public RestModel unwrappedModel=null;
        public RestModel.Field unwrappedRootField =null;

        public Field(){}

        public Field(Field source,UnwrapMode mode,RestModel sourceModel,Field rootField){
            jobFieldName=source.jobFieldName;
            variableName=source.variableName;
            jsonName=source.jsonName;
            type=source.type;
            typeImports.addAll(source.typeImports);
            typeStructure=source.typeStructure;
            isComplexType=source.isComplexType;
            isEnum=source.isEnum;
            unwrapMode=mode;
            unwrappedModel=sourceModel;
            unwrappedRootField=rootField;
        }

        public String buildGetter(String variableName){
            return variableName+"."+getGetterName()+"()";
        }

        public String getGetterName(){
            return "get"+variableName.substring(0,1).toUpperCase()+variableName.substring(1);
        }

        public String getSetterName(){
            return "set"+variableName.substring(0,1).toUpperCase()+variableName.substring(1);
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

        public enum Type{
            SIMPLE,
            COLLECTION,
            MAP
        }

        public enum UnwrapMode{
            NONE,
            UNWRAP_SOURCE,
            UNWRAP_CHILD
        }

    }
}
