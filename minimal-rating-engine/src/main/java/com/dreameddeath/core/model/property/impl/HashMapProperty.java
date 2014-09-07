package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.HasParentDocumentElement;
import com.dreameddeath.core.model.property.MapDefaultValueBuilder;
import com.dreameddeath.core.model.property.MapProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ceaj8230 on 06/08/2014.
 */
public class HashMapProperty<K,V> extends HashMap<K,V> implements MapProperty<K,V>,HasParentDocumentElement {
    CouchbaseDocumentElement _parentElt;
    public HashMapProperty(CouchbaseDocumentElement parentElement){
        _parentElt=parentElement;
    }

    public void setParentDocumentElement(CouchbaseDocumentElement parentElement){ _parentElt=parentElement;}
    public CouchbaseDocumentElement getParentDocumentElement(){return _parentElt;}

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
    public V remove(Object elt){
        V  resObj= super.remove(elt);
        if(resObj!=null){
            dirtyParent();
            if(resObj instanceof HasParentDocumentElement){
                ((HasParentDocumentElement) resObj).setParentDocumentElement(null);
            }
        }
        return resObj;
    }


    @Override
    public V put(K key,V value){
        V oldValue = super.put(key,value);
        if((oldValue!=null) &&(oldValue instanceof HasParentDocumentElement)){
            ((HasParentDocumentElement) oldValue).setParentDocumentElement(null);
        }
        if((key!=null) && (key instanceof HasParentDocumentElement)){
            ((HasParentDocumentElement) key).setParentDocumentElement(_parentElt);
        }
        if((value!=null) && (value instanceof HasParentDocumentElement)){
            ((HasParentDocumentElement) value).setParentDocumentElement(_parentElt);
        }
        dirtyParent();
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends K,? extends V> maps){
        for(Map.Entry<? extends K,? extends V> entry: maps.entrySet()){
            if((entry.getKey()!=null) && (entry.getKey() instanceof HasParentDocumentElement)){
                ((HasParentDocumentElement) entry.getKey()).setParentDocumentElement(_parentElt);
            }
            if((entry.getValue()!=null) && (entry.getValue() instanceof HasParentDocumentElement)){
                ((HasParentDocumentElement) entry.getValue()).setParentDocumentElement(_parentElt);
            }
        }
        dirtyParent();
        super.putAll(maps);
    }


    @Override
    public boolean set(Map<K,V> map){
        clear();
        super.putAll(map);
        return true;
    }

    public Map<K,V> get(){
        return Collections.unmodifiableMap(this);
    }
}
