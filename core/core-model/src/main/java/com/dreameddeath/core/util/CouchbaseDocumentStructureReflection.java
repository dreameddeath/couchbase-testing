package com.dreameddeath.core.util;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.util.processor.AnnotationProcessorUtils;
import com.dreameddeath.core.util.processor.AnnotationProcessorUtils.ClassInfo;

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

    public static boolean isReflexible(Element element){
        if(element instanceof TypeElement){
            return AnnotationProcessorUtils.isAssignableFrom(CouchbaseDocument.class,(TypeElement)element) ||AnnotationProcessorUtils.isAssignableFrom(CouchbaseDocumentElement.class,(TypeElement)element);
        }
        return false;
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



    private ClassInfo _classInfo;
    private String _structDomain;
    private String _structName;
    private String _structVersion;
    private CouchbaseDocumentStructureReflection _parentClassReflexion;
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


    protected void finalizeStructure() {
        DocumentDef docDefAnnot = _classInfo.getAnnotation(DocumentDef.class);
        if(docDefAnnot!=null){
            _structDomain = docDefAnnot.domain();
            _structName = docDefAnnot.name();
            _structVersion = docDefAnnot.version();
        }

        if(_parentClassReflexion!=null) {
            _allFields.addAll(_parentClassReflexion._allFields);
            _allNameMap.putAll(_parentClassReflexion._allNameMap);
        }
        _allFields.addAll(_fields);
        _allNameMap.putAll(_nameMap);
    }

    protected CouchbaseDocumentStructureReflection(Class clazz){
        _classInfo = new ClassInfo(clazz);

        for(Field member : clazz.getDeclaredFields()) {
            DocumentProperty annot = member.getAnnotation(DocumentProperty.class);
            if (annot == null) continue;
            addField(new CouchbaseDocumentFieldReflection(member));
        }

        if(CouchbaseDocumentStructureReflection.isReflexible(clazz.getSuperclass())){
            _parentClassReflexion = CouchbaseDocumentStructureReflection.getReflectionFromClass(clazz.getSuperclass());
        }

        finalizeStructure();
    }

    protected CouchbaseDocumentStructureReflection(TypeElement element){
        _classInfo = new ClassInfo((DeclaredType)element.asType());

        for(Element elt:element.getEnclosedElements()){
            if(elt.getAnnotation(DocumentProperty.class)!=null){
                addField(new CouchbaseDocumentFieldReflection(elt));
            }
        }

        TypeElement parent = AnnotationProcessorUtils.getSuperClass(element);
        if((parent!=null) && isReflexible(parent)){
            _parentClassReflexion = CouchbaseDocumentStructureReflection.getReflectionFromTypeElement(parent);
        }

        finalizeStructure();
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

    public String getSimpleName() {
        return _classInfo.getSimpleName();
    }

    public Class getRootClass() {
        return _classInfo.getRealClass();
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
}
