package com.dreameddeath.core.util;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.util.processor.AnnotationProcessorUtils.ClassInfo;
import com.dreameddeath.core.util.processor.AnnotationProcessorUtils.ParameterizedInfo;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.*;

/**
 * Created by CEAJ8230 on 04/01/2015.
 */
public class CouchbaseDocumentFieldReflection {
    private String _name;
    private Field _field;

    private TypeInfo _effectiveType;
    private AccessibleObject _getter;
    private Method _setter;

    private Element _getterElement;
    private Element _setterElement;



    public static class TypeInfo{
        private ClassInfo _classInfo;
        private ParameterizedInfo _collectionElementType;
        private ParameterizedInfo _mapKeyType;
        private ParameterizedInfo _mapValueType;

        public TypeInfo(DeclaredType type){
            _classInfo = new ClassInfo(type);
            if(_classInfo.isCollection()){
                _collectionElementType = ParameterizedInfo.getParameterizedArgumentClassInfo(type,0);
            }
            else if(_classInfo.isMap()){
                _mapKeyType = ParameterizedInfo.getParameterizedArgumentClassInfo(type,0);
                _mapValueType = ParameterizedInfo.getParameterizedArgumentClassInfo(type,1);
            }
        }

        public TypeInfo(Type type){
            if(type instanceof ParameterizedType) {
                _classInfo = new ClassInfo((Class) ((ParameterizedType) type).getRawType());
            }
            else if(type instanceof Class){
                _classInfo = new ClassInfo((Class)type);
            }
            else if(type instanceof TypeVariable){
                if(((TypeVariable)type).getBounds()[0] instanceof Class){
                    _classInfo = new ClassInfo((Class)((TypeVariable)type).getBounds()[0]);
                }
            }

            if((_classInfo!=null) && _classInfo.isCollection() && (type instanceof ParameterizedType)){
                _collectionElementType = ParameterizedInfo.getParameterizedArgumentClass((ParameterizedType)type,0);
            }
            else if((_classInfo!=null) && _classInfo.isMap() && (type instanceof ParameterizedType)) {
                _mapKeyType= ParameterizedInfo.getParameterizedArgumentClass((ParameterizedType)type, 0);
                _mapValueType= ParameterizedInfo.getParameterizedArgumentClass((ParameterizedType)type, 1);
            }
        }

        public ClassInfo getMainClass(){
            return _classInfo;
        }

        public ParameterizedInfo getCollectionElementType() {
            return _collectionElementType;
        }

        public ParameterizedInfo getMapKeyType() {
            return _mapKeyType;
        }

        public ParameterizedInfo getMapValueType() {
            return _mapValueType;
        }


    }

    protected String nameBuilder(String name, String prefix){
        if(name.startsWith("_")){
            name = name.substring(1);
        }
        return prefix+name.substring(0,1).toUpperCase()+name.substring(1);
    }


    public Method fieldGetterFinder(Field field) throws NoSuchMethodException{
        if(field.getAnnotation(DocumentProperty.class)!=null){
            DocumentProperty prop = field.getAnnotation(DocumentProperty.class);
            String getter = prop.getter();
            if((getter!=null)&& !getter.equals("")){
                return field.getDeclaringClass().getDeclaredMethod(getter);
            }
            else {
                String name = nameBuilder(prop.value(),"get");
                try {
                    return field.getDeclaringClass().getDeclaredMethod(name);
                }
                catch(NoSuchMethodException e){
                    //Do nothing
                }
            }
        }

        String name = nameBuilder(field.getName(),"get");
        return field.getDeclaringClass().getDeclaredMethod(name);
    }

    public Element fieldGetterFinder(Element element){
        if(element.getAnnotation(DocumentProperty.class)!=null) {
            DocumentProperty prop = element.getAnnotation(DocumentProperty.class);
            String getter = prop.getter();
            if ((getter != null) && !getter.equals("")) {
                return siblingElementFinder(element, getter,false);
            } else {
                Element result = siblingElementFinder(element, nameBuilder(prop.value(), "get"),false);
                if (result != null) {
                    return result;
                }
            }
        }

        return siblingElementFinder(element, nameBuilder(element.getSimpleName().toString(), "get"),false);
    }


    public Method fieldSetterFinder(Field field) throws NoSuchMethodException{
        if(field.getAnnotation(DocumentProperty.class)!=null){
            DocumentProperty prop = field.getAnnotation(DocumentProperty.class);
            String setter = prop.setter();
            if((setter!=null)&& !setter.equals("")){
                return field.getDeclaringClass().getDeclaredMethod(setter,_effectiveType.getMainClass().getRealClass());
            }
            else {
                String name = nameBuilder(prop.value(), "set");
                try {
                    return field.getDeclaringClass().getDeclaredMethod(name,_effectiveType.getMainClass().getRealClass());
                }
                catch(NoSuchMethodException e){
                    //Do nothing
                }
            }
        }

        String name=nameBuilder(field.getName(), "set");
        return field.getDeclaringClass().getDeclaredMethod(name,_effectiveType.getMainClass().getRealClass());
    }



    public Element fieldSetterFinder(Element element){
        if(element.getAnnotation(DocumentProperty.class)!=null) {
            DocumentProperty prop = element.getAnnotation(DocumentProperty.class);
            String setter = prop.setter();
            if((setter != null)&& !setter.equals("")) {
                return siblingElementFinder(element, setter,true);
            } else {
                Element result = siblingElementFinder(element, nameBuilder(prop.value(), "set"),true);
                if (result != null) {
                    return result;
                }
            }
        }

        return siblingElementFinder(element,nameBuilder(element.getSimpleName().toString(),"set"),true);
    }

    public TypeMirror getType(Element element){
        if(element instanceof ExecutableElement) {
            return ((ExecutableElement) _getterElement).getReturnType();
        }
        else{
            return element.asType();
        }

    }


    public Element siblingElementFinder(Element element, String name,boolean isSetter){
        for(Element sibling:element.getEnclosingElement().getEnclosedElements()){
            if((sibling instanceof ExecutableElement) && sibling.getSimpleName().toString().equals(name)){
                ExecutableElement methodElement = (ExecutableElement) sibling;
                if(isSetter){
                    if((methodElement.getParameters().size()==1) && methodElement.getParameters().get(0).asType().equals(getType(_getterElement))){
                        return sibling;
                    }
                }
                else{
                    if(methodElement.getParameters().size()==0){
                        return sibling;
                    }
                }
            }
        }
        return null;
    }


    public CouchbaseDocumentFieldReflection(Element element) {
        _name = element.getAnnotation(DocumentProperty.class).value();

        _getterElement = fieldGetterFinder(element);
        if(_getterElement==null){
            if(element.getModifiers().contains(javax.lang.model.element.Modifier.PUBLIC)){
                _getterElement = element;
            }
        }
        TypeMirror effectiveType = getType(_getterElement);
        if(effectiveType instanceof DeclaredType){
            _effectiveType = new TypeInfo((DeclaredType)effectiveType);
        }

        _setterElement= fieldSetterFinder(element);
        if(_setterElement!=null){

        }

    }

    public CouchbaseDocumentFieldReflection(Field field){
        _name = field.getAnnotation(DocumentProperty.class).value();

        try {
            _getter = fieldGetterFinder(field);
            _effectiveType = new TypeInfo(((Method)_getter).getGenericReturnType());
        } catch (NoSuchMethodException e) {
            if ((field.getModifiers() & java.lang.reflect.Modifier.PUBLIC)!=0) {
                _getter = field;
                _effectiveType = new TypeInfo(field.getGenericType());
            } else {
                //TODO throw an error
            }
        }

        _field = field;
        try {
            _setter = fieldSetterFinder(field);
        } catch (NoSuchMethodException e) {

        }



    }


    public String getName() {
        return _name;
    }

    public Field getField() {
        return _field;
    }

    public boolean isPureField(){
        if(_getterElement!=null){
            return _getterElement.getKind().isField();
        }
        return _field.equals(_getter);
    }

    public Class<?> getEffectiveTypeClass() {
        return _effectiveType.getMainClass().getRealClass();
    }
    public TypeInfo getEffectiveTypeInfo() {
        return _effectiveType;
    }

    public AccessibleObject getGetter() {
        return _getter;
    }

    public String getGetterName(){
        if(_getterElement != null){
            return _getterElement.getSimpleName().toString();
        }
        else if(_getter instanceof Field){
            return ((Field)_getter).getName();
        }
        else{
            return ((Method)_getter).getName();
        }
    }

    public Method getSetter() {
        return _setter;
    }
    public String getSetterName(){
        if(_setterElement!=null){
            return _setterElement.getSimpleName().toString();
        }
        else if(_setter!=null){
            return _setter.getName();
        }
        return null;
    }

    public boolean isCollection() {
        return _effectiveType.getMainClass().isCollection();
    }

    public Class<?> getCollectionElementClass() {
        return _effectiveType.getCollectionElementType().getRealClass();
    }
    public ParameterizedInfo getCollectionElementTypeInfo() {
        return _effectiveType.getCollectionElementType();
    }

    public boolean isMap() {
        return _effectiveType.getMainClass().isMap();
    }

    public Class<?> getMapKeyClass() {
        return _effectiveType.getMapKeyType().getRealClass();
    }
    public ParameterizedInfo getMapKeyTypeInfo() {
        return _effectiveType.getMapKeyType();
    }

    public Class<?> getMapValueClass() {
        return _effectiveType.getMapValueType().getRealClass();
    }
    public ParameterizedInfo getMapValueTypeInfo() {
        return _effectiveType.getMapValueType();
    }
}
