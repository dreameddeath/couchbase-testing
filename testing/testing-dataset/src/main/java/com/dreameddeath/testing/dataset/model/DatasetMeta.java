package com.dreameddeath.testing.dataset.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 12/04/2016.
 */
public class DatasetMeta {
    private Dataset parent=null;
    private DatasetElement parentElement=null;
    private Type type=Type.UNKNOWN;
    private String path=null;
    private String name=null;
    private List<DatasetValue> params=new ArrayList<>();

    public void setName(String name){
        this.name=name;
        this.type = Type.getFromName(name);
    }

    public String getName(){
        return name;
    }

    public void prepare(Dataset parent, DatasetElement parentElement, String path) {
        this.parent = parent;
        this.parentElement = parentElement;
        this.path = path;
    }

    public Type getType() {
        return type;
    }

    public void addParam(DatasetValue value){
        this.params.add(value);
    }

    public List<DatasetValue> getParams(){
        return Collections.unmodifiableList(params);
    }

    public <T> T getParam(int pos,Class<T> clazz){
        return params.get(pos).getContent(clazz);
    }

    public enum Type{

        NOT_NULL("NotNull"),
        NOT_EXISTING("NotExisting"),
        CONTAINS("Contains"),
        MATCH("Match"),
        NB_VALUES("NbValues"),
        LT("LowerThan"),
        LTE("LowerThanOrEqual"),
        GT("HigherThan"),
        GTE("HigherThanOrEqual"),
        TYPE("Type"),
        EVAL("Eval"),
        UNKNOWN("");

        private String name;

        Type(String name){
            this.name= name;
        }

        public String getName(){
            return name;
        }

        public static Type getFromName(String name){
            for(Type type:Type.values()){
                if(type.name.equalsIgnoreCase(name)){
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
}
