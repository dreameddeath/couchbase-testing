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
    private DatasetDirective directive;
    private final DatasetValue value=new DatasetValue();
    //private List<DatasetValue> arrayElt=new ArrayList<>();
    //private DatasetObject objElt;

    public void setName(String name) {
        this.name = name;
    }

    public void setMvel(DatasetMvel mvelElement) {
        this.mvelElement = mvelElement;
        this.type = Type.MVEL;
    }

    public void setObject(DatasetObject objElt) {
        this.value.setObjectValue(objElt);
        this.type =Type.OBJECT;
    }

    public void setArray(List<DatasetValue> arrayElt) {
        this.value.setArrayValue(arrayElt);
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
            case OBJECT:return this.value.getObjVal();
            case ARRAY: return this.value.getArrayVal();
            case MVEL:return this.mvelElement;
            case DIRECTIVE:return this.directive;
            default : return null;
        }
    }

    public void prepare(Dataset parent){
        for (DatasetMeta datasetMeta : metaList) {
            datasetMeta.prepare(parent,this,"");
        }

        switch (type){
            case OBJECT:case ARRAY:
                this.value.prepare(parent,this,"");
                break;
            case MVEL:this.mvelElement.prepare(parent,this);break;
            case DIRECTIVE:this.directive.prepare(parent,this);break;
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
        return value.getObjVal();
    }

    public List<DatasetValue> getArray() {
        return value.getArrayVal();
    }

    public DatasetValue getValue(){
        return value;
    }

    public boolean isValue(){
        return type==Type.OBJECT||type==Type.ARRAY;
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
