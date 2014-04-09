package com.dreameddeath.common.model;

import java.util.ArrayList;
import java.util.Collection;
import com.dreameddeath.common.annotation.CouchbaseCollectionField;

@CouchbaseCollectionField
public class CouchbaseDocumentArrayList<T> extends ArrayList<T>{
    private CouchbaseDocument _parent;
    
    public CouchbaseDocumentArrayList(CouchbaseDocument parent){
        _parent=parent;
    }
    @Override
    public boolean add(T elt){
        if(elt instanceof CouchbaseDocumentLink){
            ((CouchbaseDocumentLink)elt).setSourceObject(_parent);
        }
        return super.add(elt);
    }
    
    /*@Override
    public boolean addAll(Collection<? extends T> elts){
        for(T elt:elts){
            if(elt instanceof CouchbaseDocumentLink){
                ((CouchbaseDocumentLink)elt).setSourceObject(_parent);
            }
        }
        return super.addAll(elts);
    }*/
}
