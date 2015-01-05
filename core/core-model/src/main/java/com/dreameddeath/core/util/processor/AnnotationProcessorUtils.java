package com.dreameddeath.core.util.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;

/**
 * Created by CEAJ8230 on 05/01/2015.
 */
public class AnnotationProcessorUtils {
    public static String getClassName(TypeElement element){
        Element currElement = element;
        String result=element.getSimpleName().toString();
        while(currElement.getEnclosingElement()!=null){
            currElement = currElement.getEnclosingElement();
            if(currElement instanceof TypeElement){
                result = currElement.getSimpleName()+"$"+result;
            }
            else if(currElement instanceof PackageElement){
                if(!"".equals(currElement.getSimpleName())) {
                    result = ((PackageElement) currElement).getQualifiedName() + "." + result;
                }
            }
        }
        return result;
    }

    public static boolean isAssignableFrom(Class clazz,TypeElement element){
        TypeElement currElement = element;
        if(clazz.isInterface()){
            for(TypeMirror interfaceType: element.getInterfaces()){
                Element interfaceElement = ((DeclaredType) interfaceType).asElement();
                if(clazz.getName().equals(getClassName((TypeElement)interfaceElement))){
                    return true;
                }
            }
        }

        while (currElement != null) {
            if (getClassName(currElement).equals(clazz.getName())) {
                return true;
            }
            TypeMirror parent = currElement.getSuperclass();
            if(parent instanceof NoType){
                break;
            }
            if (parent instanceof DeclaredType) {
                currElement = (TypeElement) ((DeclaredType) parent).asElement();
            }
        }

        return false;
    }

    public static TypeElement getSuperClass(TypeElement element){
        TypeMirror parent = element.getSuperclass();
        if(parent instanceof DeclaredType){
            Element elt = ((DeclaredType) parent).asElement();
            if(elt instanceof TypeElement){
                return (TypeElement)elt;
            }
        }
        return null;
    }

    public static Class getClass(TypeElement element){
        try {
            return Class.forName(getClassName(element));
        }
        catch(Exception e){
            //System.out.println(e);
        }
        return null;
    }

    public static Class getClass(TypeMirror type){
        if(type instanceof DeclaredType){
            if(((DeclaredType)type).asElement() instanceof TypeElement) {
                return getClass((TypeElement) ((DeclaredType) type).asElement());
            }
        }
        return null;
    }
}
