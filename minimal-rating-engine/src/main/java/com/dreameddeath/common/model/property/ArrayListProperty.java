package com.dreameddeath.common.model.property;

import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.CouchbaseDocumentElement;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by ceaj8230 on 21/05/2014.
 */
public class ArrayListProperty<T> extends ArrayList<T> {
    CouchbaseDocumentElement _parentElt;
    public ArrayListProperty(CouchbaseDocumentElement parentElement){
        _parentElt=parentElement;
    }

    protected boolean dirtyParent(){
        CouchbaseDocument rootDoc = _parentElt.getParentDocument();
        if(rootDoc!=null){ rootDoc.setStateDirty();}
        return true;
    }

    @Override
    public boolean add(T elt){
        dirtyParent();
        return super.add(elt);
    }

    @Override
    public boolean addAll(Collection<? extends T> list){
        dirtyParent();
        return super.addAll(list);
    }
}
