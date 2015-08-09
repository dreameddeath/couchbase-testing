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
import com.dreameddeath.compile.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

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
    private static Map<Class,CouchbaseDocumentStructureReflection> _REFLECTION_CACHE=new HashMap<>();
    private static Map<TypeElement,CouchbaseDocumentStructureReflection> _TYPE_ElEMENT_REFLECTION_CACHE=new HashMap<>();


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
            if(!_TYPE_ElEMENT_REFLECTION_CACHE.containsKey(classInfo.getTypeElement())) {
                CouchbaseDocumentStructureReflection reflection = new CouchbaseDocumentStructureReflection(classInfo);
                if(classInfo.getCurrentClass()!=null){
                    _REFLECTION_CACHE.put(classInfo.getCurrentClass(),reflection);
                }
                _TYPE_ElEMENT_REFLECTION_CACHE.put(classInfo.getTypeElement(),reflection);
            }
            return _TYPE_ElEMENT_REFLECTION_CACHE.get(classInfo.getTypeElement());
        }
        else{
            if(!_REFLECTION_CACHE.containsKey(classInfo.getCurrentClass())){
                _REFLECTION_CACHE.put(classInfo.getCurrentClass(),new CouchbaseDocumentStructureReflection(classInfo));
            }
            return _REFLECTION_CACHE.get(classInfo.getCurrentClass());
        }
    }

    private ClassInfo _classInfo;
    private String _structDomain;
    private String _structName;
    private String _structVersion;
    private String _structId;
    private CouchbaseDocumentStructureReflection _parentClassReflexion;
    private List<CouchbaseDocumentFieldReflection> _fields=new ArrayList<>();
    private Map<String,CouchbaseDocumentFieldReflection> _propertyNameMap =new HashMap<>();
    private Map<String,CouchbaseDocumentFieldReflection> _getterMap=new HashMap<>();
    private Map<String,CouchbaseDocumentFieldReflection> _setterMap=new HashMap<>();
    private Map<String,CouchbaseDocumentFieldReflection> _fieldNameMap=new HashMap<>();

    private List<CouchbaseDocumentFieldReflection> _allFields=new ArrayList<>();
    private Map<String,CouchbaseDocumentFieldReflection> _allNameMap=new HashMap<>();

    protected void addField(CouchbaseDocumentFieldReflection newField){
        _fields.add(newField);
        _propertyNameMap.put(newField.getName(), newField);
        _fieldNameMap.put(newField.getField().getName(),newField);
        _getterMap.put(newField.getGetterName(),newField);
        if(newField.getSetterName()!=null){
            _setterMap.put(newField.getSetterName(),newField);
        }
    }

    protected void finalizeStructure() {
        DocumentDef docDefAnnot = _classInfo.getAnnotation(DocumentDef.class);
        if(docDefAnnot!=null){
            _structDomain = docDefAnnot.domain();
            _structName = docDefAnnot.name();
            _structVersion = docDefAnnot.version();
            _structId = _structDomain+"/"+_structName+"/"+_structVersion;
        }

        if(_parentClassReflexion!=null) {
            _allFields.addAll(_parentClassReflexion._allFields);
            _allNameMap.putAll(_parentClassReflexion._allNameMap);
        }
        _allFields.addAll(_fields);
        _allNameMap.putAll(_propertyNameMap);
    }

    protected CouchbaseDocumentStructureReflection(ClassInfo classInfo){
        _classInfo = classInfo;
        for(FieldInfo field:classInfo.getDeclaredFields()){
            DocumentProperty annot = field.getAnnotation(DocumentProperty.class);
            if (annot == null) continue;
            addField(new CouchbaseDocumentFieldReflection(field));
        }

        if(CouchbaseDocumentStructureReflection.isReflexible(classInfo.getSuperClass())){
            _parentClassReflexion = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(classInfo.getSuperClass());
        }
        finalizeStructure();
    }

    public List<CouchbaseDocumentFieldReflection> getDeclaredFields(){
        return _fields;
    }

    public CouchbaseDocumentFieldReflection getDeclaredFieldByName(String name){
        return _fieldNameMap.get(name);
    }

    public List<CouchbaseDocumentFieldReflection> getFields(){
        return _allFields;
    }

    public CouchbaseDocumentFieldReflection getFieldByPropertyName(String name) {
        return _allNameMap.get(name);
    }

    public CouchbaseDocumentFieldReflection getDeclaredFieldByGetterName(String name){
        return _getterMap.get(name);
    }

    public CouchbaseDocumentFieldReflection getDeclaredFieldBySetterName(String name){
        return _setterMap.get(name);
    }

    public String getSimpleName() {
        return _classInfo.getSimpleName();
    }

    public String getName() {
        return _classInfo.getName();
    }

    public Class getRootClass() {
        return _classInfo.getCurrentClass();
    }

    public ClassInfo getClassInfo(){
        return _classInfo;
    }

    public String getStructDomain() {
        return _structDomain;
    }

    public String getStructName() {
        return _structName;
    }

    public String getStructVersion() {
        return _structVersion;
    }

    public String getId(){
        return _structId;
    }
}
