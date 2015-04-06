/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.document.BaseCouchbaseDocument;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.HasParentDocumentElement;
import com.dreameddeath.core.model.property.MapProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ceaj8230 on 06/08/2014.
 */
public class HashMapProperty<K,V> extends HashMap<K,V> implements MapProperty<K,V>,HasParentDocumentElement {
    BaseCouchbaseDocumentElement _parentElt;
    public HashMapProperty(BaseCouchbaseDocumentElement parentElement){
        _parentElt=parentElement;
    }

    public void setParentDocumentElement(BaseCouchbaseDocumentElement parentElement){ _parentElt=parentElement;}
    public BaseCouchbaseDocumentElement getParentDocumentElement(){return _parentElt;}

    protected boolean dirtyParent(){
        BaseCouchbaseDocument rootDoc = _parentElt.getParentDocument();
        if(rootDoc!=null){ rootDoc.getBaseMeta().setStateDirty();}
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
