package com.dreameddeath.core.model.view.impl;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.view.ViewQuery;
import com.dreameddeath.core.model.view.IViewKeyTranscoder;

import java.util.Collection;

/**
 * Created by CEAJ8230 on 27/12/2014.
 */
public class ViewStringKeyTranscoder extends ViewStringTranscoder implements IViewKeyTranscoder<String> {
@Override public void key(ViewQuery query, String value) {query.key(value);}
@Override public void keys(ViewQuery query, Collection<String> value) { query.keys(JsonArray.from(value.toArray()));}
@Override public void startKey(ViewQuery query, String value) { query.startKey(value);}
@Override public void endKey(ViewQuery query, String value) {query.endKey(value);}
}
