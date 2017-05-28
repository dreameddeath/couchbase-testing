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
import com.dreameddeath.core.model.dto.annotation.processor.ConverterGeneratorContext;
import com.dreameddeath.core.model.dto.annotation.processor.FieldFilteringMode;
import com.dreameddeath.core.model.dto.annotation.processor.InputDtoField;
import com.dreameddeath.core.model.dto.annotation.processor.OutputDtoField;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.squareup.javapoet.ClassName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 04/03/2016.
 */
public class DtoModel {
    private static final Logger LOG = LoggerFactory.getLogger(DtoModel.class);
    private Map<String,DtoModelField> stringFieldForMapping = new HashMap<>();
    private Map<String,DtoModelField> stringFieldForClassMap = new HashMap<>();

    public CouchbaseDocumentStructureReflection origStructInfo;
    public ConverterServiceReferenceInfo converterServiceInfo;
    public ConverterGeneratorContext context;
    public ClassInfo origClassInfo;
    public String packageName;
    public String shortName;
    public String version;
    public boolean isInput;
    public boolean isUnwrapped;
    public List<DtoModel> unwrappedRootModels=new ArrayList<>();
    public boolean isRoot;
    public boolean hasRemoteInfo=false;
    public String remoteDomain;
    public String remoteName;
    public String remoteVersion;
    public List<DtoModel> listChildClasses=new ArrayList<>();
    public DtoModel parentModel=null;

    public String getShortName() {
        return shortName;
    }

    public String getUnwrappedSourceShortName(){ return unwrappedRootModels.get(0).shortName;}

    public DtoModel getUnwrappedSourceModel(){ return unwrappedRootModels.get(0);}


    public String getPackageName() {
        return packageName;
    }

    public String getVersion(){ return version; }

    public String getImportName(){
        return packageName+"."+shortName;
    }

    public Set<String> getImports(){
        Set<String> result = new HashSet<>();
        if(hasChildClasses()){
            result.add(ClassInfo.getClassInfo(JsonSubTypes.class).getImportName());
            result.add(ClassInfo.getClassInfo(JsonTypeInfo.class).getImportName());
            for(DtoModel childModel:listChildClasses){
                result.add(childModel.getImportName());
            }
        }

        if(hasParentModel()){
            result.add(parentModel.getImportName());
        }
        for(DtoModelField field: stringFieldForClassMap.values()){
            result.addAll(field.typeImports);
        }
        return result;
    }

    public Collection<DtoModelField> getFieldsForMapping(){
        return stringFieldForMapping.values();
    }
    public Collection<DtoModelField> getFieldsForClass(){
        return stringFieldForClassMap.values();
    }

    public void addFieldForClass(DtoModelField field){
        stringFieldForClassMap.put(field.jobFieldName,field);
    }

    public void addFieldsForClass(Collection<DtoModelField> fields) {
        for(DtoModelField field:fields){
            if(!stringFieldForClassMap.containsKey(field.jobFieldName)){
                addFieldForClass(field);
            }
        }
    }

    public void addFieldForMapping(DtoModelField field){
        stringFieldForMapping.put(field.jobFieldName,field);
    }

    public void addFieldsForMapping(Collection<DtoModelField> fields) {
        for(DtoModelField field:fields){
            if(!stringFieldForMapping.containsKey(field.jobFieldName)){
                addFieldForMapping(field);
            }
        }
    }

    public boolean hasChildClasses(){
        return (listChildClasses!=null) && (listChildClasses.size()>0);
    }

    public List<DtoModel> getListChildClasses() {
        return listChildClasses;
    }

    public ClassInfo getOrigClassInfo(){
        return origClassInfo;
    }

    public String getOrigClassSimpleName(){
        return origClassInfo.getSimpleName();
    }

    public String getOrigClassImportName(){
        return origClassInfo.getImportName();
    }

    public boolean isInput() {
        return isInput;
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

    public CouchbaseDocumentStructureReflection getDocumentStruct(){
        return origStructInfo;
    }

    public List<DtoModel> getFirstLevelChilds(){
        return listChildClasses.stream()
                .filter(
                        elt-> {
                            if(elt.origStructInfo==null){
                                LOG.error("empty orig struct for elt "+elt.getClassName());
                            }
                            if(elt.origStructInfo.getSuperclassReflexion()==null){
                                LOG.error("empty superclass for orig struct for elt "+elt.getClassName()+" / "+elt.origStructInfo.getClassInfo().getClassName());
                            }
                            return elt.origStructInfo.getSuperclassReflexion().getClassInfo().equals(this.origStructInfo.getClassInfo());
                        }
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


    public boolean canConvert(){
        return !isAbstract() && !isUnwrapped();
    }


    public static String buildKey(AbstractClassInfo clazz,ConverterServiceInfo.GenMode mode){
        return clazz.getName()+"|"+((mode.isInput())?"ForInput":"ForOutput")
                +((mode.isForUnwrap())?"|UnwrappedFor"+mode.getUnwrapSourceDtoModels().stream().map(DtoModel::getShortName).map(StringUtils::capitalizeFirst).collect(Collectors.joining("And")):"");
    }

    public static DtoModel getOrBuildModel(ConverterGeneratorContext context, CouchbaseDocumentStructureReflection clazz, String packageName, ConverterServiceInfo.GenMode mode, ConverterServiceReferenceInfo referenceInfo){
        String dtoUniqueKey = buildKey(clazz.getClassInfo(), mode);
        if(context.containsDtoModel(dtoUniqueKey)){
            return context.getDtoModelByKey(dtoUniqueKey);
        }
        DtoModel resultModel = new DtoModel();
        resultModel.context = context;
        resultModel.converterServiceInfo = referenceInfo;
        resultModel.packageName=packageName;
        resultModel.version=referenceInfo.getVersion();
        resultModel.shortName = clazz.getSimpleName().replaceAll("\\$","")
                +(!mode.isForUnwrap()?((mode.isInput())?"Input":"Output"):"");
        resultModel.origStructInfo=clazz;
        resultModel.origClassInfo = clazz.getClassInfo();
        resultModel.isInput=mode.isInput();
        resultModel.isUnwrapped=mode.isForUnwrap();
        resultModel.isRoot = mode.isRoot();
        resultModel.unwrappedRootModels.addAll(mode.getUnwrapSourceDtoModels());
        resultModel.buildChildModels(clazz, packageName, mode);
        context.putDtoModel(dtoUniqueKey, resultModel);
        if(resultModel.hasChildClasses()) {
            for (DtoModel childModel : resultModel.getFirstLevelChilds()) {
                childModel.parentModel = resultModel;
            }
        }

        for(CouchbaseDocumentFieldReflection jobField:((mode.isForChild())?clazz.getDeclaredFields():clazz.getFields())){
            Annotation resultAnnot = jobField.getField().getAnnotation((Class<? extends Annotation>)(mode.isInput()?InputDtoField.class:OutputDtoField.class));
            if(isUnwrapRequested(resultAnnot)){
                if(jobField.isCollection() || jobField.isMap()){
                    throw new RuntimeException("The field "+jobField.getName()+" of class "+ clazz.getName()+ "Shouldn't be a map or a collection");
                }
                AbstractClassInfo classInfo = jobField.getEffectiveTypeInfo().getMainType();
                if(classInfo.isBaseType() || classInfo.isInterface() || !(classInfo instanceof ClassInfo) || !CouchbaseDocumentStructureReflection.isReflexible((ClassInfo)classInfo)){
                    throw new RuntimeException("The type <"+classInfo.getFullName()+" of field "+jobField.getName()+" of class "+ clazz.getName()+ " isn't of the right type");
                }


                DtoModel subModel = getOrBuildModel(context,CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo)classInfo),packageName,new ConverterServiceInfo.GenMode(mode,getMode(resultAnnot)).unwrapFor(mode,resultModel),resultModel.converterServiceInfo);

                DtoModelField rootUnwrappedField = DtoModelField.buildField(resultModel,jobField, DtoModelField.getFieldName(resultAnnot),new ConverterServiceInfo.GenMode(mode,getMode(resultAnnot)).unwrapFor(mode,resultModel));
                rootUnwrappedField.unwrapMode= DtoModelField.UnwrapMode.UNWRAP_SOURCE;
                rootUnwrappedField.unwrappedModel=subModel;
                resultModel.addFieldForMapping(rootUnwrappedField);
                resultModel.addFieldsForClass(subModel.getFieldsForClass().stream().map(fld->new DtoModelField(fld, DtoModelField.UnwrapMode.UNWRAP_CHILD,resultModel,rootUnwrappedField)).collect(Collectors.toList()));
            }
            else if(resultAnnot!=null || mode.getFieldMode()== FieldFilteringMode.FULL) {
                DtoModelField field = DtoModelField.buildField(resultModel,jobField, DtoModelField.getFieldName(resultAnnot),new ConverterServiceInfo.GenMode(mode,getMode(resultAnnot)));
                resultModel.addFieldForMapping(field);
                resultModel.addFieldForClass(field);
            }
        }
        return resultModel;
    }

    private static  <T extends Annotation> boolean isUnwrapRequested(T annot){
        if(annot==null) return false;
        return (annot instanceof InputDtoField)?((InputDtoField)annot).unwrap():((OutputDtoField)annot).unwrap();
    }

    private static <T extends Annotation> FieldFilteringMode getMode(T annot){
        if(annot==null) return FieldFilteringMode.INHERIT;
        return (annot instanceof InputDtoField)?((InputDtoField)annot).mode():((OutputDtoField)annot).mode();
    }

    private void buildChildModels(CouchbaseDocumentStructureReflection clazz,String packageName,ConverterServiceInfo.GenMode mode) {
        List<DtoModel> resultList = new ArrayList<>();
        for(EntityDef entity:context.getEntities()){
            boolean isChild = entity.getParentEntities().contains(clazz.getEntityModelId());
            if(!isChild){
                try {
                    AbstractClassInfo entityClassInfo = AbstractClassInfo.getClassInfo(entity.getClassName());
                    if(!clazz.getClassInfo().getFullName().equals(entityClassInfo.getFullName()) && clazz.getClassInfo().isAssignableFrom(entityClassInfo)){
                        isChild=true;
                    }
                }
                catch(ClassNotFoundException e){
                    //Ignore
                }
            }
            if(isChild){
                try {
                    CouchbaseDocumentStructureReflection subClass = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo)ClassInfo.getClassInfo(entity.getClassName()));
                    DtoModel childModel = context.getDtoModelByKey(buildKey(subClass.getClassInfo(),mode));
                    if(childModel==null){
                        resultList.add(getOrBuildModel(this.context,subClass,packageName,new ConverterServiceInfo.GenMode(mode,FieldFilteringMode.INHERIT).childFor(this),this.converterServiceInfo));
                    }
                    else{
                        resultList.add(childModel);
                    }
                }
                catch(ClassNotFoundException e){
                    throw new RuntimeException(e);
                }
            }
        }
        this.listChildClasses=resultList;
    }


    public ClassName getClassName() {
        return ClassName.get(packageName,shortName);
    }

    public DtoModel getParentModel() {
        return parentModel;
    }
}
