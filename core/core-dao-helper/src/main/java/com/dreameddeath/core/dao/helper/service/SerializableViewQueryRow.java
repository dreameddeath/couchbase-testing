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

package com.dreameddeath.core.dao.helper.service;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.view.IViewQueryRow;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;


/**
 * Created by ceaj8230 on 16/04/2015.
 */
public class SerializableViewQueryRow<TKEY,TVALUE,TDOC extends CouchbaseDocument>{
    TKEY _key;
    TVALUE _value;
    String _docKey;

    public SerializableViewQueryRow(){}

    public SerializableViewQueryRow(IViewQueryRow<TKEY,TVALUE,TDOC> sourceRow){
        _docKey = sourceRow.getDocKey();
        _key = sourceRow.getKey();
        _value = sourceRow.getValue();
    }

    @JsonGetter("key")
    public TKEY getKey() {
        return _key;
    }

    @JsonSetter("key")
    public void setKey(TKEY key) {
        _key = key;
    }

    @JsonGetter("value")
    public TVALUE getValue() {
        return _value;
    }

    @JsonSetter("value")
    public void setValue(TVALUE value) {
        _value = value;
    }

    @JsonGetter("docKey")
    public String getDocKey() {
        return _docKey;
    }

    @JsonSetter("docKey")
    public void setDocKey(String docKey) {
        _docKey = docKey;
    }
}
