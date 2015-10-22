/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.dao.model.view.impl;

import com.couchbase.client.java.view.Stale;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.model.document.CouchbaseDocument;

import java.util.Collection;

/**
 * Created by Christophe Jeunesse on 19/12/2014.
 */
public class ViewQuery<TKEY,TVALUE,TDOC extends CouchbaseDocument> implements IViewQuery<TKEY,TVALUE,TDOC> {
    private final CouchbaseViewDao<TKEY,TVALUE,TDOC> dao;
    private final String keyPrefix;
    private TKEY key;
    private Collection<TKEY> keys;
    private TKEY startKey;
    private TKEY endKey;
    private boolean isInclusive;
    private boolean isDescending;
    private int start=0;
    private int limit=10;
    private boolean syncWithDoc;

    public ViewQuery(CouchbaseViewDao<TKEY,TVALUE,TDOC> dao,String keyPrefix){
        this.dao = dao;
        this.keyPrefix = keyPrefix;
    }

    public ViewQuery(ViewQuery<TKEY,TVALUE,TDOC> src, int offset){
        dao = src.dao;
        keyPrefix = src.keyPrefix;
        startKey= src.startKey;
        endKey = src.endKey;
        key = src.key;
        keys = src.keys;
        isInclusive = src.isInclusive;
        isDescending = src.isDescending;
        start = src.start+offset;
        limit = offset;
        syncWithDoc = src.syncWithDoc;
    }

    @Override
    public CouchbaseViewDao<TKEY,TVALUE,TDOC> getDao(){return dao;}

    @Override
    public IViewQuery<TKEY, TVALUE, TDOC> next(int nb) {
        return new ViewQuery(this,nb);
    }


    @Override
    public com.couchbase.client.java.view.ViewQuery toCouchbaseQuery(){
        String designDoc = ICouchbaseBucket.Utils.buildDesignDoc(keyPrefix, dao.getDesignDoc());
        com.couchbase.client.java.view.ViewQuery result = com.couchbase.client.java.view.ViewQuery.from(designDoc,dao.getViewName());
        if(key!=null) {
            dao.getKeyTranscoder().key(result,key);
        }
        else if(keys!=null){
            dao.getKeyTranscoder().keys(result,keys);
        }
        else{
            dao.getKeyTranscoder().startKey(result, startKey);
            dao.getKeyTranscoder().endKey(result,endKey);
        }

        result.descending(isDescending).
                inclusiveEnd(isInclusive).
                skip(start).
                limit(limit);

        if(syncWithDoc){
            result.stale(Stale.FALSE);
        }
        return result;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC> withKey(TKEY key) {
        this.key = key;
        this.startKey = null;
        this.endKey = null;
        this.keys = null;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC> withKeys(Collection<TKEY> keys) {
        this.key = null;
        this.startKey = null;
        this.endKey = null;
        this.keys = keys;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withStartKey(TKEY key) {
        this.startKey = key;
        this.key = null;
        this.keys = null;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withEndKey(TKEY key, boolean isInclusive) {
        this.key = null;
        this.keys = null;
        this.endKey = key;
        this.isInclusive = isInclusive;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withDescending(boolean desc) {
        this.isDescending = desc;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withOffset(int nb) {
        this.start=nb;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC>  withLimit(int nb) {
        this.limit=nb;
        return this;
    }

    @Override
    public ViewQuery<TKEY,TVALUE,TDOC> syncWithDoc(){
        this.syncWithDoc = true;
        return this;
    }
}
