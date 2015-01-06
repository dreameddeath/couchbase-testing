package com.dreameddeath.core.util;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.util.processor.AnnotationProcessorUtils;
import com.dreameddeath.core.util.processor.AnnotationProcessorUtils.ClassInfo;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.lang.annotation.Annotation;
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
            return AnnotationProcessorUtils.isAssignableFrom(CouchbaseDocument.class,(TypeElement)element) ;
        }
        return false;
    }


    public static CouchbaseDocumentReflection getReflectionFromClass(Class<? extends CouchbaseDocument> doc){
        if(!_REFLEXION_CACHE.containsKey(doc)){
            _REFLEXION_CACHE.put(doc,new CouchbaseDocumentReflection(doc));
        }
        return _REFLEXION_CACHE.get(doc);
    }


    public static CouchbaseDocumentReflection getReflectionFromTypeElement(TypeElement element){
        if(!_TYPE_ElEMENT_REFLECTION_CACHE.containsKey(element)){
            Class rootClass = AnnotationProcessorUtils.getClass(element);
            if(rootClass!=null){
                _TYPE_ElEMENT_REFLECTION_CACHE.put(element, getReflectionFromClass(rootClass));
            }
            else {
                _TYPE_ElEMENT_REFLECTION_CACHE.put(element, new CouchbaseDocumentReflection(element));
            }
        }
        return _TYPE_ElEMENT_REFLECTION_CACHE.get(element);
    }

    private ClassInfo _classInfo;
    private CouchbaseDocumentStructureReflection _structure;
    private CouchbaseDocumentReflection _superclassReflection;

    protected CouchbaseDocumentReflection(Class<? extends CouchbaseDocument> document){
        _classInfo = new ClassInfo(document);
        _structure = CouchbaseDocumentStructureReflection.getReflectionFromClass(document);

        if((document.getSuperclass()!=null) && isReflexible(document.getSuperclass())){
            _superclassReflection = CouchbaseDocumentReflection.getReflectionFromClass((Class<? extends CouchbaseDocument>) (document.getSuperclass()));
        }
    }

    protected CouchbaseDocumentReflection(TypeElement element){
        _classInfo = new ClassInfo((DeclaredType)element.asType());
        _structure = CouchbaseDocumentStructureReflection.getReflectionFromTypeElement(element);

        TypeElement parent = AnnotationProcessorUtils.getSuperClass(element);
        if((parent!=null) && isReflexible(parent)){
            _superclassReflection = CouchbaseDocumentReflection.getReflectionFromTypeElement(parent);
        }
    }

    public CouchbaseDocumentStructureReflection getStructure() {
        return _structure;
    }

    public CouchbaseDocumentReflection getSuperclassReflection() {
        return _superclassReflection;
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
