package com.dreameddeath.couchbase.core.process.remote.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 13/05/2016.
 */
public class EnumModel {
    public List<String> values=new ArrayList<>();
    public String shortName;
    public String packageName;
    public ClassInfo origClassInfo;
    public boolean forRequest=false;
    public boolean forResponse=false;

    public String getImportName(){
        return packageName+"."+shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getPackageName(){
        return packageName;
    }

    public String getOrigClassSimpleName(){ return origClassInfo.getSimpleName();}

    public boolean isForRequest() {
        return forRequest;
    }

    public boolean isForResponse() {
        return forResponse;
    }

    public List<String> getValues() {
        return values;
    }
}
