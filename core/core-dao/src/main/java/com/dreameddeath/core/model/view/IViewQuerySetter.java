package com.dreameddeath.core.model.view;

import com.couchbase.client.java.view.ViewQuery;

import java.util.Collection;

/**
 * Created by CEAJ8230 on 27/12/2014.
 */
public interface IViewQuerySetter<T> {
    public void key(ViewQuery query,T value);
    public void keys(ViewQuery query,Collection<T> value);
    public void startKey(ViewQuery query,T value);
    public void endKey(ViewQuery query,T value);
}
