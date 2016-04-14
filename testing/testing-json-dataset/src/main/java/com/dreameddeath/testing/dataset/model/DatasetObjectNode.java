package com.dreameddeath.testing.dataset.model;


/**
 * Created by Christophe Jeunesse on 12/04/2016.
 */
public class DatasetObjectNode {
    private DatasetXPath path;
    private DatasetValue value;

    public void setPath(DatasetXPath path){
        this.path = path;
    }

    public void setValue(DatasetValue value){
        this.value = value;
    }

    public DatasetXPath getPath() {
        return path;
    }

    public DatasetValue getValue() {
        return value;
    }
}
