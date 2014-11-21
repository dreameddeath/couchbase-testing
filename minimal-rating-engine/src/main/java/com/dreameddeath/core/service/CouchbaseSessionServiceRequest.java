package com.dreameddeath.core.service;

import com.dreameddeath.core.CouchbaseSession;

/**
 * Created by ceaj8230 on 03/11/2014.
 */
public interface CouchbaseSessionServiceRequest<T> {
    public CouchbaseSessionServiceContext getContext();
    public T getRequest();
}
