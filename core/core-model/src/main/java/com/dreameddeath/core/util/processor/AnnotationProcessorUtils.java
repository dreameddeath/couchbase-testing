package com.dreameddeath.core.util.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Created by CEAJ8230 on 05/01/2015.
 */
public class AnnotationProcessorUtils {
    public static String getClassName(TypeElement element) {
        Element currElement = element;
        String result = element.getSimpleName().toString();
        while (currElement.getEnclosingElement() != null) {
            currElement = currElement.getEnclosingElement();
            if (currElement instanceof TypeElement) {
                result = currElement.getSimpleName() + "$" + result;
            } else if (currElement instanceof PackageElement) {
                if (!"".equals(currElement.getSimpleName())) {
                    result = ((PackageElement) currElement).getQualifiedName() + "." + result;
                }
            }
        }
        return result;
    }

    public static boolean isAssignableFrom(Class clazz, TypeElement element) {
        TypeElement currElement = element;
        if (clazz.isInterface()) {
            for (TypeMirror interfaceType : element.getInterfaces()) {
                Element interfaceElement = ((DeclaredType) interfaceType).asElement();
                if (clazz.getName().equals(getClassName((TypeElement) interfaceElement))) {
                    return true;
                }
            }
        }

        while (currElement != null) {
            if (getClassName(currElement).equals(clazz.getName())) {
                return true;
            }
            TypeMirror parent = currElement.getSuperclass();
            if (parent instanceof NoType) {
                break;
            }
            if (parent instanceof DeclaredType) {
                currElement = (TypeElement) ((DeclaredType) parent).asElement();
            }
        }

        return false;
    }

    public static TypeElement getSuperClass(TypeElement element) {
        TypeMirror parent = element.getSuperclass();
        if (parent instanceof DeclaredType) {
            Element elt = ((DeclaredType) parent).asElement();
            if (elt instanceof TypeElement) {
                return (TypeElement) elt;
            }
        }
        return null;
    }

    public static Class getClass(TypeElement element) {
        try {
            return Class.forName(getClassName(element));
        } catch (Exception e) {
            //System.out.println(e);
        }
        return null;
    }

    public static Class getClass(TypeMirror type) {
        if (type instanceof DeclaredType) {
            if (((DeclaredType) type).asElement() instanceof TypeElement) {
                return getClass((TypeElement) ((DeclaredType) type).asElement());
            }
        }
        return null;
    }

    public static class ParameterizedInfo extends ClassInfo {
        private boolean _isWildcard;

        public static ParameterizedInfo getParameterizedArgumentClassInfo(DeclaredType field, int pos){
            TypeMirror parameter = field.getTypeArguments().get(pos);
            if(parameter instanceof javax.lang.model.type.WildcardType){
                TypeMirror upperBound = ((javax.lang.model.type.WildcardType) parameter).getExtendsBound();
                if((upperBound!=null) && (upperBound instanceof DeclaredType)){
                    return new ParameterizedInfo((DeclaredType)upperBound,true);
                }
            }
            else if(parameter instanceof DeclaredType){
                return new ParameterizedInfo((DeclaredType)parameter,false);
            }
            return null;
        }

        public static ParameterizedInfo getParameterizedArgumentClass(ParameterizedType type, int pos){
            Type paramType = type.getActualTypeArguments()[pos];
            if(paramType instanceof Class){
                return new ParameterizedInfo((Class) paramType,false);
            }
            else if(paramType instanceof java.lang.reflect.WildcardType){
                Type[] types = ((java.lang.reflect.WildcardType)paramType).getUpperBounds();
                if(types.length>0){
                    if(types[0] instanceof Class){
                        return new ParameterizedInfo((Class) types[0],true);
                    }
                }
            }
            return null;
        }

        public ParameterizedInfo(DeclaredType field,boolean isWildcard){
            super(field);
            _isWildcard = isWildcard;
        }

        public ParameterizedInfo(Class clazz,boolean isWildcard){
            super(clazz);
            _isWildcard = isWildcard;
        }

        public boolean isWildcard() {
            return _isWildcard;
        }
    }

    public static class ClassInfo {
        private DeclaredType _modeType;
        private Class<?> _class = null;
        private boolean _isCollection;
        private boolean _isMap;

        /*public static ClassInfo getClassInfoFromType(Type type) {
            if (type instanceof Class) {
                return new ClassInfo((Class) type);
            } else if (type instanceof java.lang.reflect.WildcardType) {
                Type[] types = ((java.lang.reflect.WildcardType) type).getUpperBounds();
                if (types.length > 0) {
                    if (types[0] instanceof Class) {
                        return new ClassInfo((Class) types[0]);
                    }
                }
            }
            return null;
        }*/

        public ClassInfo(DeclaredType type) {
            _modeType = type;
            _class = AnnotationProcessorUtils.getClass(type);
            if ((type.asElement() instanceof TypeElement)) {
                TypeElement typeElement = (TypeElement) type.asElement();
                if (AnnotationProcessorUtils.isAssignableFrom(Collection.class, typeElement)) {
                    _isCollection = true;
                } else if (AnnotationProcessorUtils.isAssignableFrom(Map.class, typeElement)) {
                    _isMap = true;
                }
            }
        }

        public ClassInfo(Class clazz) {
            _class = clazz;
            if (Collection.class.isAssignableFrom(_class)) {
                _isCollection = true;
            } else if (Map.class.isAssignableFrom(_class)) {
                _isMap = true;
            }
        }

        public String getName() {
            if (_class != null) {
                return _class.getName();
            } else {
                return AnnotationProcessorUtils.getClassName((TypeElement) _modeType.asElement());
            }
        }

        public String getSimpleName(){
            if(_class!=null){
                return _class.getSimpleName();
            }
            else{
                return _modeType.asElement().getSimpleName().toString();
            }
        }

        public DeclaredType getModeType() {
            return _modeType;
        }

        public Class getRealClass() {
            return _class;
        }

        public boolean isCollection() {
            return _isCollection;
        }

        public boolean isMap() {
            return _isMap;
        }

        public <A extends Annotation> A getAnnotation(Class<A> clazz){
            if(_class!=null){
                return _class.getAnnotation(clazz);
            }
            else{
                return _modeType.asElement().getAnnotation(clazz);
            }
        }

        public boolean isInstanceOf(Class clazz){
            if(_class!=null){
                return clazz.isAssignableFrom(_class);
            }
            else {
                return AnnotationProcessorUtils.isAssignableFrom(clazz, (TypeElement) _modeType.asElement());
            }
        }
    }
}