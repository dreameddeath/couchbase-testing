package com.dreameddeath.common.model;

import java.util.ArrayList;
import java.util.Collection;
import com.dreameddeath.common.annotation.CouchbaseCollection;

@CouchbaseCollection
public class CouchbaseDocumentArrayList<T extends CouchbaseDocumentElement> extends ArrayList<T>{
    private CouchbaseDocumentElement _parentElt;
    private DuplicateStrategy _duplicateStrategy;
    
    public CouchbaseDocumentArrayList(CouchbaseDocumentElement parentElt){
        this(parentElt,DuplicateStrategy.SKIP);
    }
    
    public CouchbaseDocumentArrayList(CouchbaseDocumentElement parentElt,DuplicateStrategy duplicateStrategy){
        _parentElt=parentElt;
        _duplicateStrategy=duplicateStrategy;
    }
    
    protected boolean manageElt(T elt){
        ///TODO check NULL
        if(contains(elt)){
            switch(_duplicateStrategy){
                case FAIL:
                    ///TODO generated an error
                    return false;
                case SKIP:
                    return false;
            }
        }
        CouchbaseDocument rootDoc = _parentElt.getParentDocument();
        if(rootDoc!=null){ rootDoc.setStateDirty();}
        System.out.println("Adding elt "+elt+" of type "+elt.getClass().getName() +" to parent "+_parentElt);
        elt.setParentElement(_parentElt);
        return true;
    }
    
    @Override
    public boolean add(T elt){
        return manageElt(elt) && super.add(elt);
    }
    
    @Override
    public boolean addAll(Collection<? extends T> list){
        for(T elt:list){
            manageElt(elt);
        }
        return super.addAll(list);
    }
    
    public enum DuplicateStrategy{
        ADD,
        FAIL,
        SKIP;
    }
    
    public boolean validate(){
        boolean result=true;
        for(T elt : this){
            result &=elt.validate();
        }
        return result;
    }
}
