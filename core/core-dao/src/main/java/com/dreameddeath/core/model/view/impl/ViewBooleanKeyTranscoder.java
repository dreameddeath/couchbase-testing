package com.dreameddeath.core.model.view.impl;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.view.ViewQuery;
import com.dreameddeath.core.model.view.IViewKeyTranscoder;

import java.util.Collection;

/**
 * Created by CEAJ8230 on 27/12/2014.
 */
public class ViewBooleanKeyTranscoder extends ViewBooleanTranscoder implements IViewKeyTranscoder<Boolean> {
    @Override public void key(ViewQuery query, Boolean value) {query.key(value);}
    @Override public void keys(ViewQuery query, Collection<Boolean> value) { query.keys(JsonArray.from(value.toArray()));}
    @Override public void startKey(ViewQuery query, Boolean value) { query.startKey(value);}
    @Override public void endKey(ViewQuery query, Boolean value) {query.endKey(value);}
}
