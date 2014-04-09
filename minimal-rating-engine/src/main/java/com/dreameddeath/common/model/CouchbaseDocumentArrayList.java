package com.dreameddeath.common.model;

import java.util.ArrayList;
import java.util.Collection;
import com.dreameddeath.common.annotation.CouchbaseCollectionField;

@CouchbaseCollectionField
public class CouchbaseDocumentArrayList<T extends CouchbaseDocumentElement> extends ArrayList<T>{
    private CouchbaseDocumentElement _parentElt;
    
    public CouchbaseDocumentArrayList(CouchbaseDocumentElement parentElt){
        _parentElt=parentElt;
    }
    
    @Override
    public boolean add(T elt){
        elt.setParentDocument(_parentElt.getParentDocument());
        return super.add(elt);
    }
}
