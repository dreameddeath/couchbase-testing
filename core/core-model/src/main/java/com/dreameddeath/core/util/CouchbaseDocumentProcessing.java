package com.dreameddeath.core.util;

import com.dreameddeath.core.annotation.DocumentProperty;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by ceaj8230 on 20/12/2014.
 */
public class CouchbaseDocumentProcessing {

    public static Method fieldGetterFinder(Field field) throws NoSuchMethodException{
        if(field.getAnnotation(DocumentProperty.class)!=null){
            DocumentProperty prop = field.getAnnotation(DocumentProperty.class);
            String getter = prop.getter();
            if((getter!=null)&& !getter.equals("")){
                return field.getDeclaringClass().getDeclaredMethod(getter);
            }
            else {
                String name = prop.value();
                name = "get"+name.substring(0,1).toUpperCase()+name.substring(1);
                try {
                    return field.getDeclaringClass().getDeclaredMethod(name);
                }
                catch(NoSuchMethodException e){
                    //Do nothing
                }
            }
        }

        String name=field.getName();
        if(name.startsWith("_")){
            name = name.substring(1);
        }
        name = "get"+name.substring(0,1).toUpperCase()+name.substring(1);
        return field.getDeclaringClass().getDeclaredMethod(name);
    }

    List getClassElements(Class rootObj){
        for(Field member : rootObj.getDeclaredFields()) {
            DocumentProperty annot = member.getAnnotation(DocumentProperty.class);
            if (annot == null) continue;
            AccessibleObject getter = null;
            try {
                getter = fieldGetterFinder(member);
            } catch (NoSuchMethodException e) {
                if (member.isAccessible()) {
                    getter = member;
                } else {
                    //TODO throw an error
                }
            }
        }


        return null;
    }
}