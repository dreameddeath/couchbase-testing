package com.dreameddeath.testing.dataset.model;

/**
 * Created by Christophe Jeunesse on 12/04/2016.
 */
public class DatasetMeta {
    private Dataset parent=null;
    private DatasetElement parentElement=null;
    private String path=null;
    private String name=null;

    public void setName(String name){
        this.name=name;
    }

    public String getName(){
        return name;
    }

    public void prepare(Dataset parent, DatasetElement parentElement, String path) {
        this.parent = parent;
        this.parentElement = parentElement;
        this.path = path;
    }
}
