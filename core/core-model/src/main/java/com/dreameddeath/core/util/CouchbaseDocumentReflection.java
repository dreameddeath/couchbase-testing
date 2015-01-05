package com.dreameddeath.core.util;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.util.processor.AnnotationProcessorUtils;

import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by CEAJ8230 on 04/01/2015.
 */
public class CouchbaseDocumentReflection {
    private static Map<Class<? extends CouchbaseDocument>,CouchbaseDocumentReflection> _REFLEXION_CACHE=new HashMap<>();
    private static Map<TypeElement,CouchbaseDocumentReflection> _TYPE_ElEMENT_REFLECTION_CACHE =new HashMap<>();


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



    private String _simpleName;
    private String _name;
    private TypeElement _typeElement;
    private Class<? extends CouchbaseDocument> _document;
    private CouchbaseDocumentStructureReflection _structure;
    private CouchbaseDocumentReflection _superclassReflection;

    protected CouchbaseDocumentReflection(Class<? extends CouchbaseDocument> document){
        _document = document;
        _simpleName = document.getSimpleName();
        _name = document.getName();

        if(!CouchbaseDocument.class.equals(document)){
            Class<? extends CouchbaseDocument>superclassDocument = (Class<? extends CouchbaseDocument>)document.getSuperclass();
            _superclassReflection = CouchbaseDocumentReflection.getReflectionFromClass(superclassDocument);
        }
        _structure = CouchbaseDocumentStructureReflection.getReflectionFromClass(document);
    }

    protected CouchbaseDocumentReflection(TypeElement mirroredElement){
        _typeElement = mirroredElement;

        _simpleName = mirroredElement.getSimpleName().toString();
        _name = AnnotationProcessorUtils.getClassName(mirroredElement);
        _document = (Class<? extends CouchbaseDocument>) AnnotationProcessorUtils.getClass(mirroredElement);
        _structure = CouchbaseDocumentStructureReflection.getReflectionFromTypeElement(mirroredElement);

        if(!AnnotationProcessorUtils.getClassName(mirroredElement).equals(CouchbaseDocument.class.getName())) {
            TypeElement parent = AnnotationProcessorUtils.getSuperClass(mirroredElement);
            if(parent!=null) {
                _superclassReflection = CouchbaseDocumentReflection.getReflectionFromTypeElement(parent);
            }
        }

    }

    public CouchbaseDocumentStructureReflection getStructure() {
        return _structure;
    }


    public CouchbaseDocumentReflection getSuperclassReflection() {
        return _superclassReflection;
    }

    public String getSimpleName() {
        return _simpleName;
    }

    public String getName() {
        return _name;
    }

    public <A extends Annotation> A getAnnotation(Class<A> clazz){
        if(_typeElement!=null) {
            return _typeElement.getAnnotation(clazz);
        }
        else{
            return _document.getAnnotation(clazz);
        }
    }

    public <A extends Annotation> A[] getAnnotations(Class<A> clazz){
        if(_typeElement!=null) {
            return _typeElement.getAnnotationsByType(clazz);
        }
        else{
            return _document.getAnnotationsByType(clazz);
        }
    }

}
