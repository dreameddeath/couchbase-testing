package com.dreameddeath.core.util;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.util.processor.AnnotationProcessorUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.Map;

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
        private boolean _isWildCard=false;
        private DeclaredType _modeType;
        private Class<?> _class=null;
        private boolean _isCollection;
        private TypeInfo _collectionElementType;
        private boolean _isMap;
        private TypeInfo _mapKeyType;
        private TypeInfo _mapValueType;

        protected TypeInfo getParameterizedArgumentClass(DeclaredType field, int pos){
            TypeMirror parameter = field.getTypeArguments().get(pos);
            if(parameter instanceof javax.lang.model.type.WildcardType){
                TypeMirror upperBound = ((javax.lang.model.type.WildcardType) parameter).getExtendsBound();
                if(upperBound instanceof DeclaredType){
                    return new TypeInfo((javax.lang.model.type.WildcardType)parameter,(DeclaredType)upperBound);
                }
            }
            else if(parameter instanceof DeclaredType){
                return new TypeInfo((DeclaredType)parameter);
            }
            return null;
        }


        protected TypeInfo getType(Type type){
            if(type instanceof Class){
                return new TypeInfo((Class) type);
            }
            else if(type instanceof java.lang.reflect.WildcardType){
                Type[] types = ((java.lang.reflect.WildcardType)type).getUpperBounds();
                if(types.length>0){
                    if(types[0] instanceof Class){
                        return new TypeInfo((Class) types[0]);
                    }
                }
            }
            return null;
        }

        protected TypeInfo getParameterizedArgumentClass(Field field, int pos){
            Type type = field.getGenericType();
            return  getType(((ParameterizedType)field.getGenericType()).getActualTypeArguments()[pos]);
        }

        protected TypeInfo getParameterizedArgumentClass(Method method, int pos){
            return  getType(((AnnotatedParameterizedType)method.getAnnotatedReturnType()).getAnnotatedActualTypeArguments()[pos].getType());
        }



        public TypeInfo(javax.lang.model.type.WildcardType type,DeclaredType upperType){
            this(upperType);
            _isWildCard=true;
        }

        public TypeInfo(DeclaredType type){
            _modeType = type;
            _class = AnnotationProcessorUtils.getClass(type);
            if((type.asElement() instanceof TypeElement)){
                TypeElement typeElement = (TypeElement)type.asElement();
                if(AnnotationProcessorUtils.isAssignableFrom(Collection.class,typeElement)){
                    _isCollection = true;
                    _collectionElementType = getParameterizedArgumentClass(_modeType,0);
                }
                else if(AnnotationProcessorUtils.isAssignableFrom(Map.class,typeElement)){
                    _isMap=true;
                    _mapKeyType = getParameterizedArgumentClass(type,0);
                    _mapValueType = getParameterizedArgumentClass(type,1);
                }
            }
        }

        public TypeInfo(Class clazz){
            _class = clazz;
            if(Collection.class.isAssignableFrom(_class)) {
                _isCollection = true;
            }
            else if(Map.class.isAssignableFrom(_class)){
                _isMap = true;
            }
        }

        public TypeInfo(Class clazz,AccessibleObject getter){
            this(clazz);

            if(_isCollection){
                if(getter instanceof Method){
                    _collectionElementType= getParameterizedArgumentClass((Method) getter, 0);
                }
                else{
                    _collectionElementType= getParameterizedArgumentClass((Field)getter,0);
                }
            }
            else if(_isMap){
                if(getter instanceof Method){
                    _mapKeyType = getParameterizedArgumentClass((Method)getter,0);
                    _mapValueType = getParameterizedArgumentClass((Method)getter,1);
                }
                else{
                    _mapKeyType = getParameterizedArgumentClass((Field)getter,0);
                    _mapValueType = getParameterizedArgumentClass((Field)getter,1);
                }
            }
        }

        public String getName(){
            if(_class!=null){
                return _class.getName();
            }
            else{
                return AnnotationProcessorUtils.getClassName((TypeElement)_modeType.asElement());
            }
        }

        public Class getRealClass(){
            return _class;
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
                return field.getDeclaringClass().getDeclaredMethod(setter,_effectiveType.getRealClass());
            }
            else {
                String name = nameBuilder(prop.value(), "set");
                try {
                    return field.getDeclaringClass().getDeclaredMethod(name,_effectiveType.getRealClass());
                }
                catch(NoSuchMethodException e){
                    //Do nothing
                }
            }
        }

        String name=nameBuilder(field.getName(), "set");
        return field.getDeclaringClass().getDeclaredMethod(name,_effectiveType.getRealClass());
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
            _effectiveType = new TypeInfo(((Method)_getter).getReturnType(),_getter);
        } catch (NoSuchMethodException e) {
            if ((field.getModifiers() & java.lang.reflect.Modifier.PUBLIC)!=0) {
                _getter = field;
                _effectiveType = new TypeInfo(field.getType(),_getter);
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
        return _effectiveType.getRealClass();
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
        return _effectiveType._isCollection;
    }

    public Class<?> getCollectionElementClass() {
        return _effectiveType._collectionElementType.getRealClass();
    }
    public TypeInfo getCollectionElementTypeInfo() {
        return _effectiveType._collectionElementType;
    }

    public boolean isMap() {
        return _effectiveType._isMap;
    }

    public Class<?> getMapKeyClass() {
        return _effectiveType._mapKeyType.getRealClass();
    }
    public TypeInfo getMapKeyTypeInfo() {
        return _effectiveType._mapKeyType;
    }

    public Class<?> getMapValueClass() {
        return _effectiveType._mapKeyType.getRealClass();
    }
    public TypeInfo getMapValueTypeInfo() {
        return _effectiveType._mapKeyType;
    }
}
