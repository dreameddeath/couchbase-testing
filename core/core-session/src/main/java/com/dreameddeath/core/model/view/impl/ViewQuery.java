package com.dreameddeath.core.model.view.impl;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.view.Stale;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.view.IViewQuery;
import com.dreameddeath.core.storage.ICouchbaseBucket;

import java.util.Collection;

/**
 * Created by ceaj8230 on 19/12/2014.
 */
public abstract class ViewQuery<TKEY,TVALUE,TDOC extends CouchbaseDocument> implements IViewQuery<TKEY,TVALUE,TDOC> {
    private CouchbaseViewDao<TKEY,TVALUE,TDOC> _dao;

    private TKEY _key;
    private Collection<TKEY> _keys;
    private TKEY _startKey;
    private TKEY _endKey;
    private boolean _isInclusive;
    private boolean _isDescending;
    private int _start=0;
    private int _limit=10;
    private boolean _syncWithDoc;

    public enum KeyType{
        LONG,
        DOUBLE,
        BOOLEAN,
        STRING,
        ARRAY
    }

    public abstract KeyType getKeyType();

    public ViewQuery(CouchbaseViewDao<TKEY,TVALUE,TDOC> dao){
        _dao = dao;
    }

    @Override
    public CouchbaseViewDao<TKEY,TVALUE,TDOC> getDao(){return _dao;}


    @Override
    public com.couchbase.client.java.view.ViewQuery toCouchbaseQuery(){
        String designDoc = ICouchbaseBucket.Utils.buildDesignDoc(_dao.getClient().getPrefix(), _dao.getDesignDoc());
        com.couchbase.client.java.view.ViewQuery result = com.couchbase.client.java.view.ViewQuery.from(designDoc,_dao.getViewName());
        if(_key!=null) {
            switch(getKeyType()){
                case BOOLEAN:result.key((Boolean)_key);break;
                case LONG:result.key((Long)_key);break;
                case DOUBLE:result.key((Double)_key);break;
                case STRING:result.key((String)_key);break;
                case ARRAY:result.key(JsonArray.from(((Collection)_key).toArray()));break;
            }
        }
        else if(_keys!=null){
            switch(getKeyType()){
                case BOOLEAN:
                case LONG:
                case DOUBLE:
                case STRING:
                    result.keys(JsonArray.from(_keys.toArray()));
                    break;
                case ARRAY: {
                    JsonArray keysList = JsonArray.create();
                    for (TKEY key : _keys) {
                        keysList.add(JsonArray.from(((Collection)key).toArray()));
                    }
                    result.keys(keysList);
                }
            }
        }
        else{
            switch(getKeyType()){
                case BOOLEAN:
                    result.startKey((Boolean) _startKey);
                    result.endKey((Boolean) _key);
                    break;
                case LONG:
                    result.startKey((Long)_startKey);
                    result.endKey((Long)_endKey);
                    break;
                case DOUBLE:
                    result.startKey((Double)_startKey);
                    result.endKey((Double)_endKey);
                    break;
                case STRING:
                    result.startKey((String)_startKey);
                    result.endKey((String)_endKey);
                    break;
                case ARRAY:
                    result.startKey(JsonArray.from(((Collection) _startKey).toArray()));
                    result.endKey(JsonArray.from(((Collection)_endKey).toArray()));
                    break;
            }
        }
        result.descending(_isDescending);
        result.inclusiveEnd(_isInclusive);
        result.skip(_start);
        result.limit(_limit);
        if(_syncWithDoc) result.stale(Stale.TRUE);
        return result;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC> withKey(TKEY key) {
        _key = key;
        _startKey = null;
        _endKey = null;
        _keys = null;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC> withKeys(Collection<TKEY> keys) {
        _key = null;
        _startKey = null;
        _endKey = null;
        _keys = keys;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withStartKey(TKEY key) {
        _startKey = key;
        _key = null;
        _keys = null;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withEndKey(TKEY key, boolean isInclusive) {
        _key = null;
        _keys = null;
        _endKey = key;
        _isInclusive = isInclusive;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withDescending(boolean desc) {
        _isDescending = desc;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withOffset(int nb) {
        _start=nb;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withLimit(int nb) {
        _limit=nb;
        return this;
    }

    public ViewQuery<TKEY,TVALUE,TDOC> syncWithDoc(){
        _syncWithDoc = true;
        return this;
    }
}
