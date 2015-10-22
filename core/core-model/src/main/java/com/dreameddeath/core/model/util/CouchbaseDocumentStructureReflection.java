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

package com.dreameddeath.core.model.util;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.entity.model.EntityModelId;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 04/01/2015.
 */
public class CouchbaseDocumentStructureReflection {
    private final static Map<Class,CouchbaseDocumentStructureReflection> REFLECTION_CACHE=new HashMap<>();
    private final static Map<TypeElement,CouchbaseDocumentStructureReflection> TYPE_ELEMENT_REFLECTION_CACHE =new HashMap<>();

    public static boolean isReflexible(String name) throws ClassNotFoundException{
        AbstractClassInfo classInfo = AbstractClassInfo.getClassInfo(name);
        if(classInfo instanceof ClassInfo) {
            return isReflexible((ClassInfo) classInfo);
        }
        else{
            return false;
        }
    }

    public static boolean isReflexible(ClassInfo classInfo){
        if(classInfo.getCurrentClass()!=null) {
            return isReflexible(classInfo.getCurrentClass());
        }
        else{
            return isReflexible(classInfo.getTypeElement());
        }
    }

    public static boolean isReflexible(Class clazz){
        return CouchbaseDocument.class.isAssignableFrom(clazz) || CouchbaseDocumentElement.class.isAssignableFrom(clazz);
    }

    public static boolean isReflexible(Element element){
        if(element instanceof TypeElement){
            return AbstractClassInfo.getClassInfo(CouchbaseDocument.class).isAssignableFrom((TypeElement)element) ||
                    AbstractClassInfo.getClassInfo(CouchbaseDocumentElement.class).isAssignableFrom((TypeElement)element);
        }
        return false;
    }

    public static CouchbaseDocumentStructureReflection getReflectionFromClassInfo(Class<? extends CouchbaseDocumentElement> docEltclass) {
        ClassInfo classInfo = (ClassInfo)AbstractClassInfo.getClassInfo(docEltclass);
        return getReflectionFromClassInfo(classInfo);
    }

    public static CouchbaseDocumentStructureReflection getReflectionFromClassInfo(ClassInfo classInfo){
        if(classInfo.getTypeElement()!=null){
            synchronized (TYPE_ELEMENT_REFLECTION_CACHE) {
                if (!TYPE_ELEMENT_REFLECTION_CACHE.containsKey(classInfo.getTypeElement())) {
                    CouchbaseDocumentStructureReflection reflection = new CouchbaseDocumentStructureReflection(classInfo);
                    if (classInfo.getCurrentClass() != null) {
                        synchronized (REFLECTION_CACHE) {
                            if(!REFLECTION_CACHE.containsKey(classInfo.getCurrentClass())) {
                                REFLECTION_CACHE.put(classInfo.getCurrentClass(), reflection);
                            }
                        }
                    }
                    TYPE_ELEMENT_REFLECTION_CACHE.put(classInfo.getTypeElement(), reflection);
                }
                return TYPE_ELEMENT_REFLECTION_CACHE.get(classInfo.getTypeElement());
            }
        }
        else{
            synchronized (REFLECTION_CACHE) {
                if (!REFLECTION_CACHE.containsKey(classInfo.getCurrentClass())) {
                    REFLECTION_CACHE.put(classInfo.getCurrentClass(), new CouchbaseDocumentStructureReflection(classInfo));
                }
                return REFLECTION_CACHE.get(classInfo.getCurrentClass());
            }
        }
    }

    private final ClassInfo classInfo;
    private final EntityModelId modelId;
    private final CouchbaseDocumentStructureReflection parentClassReflexion;
    private final List<CouchbaseDocumentFieldReflection> fields=new ArrayList<>();
    private final Map<String,CouchbaseDocumentFieldReflection> propertyNameMap =new HashMap<>();
    private final Map<String,CouchbaseDocumentFieldReflection> getterMap=new HashMap<>();
    private final Map<String,CouchbaseDocumentFieldReflection> setterMap=new HashMap<>();
    private final Map<String,CouchbaseDocumentFieldReflection> fieldNameMap=new HashMap<>();
    private final List<CouchbaseDocumentFieldReflection> allFields=new ArrayList<>();
    private final Map<String,CouchbaseDocumentFieldReflection> allPropertyNameMap =new HashMap<>();

    protected CouchbaseDocumentFieldReflection addField(CouchbaseDocumentFieldReflection newField){
        fields.add(newField);
        propertyNameMap.put(newField.getName(), newField);
        fieldNameMap.put(newField.getField().getName(),newField);
        getterMap.put(newField.getGetterName(),newField);
        if(newField.getSetterName()!=null){
            setterMap.put(newField.getSetterName(),newField);
        }
        allFields.add(newField);
        allPropertyNameMap.put(newField.getName(), newField);
        return newField;
    }

    protected CouchbaseDocumentStructureReflection(ClassInfo classInfo){
        this.classInfo = classInfo;
        DocumentDef docDefAnnot = classInfo.getAnnotation(DocumentDef.class);
        modelId = (docDefAnnot!=null)?EntityModelId.build(docDefAnnot,classInfo):EntityModelId.EMPTY_MODEL_ID;

        if(CouchbaseDocumentStructureReflection.isReflexible(classInfo.getSuperClass())){
            parentClassReflexion = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(classInfo.getSuperClass());
            allFields.addAll(parentClassReflexion.allFields);
            allPropertyNameMap.putAll(parentClassReflexion.allPropertyNameMap);
        }
        else{
            parentClassReflexion=null;
        }

        allFields.addAll(fields);
        allPropertyNameMap.putAll(propertyNameMap);

        classInfo.getDeclaredFields()
                .stream()
                .filter(field ->
                        field.getAnnotation(DocumentProperty.class) != null
                )
                .forEach(field ->
                                CouchbaseDocumentStructureReflection.this.addField(new CouchbaseDocumentFieldReflection(field))
                );
    }

    public List<CouchbaseDocumentFieldReflection> getDeclaredFields(){
        return fields;
    }

    public CouchbaseDocumentFieldReflection getDeclaredFieldByName(String name){
        return fieldNameMap.get(name);
    }

    public List<CouchbaseDocumentFieldReflection> getFields(){
        return allFields;
    }

    public CouchbaseDocumentFieldReflection getFieldByPropertyName(String name) {
        return allPropertyNameMap.get(name);
    }

    public CouchbaseDocumentFieldReflection getDeclaredFieldByGetterName(String name){
        return getterMap.get(name);
    }

    public CouchbaseDocumentFieldReflection getDeclaredFieldBySetterName(String name){
        return setterMap.get(name);
    }

    public String getSimpleName() {
        return classInfo.getSimpleName();
    }

    public String getName() {
        return classInfo.getName();
    }

    public Class getRootClass() {
        return classInfo.getCurrentClass();
    }

    public ClassInfo getClassInfo(){
        return classInfo;
    }

    public String getStructDomain() {
        return modelId.getDomain();
    }

    public String getStructName() {
        return modelId.getName();
    }

    public String getStructVersion() {
        return modelId.getEntityVersion().toString();
    }

    public String getId(){
        return modelId.toString();
    }

    public EntityModelId getEntityModelId(){
        return modelId;
    }

    public CouchbaseDocumentStructureReflection getSuperclassReflexion() {
        return parentClassReflexion;
    }
}
