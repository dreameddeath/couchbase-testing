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
import com.dreameddeath.compile.tools.annotation.processor.reflection.AnnotGetter;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.model.document.CouchbaseDocument;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 04/01/2015.
 */
public class CouchbaseDocumentReflection {
    private static Map<Class<? extends CouchbaseDocument>,CouchbaseDocumentReflection> REFLEXION_CACHE=new HashMap<>();
    private static Map<TypeElement,CouchbaseDocumentReflection> TYPE_ElEMENT_REFLECTION_CACHE =new HashMap<>();


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


    public static <T extends Annotation> CouchbaseDocumentReflection getClassInfo(String name) throws ClassNotFoundException{
        AbstractClassInfo classInfo = AbstractClassInfo.getClassInfo(name);
        if((classInfo instanceof ClassInfo) && isReflexible((ClassInfo)classInfo)){
            return getReflectionFromClassInfo((ClassInfo)classInfo);
        }
        else{
            return null;
        }
    }

    public static <T extends Annotation> CouchbaseDocumentReflection getClassInfoFromAnnot(T annot,AnnotGetter<T> getter){
        AbstractClassInfo classInfo = AbstractClassInfo.getClassInfoFromAnnot(annot, getter);
        if((classInfo instanceof ClassInfo) && isReflexible((ClassInfo)classInfo)){
            return getReflectionFromClassInfo((ClassInfo)classInfo);
        }
        else{
            return null;
        }
    }

    public static CouchbaseDocumentReflection getReflectionFromClassInfo(ClassInfo classInfo){
        if(classInfo.getTypeElement()!=null){
            return getReflectionFromTypeElement(classInfo.getTypeElement());
        }
        else{
            return getReflectionFromClass(classInfo.getCurrentClass());
        }
    }

    public static CouchbaseDocumentReflection getReflectionFromClass(Class<? extends CouchbaseDocument> doc){
        if(!REFLEXION_CACHE.containsKey(doc)){
            ClassInfo classInfo = (ClassInfo)AbstractClassInfo.getClassInfo(doc);
            if(classInfo.getTypeElement()!=null){
                getReflectionFromTypeElement(classInfo.getTypeElement());
            }
            else{
                REFLEXION_CACHE.put(doc,new CouchbaseDocumentReflection(classInfo));
            }
        }
        return REFLEXION_CACHE.get(doc);
    }


    public static CouchbaseDocumentReflection getReflectionFromTypeElement(TypeElement element){
        if(!TYPE_ElEMENT_REFLECTION_CACHE.containsKey(element)){
            ClassInfo classInfo = (ClassInfo)AbstractClassInfo.getClassInfo(element);
            CouchbaseDocumentReflection reflection = new CouchbaseDocumentReflection(classInfo);
            if(classInfo.getCurrentClass()!=null){
                TYPE_ElEMENT_REFLECTION_CACHE.put(element,reflection );
            }

            TYPE_ElEMENT_REFLECTION_CACHE.put(element, reflection);
        }
        return TYPE_ElEMENT_REFLECTION_CACHE.get(element);
    }

    private ClassInfo classInfo;
    private CouchbaseDocumentStructureReflection structure;

    protected CouchbaseDocumentReflection(ClassInfo classInfo){
        this.classInfo = classInfo;
        structure = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(classInfo);
    }

    public CouchbaseDocumentStructureReflection getStructure() {
        return structure;
    }

    public CouchbaseDocumentReflection getSuperclassReflection() {
        if((classInfo.getSuperClass()!=null) && isReflexible(classInfo.getSuperClass())) {
            return  CouchbaseDocumentReflection.getReflectionFromClassInfo(classInfo.getSuperClass());
        }
        return null;
    }

    public String getSimpleName() {
        return classInfo.getSimpleName();
    }

    public String getName() {
        return classInfo.getName();
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }
}
