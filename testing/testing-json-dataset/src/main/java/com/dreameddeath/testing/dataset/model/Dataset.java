package com.dreameddeath.testing.dataset.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 14/04/2016.
 */
public class Dataset {
    private List<DatasetElement> elementList = new ArrayList<>();

    public void addElement(DatasetElement elt){
        this.elementList.add(elt);
    }

    public List<DatasetElement> getElements(){
        return Collections.unmodifiableList(elementList);
    }
}
