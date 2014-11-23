package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.property.HasParent;
import com.dreameddeath.core.model.property.SetProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ceaj8230 on 08/08/2014.
 */
public class HashSetProperty<T> extends HashSet<T> implements SetProperty<T>,HasParent {
    HasParent _parentElt;

    public HashSetProperty(HasParent parentElement){
        _parentElt=parentElement;
    }
    @Override
    public void setParentElement(HasParent parentElement){ _parentElt=parentElement;}
    @Override
    public HasParent getParentElement(){ return _parentElt;}


    @Override
    public void clear(){
        HasParent.Helper.dirtyParentDocument(this);
        super.clear();
    }

    @Override
    public boolean remove(Object elt){
        boolean res = super.remove(elt);
        if(res){
            HasParent.Helper.dirtyParentDocument(this);
            if((elt!=null) && (elt instanceof HasParent)){
                ((HasParent) elt).setParentElement(null);
            }
        }
        return res;
    }

    @Override
    public boolean removeAll(Collection<?> elts){
        for(Object elt:elts) {
            if ((elt != null) && (elt instanceof HasParent)) {
                ((HasParent) elt).setParentElement(null);
            }
        }
        return super.removeAll(elts);
    }

    @Override
    public boolean add(T elt){
        HasParent.Helper.dirtyParentDocument(this);
        if((elt!=null) && (elt instanceof HasParent)){
            ((HasParent) elt).setParentElement(_parentElt);
        }
        return super.add(elt);
    }

    @Override
    public boolean addAll(Collection<? extends T> elts){
        HasParent.Helper.dirtyParentDocument(this);
        for(T elt:elts) {
            if ((elt != null) && (elt instanceof HasParent)) {
                ((HasParent) elt).setParentElement(_parentElt);
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
