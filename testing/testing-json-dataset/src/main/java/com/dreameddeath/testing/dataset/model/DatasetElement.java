package com.dreameddeath.testing.dataset.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 14/04/2016.
 */
public class DatasetElement {
    private String name;
    private Type type;
    private List<DatasetMeta> metaList=new ArrayList<>();
    private DatasetMvel mvelElement;
    private DatasetObject objElt;
    private DatasetDirective directive;
    private List<DatasetValue> arrayElt=new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public void setMvel(DatasetMvel mvelElement) {
        this.mvelElement = mvelElement;
        this.type = Type.MVEL;
    }

    public void setObject(DatasetObject objElt) {
        this.objElt = objElt;
        this.type =Type.OBJECT;
    }

    public void setArray(List<DatasetValue> arrayElt) {
        this.arrayElt.clear();
        this.arrayElt.addAll(arrayElt);
        this.type = Type.ARRAY;
    }

    public void addMeta(DatasetMeta meta){
        this.metaList.add(meta);
    }


    public void setDirective(DatasetDirective directive){
        this.directive=directive;
        this.type = Type.DIRECTIVE;
    }

    public Object getContent(){
        switch (type){
            case OBJECT:return this.objElt;
            case ARRAY: return Collections.unmodifiableList(arrayElt);
            case MVEL:return this.mvelElement;
            case DIRECTIVE:return this.directive;
            default : return null;
        }
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public List<DatasetMeta> getMetaList() {
        return Collections.unmodifiableList(metaList);
    }

    public DatasetMvel getMvel() {
        return mvelElement;
    }

    public DatasetObject getObject() {
        return objElt;
    }

    public List<DatasetValue> getArray() {
        return Collections.unmodifiableList(arrayElt);
    }

    public DatasetDirective getDirective(){
        return directive;
    }

    public enum Type{
        OBJECT,
        ARRAY,
        MVEL,
        DIRECTIVE
    }
}
