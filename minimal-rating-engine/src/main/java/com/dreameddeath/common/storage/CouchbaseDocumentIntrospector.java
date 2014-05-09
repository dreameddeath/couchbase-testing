package com.dreameddeath.common.storage;

import java.lang.reflect.Field;


import com.dreameddeath.common.annotation.DocumentProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.PropertyName;


public class CouchbaseDocumentIntrospector extends JacksonAnnotationIntrospector implements
        Versioned {

    @Override
    public Version version() {
        return VersionUtil.versionFor(getClass());
    }

    @Override
    public PropertyName findNameForSerialization(Annotated a)
    {
        PropertyName name = super.findNameForSerialization(a);
        if(name==null) {
            if (a instanceof AnnotatedMethod){
                AnnotatedMethod am = (AnnotatedMethod) a;
                if(am.getName().startsWith("get") && (am.getName().length()>3)){
                    String fieldName = am.getName().substring(3,4).toLowerCase()+am.getName().substring(4);

                    for(Field field: am.getDeclaringClass().getDeclaredFields()) {
                        DocumentProperty fieldProp = field.getAnnotation(DocumentProperty.class);
                        if(fieldProp==null) continue;
                        if(am.getName().equals(fieldProp.getter())){
                            name = new PropertyName(fieldProp.value());
                            break;
                        }
                        else if(fieldName.equals(fieldProp.value())){
                            name=new PropertyName(fieldProp.value());
                        }
                    }
                }
            }
        }

        return name;
    }

    @Override
    public PropertyName findNameForDeserialization(Annotated a)
    {
        PropertyName name = super.findNameForSerialization(a);
        if(name==null) {
            DocumentProperty annotation= a.getAnnotation(DocumentProperty.class);
            if (a instanceof AnnotatedMethod){
                AnnotatedMethod am = (AnnotatedMethod) a;
                if(am.getName().startsWith("set")) {
                    if(am.getDeclaringClass().getSimpleName().startsWith("AbstractRating")){
                        String test = "sample";

                    }
                    for(Field field: am.getDeclaringClass().getDeclaredFields()) {
                        DocumentProperty fieldProp = field.getAnnotation(DocumentProperty.class);
                        if((fieldProp!=null) && (am.getName().equals(fieldProp.setter()))){
                            name = new PropertyName(fieldProp.value());
                        }
                    }
                }
            }
        }

        return name;
    }
}