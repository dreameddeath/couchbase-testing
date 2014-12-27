package com.dreameddeath.core.model.view.impl;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.view.*;
import com.dreameddeath.core.model.view.IViewKeyTranscoder;

import java.util.Collection;

/**
 * Created by CEAJ8230 on 27/12/2014.
 */
public class ViewLongKeyTranscoder extends ViewLongTranscoder implements IViewKeyTranscoder<Long> {
    @Override public void key(com.couchbase.client.java.view.ViewQuery query, Long value) {query.key(value);}
    @Override public void keys(com.couchbase.client.java.view.ViewQuery query, Collection<Long> value) { query.keys(JsonArray.from(value.toArray()));}
    @Override public void startKey(com.couchbase.client.java.view.ViewQuery query, Long value) { query.startKey(value);}
    @Override public void endKey(com.couchbase.client.java.view.ViewQuery query, Long value) {query.endKey(value);}
}
