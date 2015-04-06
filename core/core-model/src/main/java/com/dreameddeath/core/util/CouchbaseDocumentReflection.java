/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.util;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.core.tools.annotation.processor.reflection.ClassInfo;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by CEAJ8230 on 04/01/2015.
 */
public class CouchbaseDocumentReflection {
    private static Map<Class<? extends CouchbaseDocument>,CouchbaseDocumentReflection> _REFLEXION_CACHE=new HashMap<>();
    private static Map<TypeElement,CouchbaseDocumentReflection> _TYPE_ElEMENT_REFLECTION_CACHE =new HashMap<>();


    public static boolean isReflexible(Class clazz){
        return CouchbaseDocument.class.isAssignableFrom(clazz);
    }

    public static boolean isReflexible(Element element){
        if(element instanceof TypeElement){
            return AbstractClassInfo.getClassInfo(CouchbaseDocument.class).isAssignableFrom((TypeElement)element);
            //return AnnotationProcessorUtils.isAssignableFrom(CouchbaseDocument.class,) ;
        }
        return false;
    }


    public static boolean isReflexible(ClassInfo classInfo){
        return AbstractClassInfo.getClassInfo(CouchbaseDocument.class).isAssignableFrom(classInfo);

    }


    private static CouchbaseDocumentReflection getReflectionFromClassInfo(ClassInfo classInfo){
        if(classInfo.getTypeElement()!=null){
            return getReflectionFromTypeElement(classInfo.getTypeElement());
        }
        else{
            return getReflectionFromClass(classInfo.getCurrentClass());
        }
    }

    public static CouchbaseDocumentReflection getReflectionFromClass(Class<? extends CouchbaseDocument> doc){
        if(!_REFLEXION_CACHE.containsKey(doc)){
            ClassInfo classInfo = (ClassInfo)AbstractClassInfo.getClassInfo(doc);
            if(classInfo.getTypeElement()!=null){
                getReflectionFromTypeElement(classInfo.getTypeElement());
            }
            else{
                _REFLEXION_CACHE.put(doc,new CouchbaseDocumentReflection(classInfo));
            }
        }
        return _REFLEXION_CACHE.get(doc);
    }


    public static CouchbaseDocumentReflection getReflectionFromTypeElement(TypeElement element){
        if(!_TYPE_ElEMENT_REFLECTION_CACHE.containsKey(element)){
            ClassInfo classInfo = (ClassInfo)AbstractClassInfo.getClassInfo(element);
            CouchbaseDocumentReflection reflection = new CouchbaseDocumentReflection(classInfo);
            if(classInfo.getCurrentClass()!=null){
                _TYPE_ElEMENT_REFLECTION_CACHE.put(element,reflection );
            }

            _TYPE_ElEMENT_REFLECTION_CACHE.put(element, reflection);
        }
        return _TYPE_ElEMENT_REFLECTION_CACHE.get(element);
    }

    private ClassInfo _classInfo;
    private CouchbaseDocumentStructureReflection _structure;

    protected CouchbaseDocumentReflection(ClassInfo classInfo){
        _classInfo = classInfo;
        _structure = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(_classInfo);
    }

    public CouchbaseDocumentStructureReflection getStructure() {
        return _structure;
    }

    public CouchbaseDocumentReflection getSuperclassReflection() {
        if((_classInfo.getSuperClass()!=null) && isReflexible(_classInfo.getSuperClass())) {
            return  CouchbaseDocumentReflection.getReflectionFromClassInfo(_classInfo.getSuperClass());
        }
        return null;
    }

    public String getSimpleName() {
        return _classInfo.getSimpleName();
    }

    public String getName() {
        return _classInfo.getName();
    }

    public ClassInfo getClassInfo() {
        return _classInfo;
    }
}
