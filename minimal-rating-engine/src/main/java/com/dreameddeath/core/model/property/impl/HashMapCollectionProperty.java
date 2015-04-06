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

import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
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
