package com.dreameddeath.testing.dataset.utils;

/**
 * Created by Christophe Jeunesse on 15/04/2016.
 */
public class DatasetUtils {

    public static String parseJavaEncodedString(String string){
        return string.substring(1,string.length()-1)
                .replaceAll("\\\\(.)","$1");
    }
}
