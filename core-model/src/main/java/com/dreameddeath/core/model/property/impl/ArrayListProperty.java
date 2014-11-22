package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.property.HasParent;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.MapDefaultValueBuilder;
import com.dreameddeath.core.model.property.MapProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class ArrayListProperty<T> extends ArrayList<T> implements ListProperty<T>,HasParent {
    HasParent _parentElt;

    public ArrayListProperty(HasParent parentElement){
        _parentElt=parentElement;
    }

    @Override
    public void setParentElement(HasParent parentElement){_parentElt=parentElement;}
    @Override
    public HasParent getParentElement(){return _parentElt;}

    @Override
    public void clear(){
        HasParent.Helper.dirtyParentDocument(this);
        super.clear();
    }

    @Override
    public boolean remove(Object elt){
        boolean  res = super.remove(elt);
        if(res && (elt!=null) && (elt instanceof HasParent)){
            ((HasParent) elt).setParentElement(null);
        }
        if(res){ HasParent.Helper.dirtyParentDocument(this); }
        return res;
    }

    @Override
    public T remove(int index){
        T res = super.remove(index);
        if(res!=null){
            HasParent.Helper.dirtyParentDocument(this);
            if(res instanceof HasParent) {
                ((HasParent) res).setParentElement(null);
            }
        }
        return res;
    }


    @Override
    public boolean add(T elt){
        if(elt instanceof HasParent){
            ((HasParent) elt).setParentElement(_parentElt);
        }
        HasParent.Helper.dirtyParentDocument(this);
        return super.add(elt);
    }

    @Override
    public void add(int index,T elt){
        if(elt instanceof HasParent){
            ((HasParent) elt).setParentElement(_parentElt);
        }
        HasParent.Helper.dirtyParentDocument(this);
        super.add(index,elt);
    }

    @Override
    public boolean addAll(Collection<? extends T> list){
        for(T elt : list){
            if(elt instanceof HasParent){
                ((HasParent) elt).setParentElement(_parentElt);
            }
        }
        HasParent.Helper.dirtyParentDocument(this);
        return super.addAll(list);
    }

    @Override
    public boolean addAll(int index,Collection<? extends T> list){
        for(T elt : list){
            if(elt instanceof HasParent){
                ((HasParent) elt).setParentElement(_parentElt);
            }
        }
        HasParent.Helper.dirtyParentDocument(this);
        return super.addAll(index,list);
    }

    @Override
    public List<T> get(){
        return Collections.unmodifiableList(this);
    }

    @Override
    public boolean set(Collection<T> list){
        HasParent.Helper.dirtyParentDocument(this);
        clear();
        return addAll(list);
    }

    @Override
    public T set(int index,T elt){
        T res = super.set(index,elt);
        if(elt instanceof HasParent){
            ((HasParent) elt).setParentElement(_parentElt);
        }
        HasParent.Helper.dirtyParentDocument(this);

        if((res!=null)&& (res instanceof HasParent)){
            ((HasParent) res).setParentElement(null);
        }
        return res;
    }


    @Override
    public boolean set(List<T> list){
        return set((Collection<T>)list);
    }

    public static class MapValueBuilder<T> implements MapDefaultValueBuilder<List<T>>{
        public ListProperty<T> build(MapProperty<?,List<T>> map){
            HasParent parent=null;
            if(map instanceof HasParent){
                parent = ((HasParent) map).getParentElement();
            }
            return new ArrayListProperty<T>(parent);
        }
    }

}
