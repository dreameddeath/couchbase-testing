package com.dreameddeath.core.dao.archive;

import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import net.spy.memcached.transcoders.Transcoder;

/**
 * Created by CEAJ8230 on 17/09/2014.
 */
public abstract class CouchbaseArchiveDao<T extends CouchbaseDocument> {
    private CouchbaseClientWrapper _client;
    private Integer _expiration;
    public abstract Transcoder<T> getTranscoder();

    protected CouchbaseClientWrapper getClientWrapper(){
        return _client;
    }

    public CouchbaseArchiveDao(CouchbaseClientWrapper client,String key, Integer expiration){
        _client = client;
        _expiration = expiration;
    }
}
