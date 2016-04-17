package com.dreameddeath.testing.dataset.model;


import com.dreameddeath.core.java.utils.StringUtils;

/**
 * Created by Christophe Jeunesse on 12/04/2016.
 */
public class DatasetObjectNode {
    private Dataset parent=null;
    private DatasetElement parentElement = null;
    private String path=null;

    private DatasetXPath xpath;
    private DatasetValue value;

    public void setXPath(DatasetXPath xpath){
        this.xpath = xpath;
    }

    public void setValue(DatasetValue value){
        this.value = value;
    }

    public DatasetXPath getXPath() {
        return xpath;
    }

    public DatasetValue getValue() {
        return value;
    }

    public void prepare(Dataset parent, DatasetElement parentElt, String path) {
        this.parent = parent;
        this.parentElement = parentElt;
        this.path =path;
        this.xpath.prepare(parent,parentElt,path);
        this.value.prepare(parent,parentElt,(StringUtils.isNotEmpty(path)?path+".":"")+this.xpath.getPath());
    }
}
