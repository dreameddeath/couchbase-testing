package com.dreameddeath.core.storage;


import com.couchbase.client.java.transcoder.Transcoder;
import com.dreameddeath.core.model.common.RawCouchbaseDocument;

/**
 * Created by ceaj8230 on 21/11/2014.
 */
public interface ICouchbaseTranscoder<T extends RawCouchbaseDocument> extends Transcoder<BucketDocument<T>,T> {
    public BucketDocument<T> newDocument(T baseDocument);
}
