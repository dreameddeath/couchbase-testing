package com.dreameddeath.core.dao.archive;

import com.couchbase.client.java.transcoder.Transcoder;
import com.dreameddeath.core.model.common.BucketDocument;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.storage.CouchbaseBucketWrapper;

/**
 * Created by CEAJ8230 on 17/09/2014.
 */
public abstract class CouchbaseArchiveDao<T extends CouchbaseDocument> {
    private CouchbaseBucketWrapper _client;
    private Integer _expiration;
    public abstract Transcoder<BucketDocument<T>,T> getTranscoder();

    protected CouchbaseBucketWrapper getClientWrapper(){
        return _client;
    }

    public CouchbaseArchiveDao(CouchbaseBucketWrapper client,String key, Integer expiration){
        _client = client;
        _expiration = expiration;
    }
}
