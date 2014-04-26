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
    
    @Override
    public boolean add(T elt){
        ///TODO check NULL
        if(_duplicateStrategy.equals(DuplicateStrategy.ADD)){
            if(contains(elt)){
                if(_duplicateStrategy.equals(DuplicateStrategy.FAIL)){
                    ///TODO generate an error
                }
                return false;
            }
        }
        CouchbaseDocument rootDoc = _parentElt.getParentDocument();
        if(rootDoc!=null){ rootDoc.setStateDirty();}
        elt.setParentElement(_parentElt);
        return super.add(elt);
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
