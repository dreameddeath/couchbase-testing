package com.dreameddeath.core.model.property;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

import java.util.*;

/**
 * Created by ceaj8230 on 08/08/2014.
 */
public class HashSetProperty<T> extends HashSet<T> implements SetProperty<T> {
    CouchbaseDocumentElement _parentElt;

    public HashSetProperty(CouchbaseDocumentElement parentElement){
        _parentElt=parentElement;
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
        boolean res = super.remove(elt);
        if(res){
            dirtyParent();
            if((elt!=null) && (elt instanceof CouchbaseDocumentElement)){
                ((CouchbaseDocumentElement) elt).setParentElement(null);
            }
        }
        return res;
    }

    @Override
    public boolean removeAll(Collection<?> elts){
        for(Object elt:elts) {
            if ((elt != null) && (elt instanceof CouchbaseDocumentElement)) {
                ((CouchbaseDocumentElement) elt).setParentElement(null);
            }
        }
        return super.removeAll(elts);
    }

    @Override
    public boolean add(T elt){
        dirtyParent();
        if((elt!=null) && (elt instanceof CouchbaseDocumentElement)){
            ((CouchbaseDocumentElement) elt).setParentElement(_parentElt);
        }
        return super.add(elt);
    }

    @Override
    public boolean addAll(Collection<? extends T> elts){
        dirtyParent();
        for(T elt:elts) {
            if ((elt != null) && (elt instanceof CouchbaseDocumentElement)) {
                ((CouchbaseDocumentElement) elt).setParentElement(_parentElt);
            }
        }
        return super.addAll(elts);
    }


    public Set<T> get(){
        return Collections.unmodifiableSet(this);
    }

    public boolean set(Collection<T> list){
        clear();
        return addAll(list);
    }

    public boolean set(Set<T> list){return set((Collection<T>)list);    }
}
