package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.MapDefaultValueBuilder;

import java.util.Collection;

/**
 * Created by ceaj8230 on 07/09/2014.
 */
public class HashMapCollectionProperty<K,V extends Collection> extends HashMapProperty<K,V> {
    MapDefaultValueBuilder<V> _defaultValueBuilder;

    public HashMapCollectionProperty(BaseCouchbaseDocumentElement parentElement,MapDefaultValueBuilder<V> builder){
        super(parentElement);
        _defaultValueBuilder=builder;
    }

    @Override
    public V put(K key,V value){
        V newValue=_defaultValueBuilder.build(this);
        newValue.addAll(value);
        return super.put(key,value);
    }

}
