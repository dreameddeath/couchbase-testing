package com.dreameddeath.core.model.property;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class ArrayListProperty<T> extends ArrayList<T> implements ListProperty<T> {
    CouchbaseDocumentElement _parentElt;
    public ArrayListProperty(CouchbaseDocumentElement parentElement){
        _parentElt=parentElement;
    }

    protected CouchbaseDocumentElement getParentElement(){
        return _parentElt;
    }
    protected boolean dirtyParent(){
        CouchbaseDocument rootDoc = _parentElt.getParentDocument();
        if(rootDoc!=null){ rootDoc.setStateDirty();}
        return true;
    }

    @Override
    public void clear(){
        dirtyParent();
        super.clear();
    }

    @Override
    public boolean remove(Object elt){
        boolean  res = super.remove(elt);
        if(res && (elt!=null) && (elt instanceof CouchbaseDocumentElement)){
            ((CouchbaseDocumentElement) elt).setParentElement(null);
        }
        if(res){ dirtyParent(); }
        return res;
    }


    @Override
    public boolean add(T elt){
        if(elt instanceof CouchbaseDocumentElement){
            ((CouchbaseDocumentElement) elt).setParentElement(_parentElt);
        }
        dirtyParent();
        return super.add(elt);
    }

    @Override
    public boolean addAll(Collection<? extends T> list){
        for(T elt : list){
            if(elt instanceof CouchbaseDocumentElement){
                ((CouchbaseDocumentElement) elt).setParentElement(_parentElt);
            }
        }
        dirtyParent();
        return super.addAll(list);
    }

    public List<T> get(){
        return Collections.unmodifiableList(this);
    }

    public boolean set(Collection<T> list){
        dirtyParent();
        clear();
        return addAll(list);
    }

    public boolean set(List<T> list){
        return set((Collection<T>)list);
    }

}
