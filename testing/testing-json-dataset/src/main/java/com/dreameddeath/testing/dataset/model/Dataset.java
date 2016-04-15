package com.dreameddeath.testing.dataset.model;

import com.dreameddeath.testing.dataset.DatasetManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 14/04/2016.
 */
public class Dataset {
    private DatasetManager manager =null;
    private String name=null;

    private List<DatasetElement> elementList = new ArrayList<>();

    public void addElement(DatasetElement elt){
        this.elementList.add(elt);
        if(elt.getType()== DatasetElement.Type.DIRECTIVE){
            if(elt.getDirective().getType()== DatasetDirective.Type.DATASET_NAME){
                this.name = elt.getDirective().getParams().get(0);
            }
        }
    }

    public String getName() {
        return name;
    }

    public List<DatasetElement> getElements(){
        return Collections.unmodifiableList(elementList);
    }


    public List<DatasetDirective> getImportDirectives(){
        return this.elementList.stream()
                .filter(elem->elem.getType()==DatasetElement.Type.DIRECTIVE && elem.getDirective().getType()== DatasetDirective.Type.IMPORT)
                .map(DatasetElement::getDirective)
                .collect(Collectors.toList());
    }

    public void setManager(DatasetManager manager){
        this.manager = manager;
    }

    public DatasetManager getManager() {
        return manager;
    }
}
