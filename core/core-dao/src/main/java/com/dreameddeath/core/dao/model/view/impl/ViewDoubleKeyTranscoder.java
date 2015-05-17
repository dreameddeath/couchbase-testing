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

package com.dreameddeath.core.dao.model.view.impl;

import com.couchbase.client.java.document.json.JsonArray;
import com.dreameddeath.core.dao.model.view.IViewKeyTranscoder;

import java.util.Collection;

/**
 * Created by CEAJ8230 on 27/12/2014.
 */
public class ViewDoubleKeyTranscoder extends ViewDoubleTranscoder implements IViewKeyTranscoder<Double> {
    @Override public void key(com.couchbase.client.java.view.ViewQuery query, Double value) {query.key(value);}
    @Override public void keys(com.couchbase.client.java.view.ViewQuery query, Collection<Double> value) { query.keys(JsonArray.from(value.toArray()));}
    @Override public void startKey(com.couchbase.client.java.view.ViewQuery query, Double value) { query.startKey(value);}
    @Override public void endKey(com.couchbase.client.java.view.ViewQuery query, Double value) {query.endKey(value);}
}
