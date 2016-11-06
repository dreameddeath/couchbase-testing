/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.business.dao;

import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.document.IDaoWithKeyPattern;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.utils.KeyPattern;
import rx.Observable;
import rx.functions.Func1;

import java.util.Arrays;

/**
 * Created by Christophe Jeunesse on 28/12/2015.
 */
public abstract class BusinessCouchbaseDocumentWithKeyPatternDao<T extends BusinessDocument> extends BusinessCouchbaseDocumentDao<T> implements IDaoWithKeyPattern<T>{
    private KeyPattern keyPattern =null;

    @Override
    public BlockingDao toBlocking(){
        return new BlockingDao();
    }

    @Override
    public void init() {
        super.init();
        keyPattern = new KeyPattern(getKeyRawPattern());
    }

    abstract protected String getKeyRawPattern();

    abstract protected T updateTransientFromKeyPattern(T obj, String... keyParams);

    @Override
    abstract public String getKeyFromParams(Object... params);

    @Override
    public KeyPattern getKeyPattern() {
        return keyPattern;
    }

    @Override
    final public Observable<T> asyncGetFromKeyParams(ICouchbaseSession session, Object ...params){
        return asyncGet(session,getKeyFromParams(params));
    }

    @Override
    public T manageDaoLevelTransientFields(T obj) {
        obj=super.manageDaoLevelTransientFields(obj);
        return updateTransientFromKeyPattern(obj,keyPattern.extractParamsArrayFromKey(obj.getBaseMeta().getKey()));
    }

    public class BlockingDao extends BusinessCouchbaseDocumentDao<T>.BlockingDao implements IDaoWithKeyPattern.IBlockingDaoWithKeyPattern<T> {
        @Override
        public T blockingGetFromKeyParams(ICouchbaseSession session, Object ...params) throws DaoException,StorageException{
            return blockingGet(session,getKeyFromParams(params));
        }
    }

    protected class BuildKeyFromCounterFunc implements Func1<Long,T> {
        private final T obj;
        private final Object[] params;

        public BuildKeyFromCounterFunc(T obj,Object ...params){
            this.obj = obj;
            this.params = Arrays.copyOf(params,params.length+1);
        }

        @Override
        public T call(Long aLong) {
            params[params.length-1]=aLong;
            obj.getBaseMeta().setKey(BusinessCouchbaseDocumentWithKeyPatternDao.this.getKeyFromParams(params));
            BusinessCouchbaseDocumentWithKeyPatternDao.this.updateTransientFromKeyPattern(obj,keyPattern.extractParamsArrayFromKey(obj.getBaseMeta().getKey()));
            return obj;
        }
    }

}
