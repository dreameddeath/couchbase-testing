package com.dreameddeath.core.model.view.impl;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.view.*;
import com.dreameddeath.core.model.view.IViewKeyTranscoder;

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
