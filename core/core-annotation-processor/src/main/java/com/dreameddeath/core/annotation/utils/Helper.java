package com.dreameddeath.core.annotation.utils;

import com.dreameddeath.core.annotation.DocumentDef;

import javax.lang.model.element.Element;

/**
 * Created by ceaj8230 on 27/11/2014.
 */
public class Helper {
    private static String ROOT_PATH="META-INF/core-annotation";

    public static String getDocumentEntityRootFilename(){
        return ROOT_PATH+"/DocumentDef/";
    }
    public static String getDocumentEntityFilename(String domain,String name, String version){
        return getDocumentEntityRootFilename()+domain+"."+name+".v"+version.split("\\.")[0];
    }

    public static String getFilename(DocumentDef annotation,Element elt){
        String name = annotation.name();
        if("".equals(annotation.name())){
            name = elt.getSimpleName().toString();
        }
        return getDocumentEntityFilename(annotation.domain(),name,annotation.version());
    }

    public static String getFilename(DocumentDef annotation,Class<?> clazz){
        String name = annotation.name();
        if("".equals(annotation.name())){
            name = clazz.getSimpleName();
        }
        return getDocumentEntityFilename(annotation.domain(),name,annotation.version());
    }
}
