package com.dreameddeath.core.model.property;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ceaj8230 on 06/08/2014.
 */
public class HashMapProperty<K,V> extends HashMap<K,V> implements MapProperty<K,V> {
    CouchbaseDocumentElement _parentElt;
    public HashMapProperty(CouchbaseDocumentElement parentElement){
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
    public V remove(Object elt){
        V  resObj= super.remove(elt);
        if(resObj!=null){
            dirtyParent();
            if((resObj!=null) && (elt!=null) && (elt instanceof CouchbaseDocumentElement)){
                ((CouchbaseDocumentElement) elt).setParentElement(null);
            }
        }
        return resObj;
    }


    @Override
    public V put(K key,V value){
        V oldValue = super.put(key,value);
        if((oldValue!=null) &&(oldValue instanceof CouchbaseDocumentElement)){
            ((CouchbaseDocumentElement) oldValue).setParentElement(null);
        }
        if((key!=null) && (key instanceof CouchbaseDocumentElement)){
            ((CouchbaseDocumentElement) key).setParentElement(_parentElt);
        }
        if((value!=null) && (value instanceof CouchbaseDocumentElement)){
            ((CouchbaseDocumentElement) value).setParentElement(_parentElt);
        }
        dirtyParent();
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends K,? extends V> maps){
        for(Map.Entry<? extends K,? extends V> entry: maps.entrySet()){
            if((entry.getKey()!=null) && (entry.getKey() instanceof CouchbaseDocumentElement)){
                ((CouchbaseDocumentElement) entry.getKey()).setParentElement(_parentElt);
            }
            if((entry.getValue()!=null) && (entry.getValue() instanceof CouchbaseDocumentElement)){
                ((CouchbaseDocumentElement) entry.getValue()).setParentElement(_parentElt);
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
