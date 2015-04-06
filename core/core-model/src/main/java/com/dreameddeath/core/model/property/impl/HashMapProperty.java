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

import com.dreameddeath.core.model.property.HasParent;
import com.dreameddeath.core.model.property.MapProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ceaj8230 on 06/08/2014.
 */
public class HashMapProperty<K,V> extends HashMap<K,V> implements MapProperty<K,V>,HasParent {
    HasParent _parentElt;
    public HashMapProperty(HasParent parentElement){
        _parentElt=parentElement;
    }

    @Override
    public void setParentElement(HasParent parentElement){ _parentElt=parentElement;}
    @Override
    public HasParent getParentElement(){return _parentElt;}



    @Override
    public void clear(){
        HasParent.Helper.dirtyParentDocument(this);
        super.clear();
    }

    @Override
    public V remove(Object elt){
        V  resObj= super.remove(elt);
        if(resObj!=null){
            HasParent.Helper.dirtyParentDocument(this);
            if(resObj instanceof HasParent){
                ((HasParent) resObj).setParentElement(null);
            }
        }
        return resObj;
    }


    @Override
    public V put(K key,V value){
        V oldValue = super.put(key,value);
        if((oldValue!=null) &&(oldValue instanceof HasParent)){
            ((HasParent) oldValue).setParentElement(null);
        }
        if((key!=null) && (key instanceof HasParent)){
            ((HasParent) key).setParentElement(_parentElt);
        }
        if((value!=null) && (value instanceof HasParent)){
            ((HasParent) value).setParentElement(_parentElt);
        }
        HasParent.Helper.dirtyParentDocument(this);
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends K,? extends V> maps){
        for(Map.Entry<? extends K,? extends V> entry: maps.entrySet()){
            if((entry.getKey()!=null) && (entry.getKey() instanceof HasParent)){
                ((HasParent) entry.getKey()).setParentElement(_parentElt);
            }
            if((entry.getValue()!=null) && (entry.getValue() instanceof HasParent)){
                ((HasParent) entry.getValue()).setParentElement(_parentElt);
            }
        }
        HasParent.Helper.dirtyParentDocument(this);
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
