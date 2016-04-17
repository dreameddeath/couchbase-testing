package com.dreameddeath.testing.dataset.model;

import com.dreameddeath.testing.dataset.DatasetManager;
import org.mvel2.ParserContext;
import org.slf4j.Logger;

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
    private ParserContext mvel2Ctxt=null;

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

    public void setName(String name) {
        this.name = name;
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

    public void prepare(){
        for(DatasetElement element:elementList){
            element.prepare(this);
        }
    }

    synchronized public ParserContext getParserContext(){
        if(mvel2Ctxt==null){
            mvel2Ctxt = new ParserContext();
            mvel2Ctxt.setStrongTyping(true);
            mvel2Ctxt.setStrictTypeEnforcement(true);
            mvel2Ctxt.addVariable("globalManager",DatasetManager.class,true);
            mvel2Ctxt.addVariable("globalDataset",Dataset.class,true);
            mvel2Ctxt.addVariable("log",Logger.class,true);
        }
        return mvel2Ctxt;
    }
}
