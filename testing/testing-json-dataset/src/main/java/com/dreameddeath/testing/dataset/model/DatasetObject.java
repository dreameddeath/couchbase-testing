package com.dreameddeath.testing.dataset.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 14/04/2016.
 */
public class DatasetObject {
    private Dataset parent=null;
    private DatasetElement parentElement=null;
    private String path=null;
    private final List<DatasetObjectNode> nodes = new ArrayList<>();

    public void addNode(DatasetObjectNode node){
        nodes.add(node);
    }

    public List<DatasetObjectNode> getNodes(){
        return Collections.unmodifiableList(nodes);
    }

    public void prepare(Dataset parent,DatasetElement parentElt){
        this.prepare(parent,parentElt,"");
    }

    public void prepare(Dataset parent,DatasetElement parentElt,String path){
        this.parent=parent;
        this.parentElement = parentElt;
        nodes.forEach(node->node.prepare(parent,parentElt,path));
    }
}
