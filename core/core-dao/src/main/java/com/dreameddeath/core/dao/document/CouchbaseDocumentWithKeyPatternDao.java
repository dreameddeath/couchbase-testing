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

package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.utils.KeyPattern;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import rx.Observable;
import rx.functions.Func1;

import java.util.Arrays;


/**
 * Created by Christophe Jeunesse on 12/10/2014.
 */
public abstract class CouchbaseDocumentWithKeyPatternDao<T extends CouchbaseDocument> extends CouchbaseDocumentDao<T> implements IDaoWithKeyPattern<T> {
    private final KeyPattern keyPattern;

    protected abstract String getKeyRawPattern();

    public CouchbaseDocumentWithKeyPatternDao() {
        this.keyPattern = new KeyPattern(getKeyRawPattern());
    }

    protected T updateTransientFromKeyPattern(T obj, String ... keyParams){
        return obj;
    }

    @Override
    public  abstract String getKeyFromParams(Object ...params);

    @Override
    public T getFromKeyParams(ICouchbaseSession session,Object ...params) throws DaoException,StorageException{
        return get(session,getKeyFromParams(params));
    }

    @Override
    public Observable<T> asyncGetFromKeyParams(ICouchbaseSession session, Object ...params){
        return asyncGet(session,getKeyFromParams(params));
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public KeyPattern getKeyPattern(){
        return keyPattern;
    }

    @Override
    public Observable<T> asyncGet(ICouchbaseSession session, String key){
        return super.asyncGet(session,key).map(obj->updateTransientFromKeyPattern(obj,keyPattern.extractParamsArrayFromKey(key)));
    }

    @Override
    public Observable<T> asyncCreate(ICouchbaseSession session, T obj, boolean isCalcOnly) {
        return super.asyncCreate(session,obj,isCalcOnly);
    }

    protected class BuildKeyFromCounterFunc implements Func1<Long,T>{
        private final T obj;
        private final Object[] params;

        public BuildKeyFromCounterFunc(T obj,Object ...params){
            this.obj = obj;
            this.params = Arrays.copyOf(params,params.length+1);
        }

        @Override
        public T call(Long aLong) {
            params[params.length-1]=aLong;
            obj.getBaseMeta().setKey(CouchbaseDocumentWithKeyPatternDao.this.getKeyFromParams(params));
            CouchbaseDocumentWithKeyPatternDao.this.updateTransientFromKeyPattern(obj,keyPattern.extractParamsArrayFromKey(obj.getBaseMeta().getKey()));
            return obj;
        }
    }
}
