package com.dreameddeath.core.service;

/**
 * Created by ceaj8230 on 03/11/2014.
 */
public interface CouchbaseSessionService<TREQ extends CouchbaseSessionServiceRequest,TRES> {
    public CouchbaseSessionServiceContext getContext();
    public TRES execute(TREQ request);
}
