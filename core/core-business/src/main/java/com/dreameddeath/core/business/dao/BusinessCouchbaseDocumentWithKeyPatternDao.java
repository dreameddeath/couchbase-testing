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

package com.dreameddeath.core.business.dao;

import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.document.IDaoWithKeyPattern;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.utils.KeyPattern;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 28/12/2015.
 */
public abstract class BusinessCouchbaseDocumentWithKeyPatternDao<T extends BusinessDocument> extends BusinessCouchbaseDocumentDao<T> implements IDaoWithKeyPattern<T>{
    private KeyPattern keyPattern =null;
    @Override
    public void init() {
        super.init();
        keyPattern = new KeyPattern(getKeyRawPattern());
    }

    @Override
    abstract protected String getKeyRawPattern();

    @Override
    abstract protected T updateTransientFromKeyPattern(T obj, String... keyParams);

    @Override
    abstract public String getKeyFromParams(Object... params);

    @Override
    final public T getFromKeyParams(ICouchbaseSession session, Object ...params) throws DaoException,StorageException{
        return get(session,getKeyFromParams(params));
    }

    @Override
    final public Observable<T> asyncGetFromKeyParams(ICouchbaseSession session, Object ...params){
        return asyncGet(session,getKeyFromParams(params));
    }

    @Override
    public Observable<T> asyncGet(ICouchbaseSession session, String key) {
        return super.asyncGet(session,key).map(obj->updateTransientFromKeyPattern(obj,keyPattern.extractParamsArrayFromKey(key)));
    }

    @Override
    public Observable<T> asyncCreate(ICouchbaseSession session, T obj, boolean isCalcOnly) {
        return super.asyncCreate(session,obj,isCalcOnly).map(val->updateTransientFromKeyPattern(val,keyPattern.extractParamsArrayFromKey(val.getBaseMeta().getKey())));
    }
}
