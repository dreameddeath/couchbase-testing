package com.dreameddeath.testing.dataset.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 14/04/2016.
 */
public class DatasetObject {
    private List<DatasetObjectNode> nodes = new ArrayList<>();

    public void addNode(DatasetObjectNode node){
        nodes.add(node);
    }

    public List<DatasetObjectNode> getNodes(){
        return Collections.unmodifiableList(nodes);
    }
}
