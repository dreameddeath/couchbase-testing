package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.HasParentDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.MapDefaultValueBuilder;
import com.dreameddeath.core.model.property.MapProperty;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class ArrayListProperty<T> extends ArrayList<T> implements ListProperty<T>,HasParentDocumentElement {
    BaseCouchbaseDocumentElement _parentElt;
    public ArrayListProperty(BaseCouchbaseDocumentElement parentElement){
        _parentElt=parentElement;
    }


    public void setParentDocumentElement(BaseCouchbaseDocumentElement parentElement){
        _parentElt=parentElement;
    }
    public BaseCouchbaseDocumentElement getParentDocumentElement(){return _parentElt;}

    protected boolean dirtyParent(){
        CouchbaseDocument rootDoc = _parentElt.getParentDocument();
        if(rootDoc!=null){ rootDoc.setDocStateDirty();}
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
        if(res && (elt!=null) && (elt instanceof HasParentDocumentElement)){
            ((HasParentDocumentElement) elt).setParentDocumentElement(null);
        }
        if(res){ dirtyParent(); }
        return res;
    }


    @Override
    public boolean add(T elt){
        if(elt instanceof HasParentDocumentElement){
            ((HasParentDocumentElement) elt).setParentDocumentElement(_parentElt);
        }
        dirtyParent();
        return super.add(elt);
    }

    @Override
    public boolean addAll(Collection<? extends T> list){
        for(T elt : list){
            if(elt instanceof HasParentDocumentElement){
                ((HasParentDocumentElement) elt).setParentDocumentElement(_parentElt);
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

    public static class MapValueBuilder<T> implements MapDefaultValueBuilder<List<T>>{
        public ListProperty<T> build(MapProperty<?,List<T>> map){
            BaseCouchbaseDocumentElement parent=null;
            if(map instanceof HasParentDocumentElement){
                parent = ((HasParentDocumentElement) map).getParentDocumentElement();
            }
            return new ArrayListProperty<T>(parent);
        }
    }

}
