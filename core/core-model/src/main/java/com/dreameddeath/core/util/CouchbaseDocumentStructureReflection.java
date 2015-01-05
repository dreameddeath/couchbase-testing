package com.dreameddeath.core.util;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.util.processor.AnnotationProcessorUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CEAJ8230 on 04/01/2015.
 */
public class CouchbaseDocumentStructureReflection {
    private static Map<Class,CouchbaseDocumentStructureReflection> _REFLECTION_CACHE=new HashMap<>();
    private static Map<TypeElement,CouchbaseDocumentStructureReflection> _TYPE_ElEMENT_REFLECTION_CACHE=new HashMap<>();


    public static boolean isReflexible(Class clazz){
        return CouchbaseDocument.class.isAssignableFrom(clazz) || CouchbaseDocumentElement.class.isAssignableFrom(clazz);
    }
    public static CouchbaseDocumentStructureReflection getReflectionFromClass(Class element){
        if(!_REFLECTION_CACHE.containsKey(element)){
            _REFLECTION_CACHE.put(element,new CouchbaseDocumentStructureReflection(element));
        }
        return _REFLECTION_CACHE.get(element);
    }


    public static CouchbaseDocumentStructureReflection getReflectionFromTypeElement(TypeElement element){
        if(!_TYPE_ElEMENT_REFLECTION_CACHE.containsKey(element)){
            Class rootClass = AnnotationProcessorUtils.getClass(element);
            if(rootClass!=null){
                _TYPE_ElEMENT_REFLECTION_CACHE.put(element,getReflectionFromClass(rootClass));
            }
            else {
                _TYPE_ElEMENT_REFLECTION_CACHE.put(element, new CouchbaseDocumentStructureReflection(element));
            }
        }
        return _TYPE_ElEMENT_REFLECTION_CACHE.get(element);
    }


    private Class _rootClass;
    private String _simpleName;
    private String _name;
    private List<CouchbaseDocumentFieldReflection> _fields=new ArrayList<>();
    private Map<String,CouchbaseDocumentFieldReflection> _nameMap=new HashMap<>();
    private Map<String,CouchbaseDocumentFieldReflection> _getterMap=new HashMap<>();
    private Map<String,CouchbaseDocumentFieldReflection> _setterMap=new HashMap<>();
    private Map<Field,CouchbaseDocumentFieldReflection> _fieldMap =new HashMap<>();


    private List<CouchbaseDocumentFieldReflection> _allFields=new ArrayList<>();
    private Map<String,CouchbaseDocumentFieldReflection> _allNameMap=new HashMap<>();
    private Map<String,CouchbaseDocumentFieldReflection> _allGetterMap=new HashMap<>();

    protected void addField(CouchbaseDocumentFieldReflection newField){
        _fields.add(newField);
        _fieldMap.put(newField.getField(), newField);
        _nameMap.put(newField.getName(),newField);
        _getterMap.put(newField.getGetterName(),newField);
        if(newField.getSetterName()!=null){
            _setterMap.put(newField.getSetterName(),newField);
        }
    }

    protected CouchbaseDocumentStructureReflection(Class element){
        _rootClass = element;
        _simpleName = element.getSimpleName();
        _name = element.getName();
        for(Field member : element.getDeclaredFields()) {
            DocumentProperty annot = member.getAnnotation(DocumentProperty.class);
            if (annot == null) continue;
            addField(new CouchbaseDocumentFieldReflection(member));
        }


        if(!CouchbaseDocument.class.equals(element) &&
                !CouchbaseDocumentElement.class.equals(element)){
            CouchbaseDocumentStructureReflection parentStructure=null;
            if(CouchbaseDocument.class.isAssignableFrom(element)){
                CouchbaseDocumentReflection result = CouchbaseDocumentReflection.getReflectionFromClass(element.getSuperclass());
                parentStructure = result.getStructure();
            }
            else{
                parentStructure = CouchbaseDocumentStructureReflection.getReflectionFromClass(element.getSuperclass());
            }

            if(parentStructure!=null) {
                _allFields.addAll(parentStructure._allFields);
                _allNameMap.putAll(parentStructure._allNameMap);
            }
        }
        _allFields.addAll(_fields);
        _allNameMap.putAll(_nameMap);
    }

    protected CouchbaseDocumentStructureReflection(TypeElement element){
        _simpleName = element.getSimpleName().toString();
        _name= AnnotationProcessorUtils.getClassName(element);
        _rootClass = AnnotationProcessorUtils.getClass(element);
        for(Element elt:element.getEnclosedElements()){
            if(elt.getAnnotation(DocumentProperty.class)!=null){
                addField(new CouchbaseDocumentFieldReflection(elt));
            }
        }

        if(!CouchbaseDocument.class.getName().equals(_name) &&
                !CouchbaseDocument.class.getName().equals(_name)){
            CouchbaseDocumentStructureReflection parentStructure=null;
            if(AnnotationProcessorUtils.isAssignableFrom(CouchbaseDocument.class,element)){
                CouchbaseDocumentReflection result = CouchbaseDocumentReflection.getReflectionFromTypeElement(AnnotationProcessorUtils.getSuperClass(element));
                parentStructure = result.getStructure();
            }
            else{
                parentStructure = CouchbaseDocumentStructureReflection.getReflectionFromTypeElement((TypeElement)((DeclaredType) element.getSuperclass()).asElement());
            }

            if(parentStructure!=null) {
                _allFields.addAll(parentStructure._allFields);
                _allNameMap.putAll(parentStructure._allNameMap);
            }
        }
        _allFields.addAll(_fields);
        _allNameMap.putAll(_nameMap);
    }

    public List<CouchbaseDocumentFieldReflection> getDeclaredFields(){
        return _fields;
    }

    public CouchbaseDocumentFieldReflection getDeclaredFieldByName(String name){
        return _nameMap.get(name);
    }

    public List<CouchbaseDocumentFieldReflection> getFields(){
        return _allFields;
    }

    public CouchbaseDocumentFieldReflection getFieldByName(String name) {
        return _allNameMap.get(name);
    }

    public CouchbaseDocumentFieldReflection getDeclaredFieldByGetterName(String name){
        return _getterMap.get(name);
    }

    public CouchbaseDocumentFieldReflection getDeclaredFieldBySetterName(String name){
        return _setterMap.get(name);
    }

    public CouchbaseDocumentFieldReflection getDeclaredField(Field field){
        return _fieldMap.get(field);
    }

}
