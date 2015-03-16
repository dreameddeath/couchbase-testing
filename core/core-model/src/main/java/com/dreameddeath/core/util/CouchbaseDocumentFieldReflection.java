package com.dreameddeath.core.util;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.core.tools.annotation.processor.reflection.MemberInfo;
import com.dreameddeath.core.tools.annotation.processor.reflection.MethodInfo;
import com.dreameddeath.core.tools.annotation.processor.reflection.ParameterizedTypeInfo;

import java.util.Collection;
import java.util.Map;

/**
 * Created by CEAJ8230 on 04/01/2015.
 */
public class CouchbaseDocumentFieldReflection {
    private String _name;
    private FieldInfo _field;
    private ParameterizedTypeInfo _effectiveType;
    //private Field _field;

    private MemberInfo _getter;
    private MemberInfo _setter;
    /*private TypeInfo _effectiveType;
    private AccessibleObject _getter;
    private Method _setter;

    private MemberInfo _getterElement;
    private MemberInfo _setterElement;*/



    /*public static class TypeInfo{
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


    }*/

    protected String nameBuilder(String name, String prefix){
        if(name.startsWith("_")){
            name = name.substring(1);
        }
        return prefix+name.substring(0,1).toUpperCase()+name.substring(1);
    }


    public MethodInfo fieldGetterFinder(){
        MethodInfo result = null;
        if(_field.getAnnotation(DocumentProperty.class)!=null){
            DocumentProperty prop = _field.getAnnotation(DocumentProperty.class);
            String getter = prop.getter();
            if((getter!=null)&& !getter.equals("")){
                result=_field.getDeclaringClassInfo().getDeclaredMethod(getter);
            }
            else {
                String name = nameBuilder(prop.value(),"get");
                result= _field.getDeclaringClassInfo().getDeclaredMethod(name);
            }
        }

        if(result==null){
            String name = nameBuilder(_field.getName(),"get");
            result = _field.getDeclaringClassInfo().getDeclaredMethod(name);
        }

        return result;
    }

    /*public Method fieldGetterFinder(Field field) throws NoSuchMethodException{
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
    }*/

    public MethodInfo fieldSetterFinder(){
        MethodInfo result = null;
        if(_field.getAnnotation(DocumentProperty.class)!=null){
            DocumentProperty prop = _field.getAnnotation(DocumentProperty.class);
            String setter = prop.setter();
            if((setter!=null)&& !setter.equals("")){
                result = _field.getDeclaringClassInfo().getDeclaredMethod(setter, _effectiveType);
            }
            else {
                String name = nameBuilder(prop.value(), "set");
                result= _field.getDeclaringClassInfo().getDeclaredMethod(name,_effectiveType);
            }
        }

        if(result==null) {
            String name = nameBuilder(_field.getName(), "set");
            result = _field.getDeclaringClassInfo().getDeclaredMethod(name,_effectiveType);
        }
        return result;
    }

    /*public Method fieldSetterFinder(Field field) throws NoSuchMethodException{
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

    }*/


    /*public Element siblingElementFinder(Element element, String name,boolean isSetter){
        for(Element sibling:element.getEnclosingElement().getEnclosedElements()){
            if((sibling instanceof ExecutableElement) && sibling.getSimpleName().toString().equals(name)){
                ExecutableElement methodElement = (ExecutableElement) sibling;
                if(isSetter){
                    if((methodElement.getParameters().size()==1) && AnnotationProcessorUtils.isAssignableFrom(methodElement.getParameters().get(0).asType(),getType(_getterElement))){
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
    }*/

    public CouchbaseDocumentFieldReflection(FieldInfo fieldInfo) {
        _name = fieldInfo.getAnnotation(DocumentProperty.class).value();
        _field = fieldInfo;
        _getter = fieldGetterFinder();
        if(_getter==null){
            if(fieldInfo.isPublic()){
                _getter = fieldInfo;
                _effectiveType = fieldInfo.getType();
            }
            else{
                throw new RuntimeException("Cannot find getter of field "+_name+ " for entity "+fieldInfo.getDeclaringClassInfo().getFullName());
            }
        }
        else{
            _effectiveType = ((MethodInfo)_getter).getReturnType();
        }

        _setter= fieldSetterFinder();
        if(_setter==null) {
            if (fieldInfo.isPublic()) {
                _setter = fieldInfo;
            }
            else {
                throw new RuntimeException("Cannot find setter of field " + _name + " for entity" + fieldInfo.getDeclaringClassInfo().getFullName());
            }
        }
    }

    public String getName() {
        return _name;
    }

    public FieldInfo getField() {
        return _field;
    }

    public Class<?> getEffectiveTypeClass() {
        return getEffectiveTypeInfo().getMainType().getCurrentClass();
    }

    public ParameterizedTypeInfo getEffectiveTypeInfo() {
        return _effectiveType;
    }

    public MemberInfo getGetter() {
        return _getter;
    }

    public String buildGetterCode(){
        if(_getter instanceof FieldInfo){
            return getGetterName();
        }
        else{
            return getGetterName()+"()";
        }
    }

    public String getGetterName(){
        return _getter.getName();
    }

    public MemberInfo getSetter() {
        return _setter;
    }

    public String getSetterName(){
        return _setter.getName();
    }

    public boolean isCollection() {
        return _field.getType().isAssignableTo(Collection.class);
    }

    public Class<?> getCollectionElementClass() {
        return getCollectionElementTypeInfo().getMainType().getCurrentClass();
    }
    public ParameterizedTypeInfo getCollectionElementTypeInfo() {
        return _field.getType().getMainTypeGeneric(0);
    }

    public boolean isMap() {
        return _field.getType().isAssignableTo(Map.class);
    }

    public Class<?> getMapKeyClass() {
        return getMapKeyTypeInfo().getMainType().getCurrentClass();
    }
    public ParameterizedTypeInfo getMapKeyTypeInfo() {
        return _field.getType().getMainTypeGeneric(0);
    }

    public Class<?> getMapValueClass() {
        return getMapValueTypeInfo().getMainType().getCurrentClass();
    }
    public ParameterizedTypeInfo getMapValueTypeInfo() {
        return _field.getType().getMainTypeGeneric(1);
    }
}
