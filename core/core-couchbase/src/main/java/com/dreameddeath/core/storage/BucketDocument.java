package com.dreameddeath.core.storage;

import com.couchbase.client.java.document.Document;
import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by ceaj8230 on 10/10/2014.
 */
public abstract class BucketDocument<T extends CouchbaseDocument> implements Document<T> {
    final private T _doc;

    public BucketDocument(T doc){ _doc = doc;}
    @Override
    public String id() {
        return _doc.getBaseMeta().getKey();
    }

    @Override
    public T content() {
        return _doc;
    }

    @Override
    public long cas() {
        return _doc.getBaseMeta().getCas();
    }

    @Override
    public int expiry() {
        return _doc.getBaseMeta().getExpiry();
    }

    public void syncMeta(BucketDocument<T> ref){
        _doc.getBaseMeta().setCas(ref._doc.getBaseMeta().getCas());
        _doc.getBaseMeta().setExpiry(ref._doc.getBaseMeta().getExpiry());
        _doc.getBaseMeta().setDbSize(ref._doc.getBaseMeta().getDbSize());
    }

    public T getDocument(){
        return _doc;
    }
}
