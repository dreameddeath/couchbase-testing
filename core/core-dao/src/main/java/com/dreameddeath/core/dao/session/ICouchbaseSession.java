/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.dao.session;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.model.view.IViewAsyncQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.user.IUser;
import io.reactivex.Single;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 20/11/2014.
 */
public interface ICouchbaseSession {
    IBlockingCouchbaseSession toBlocking();

    String getDomain();

    <T extends CouchbaseDocument> String getKeyFromUID(String uid, Class<T> targetClass) throws DaoException;
    <T extends CouchbaseDocument> String getKeyFromKeyParams(Class<T> targetClass,Object ...params) throws DaoException;

    Single<Long> asyncGetCounter(String key) throws DaoException;
    Single<Long> asyncIncrCounter(String key, long byVal)throws DaoException;
    Single<Long> asyncDecrCounter(String key, long byVal)throws DaoException;




    <T extends CouchbaseDocument> Single<T> asyncGet(String key);
    <T extends CouchbaseDocument> Single<T> asyncRefresh(T doc);
    <T extends CouchbaseDocument> Single<T> asyncGet(String key, Class<T> targetClass);
    <T extends CouchbaseDocument> Single<T> asyncGetFromUID(String uid, Class<T> targetClass);
    <T extends CouchbaseDocument> Single<T> asyncGetFromKeyParams(Class<T> targetClass,Object ...params);

    <T extends CouchbaseDocument> T newEntity(Class<T> clazz);
    <T extends CouchbaseDocument> T attachEntity(T entity);
    <T extends CouchbaseDocument> Single<T> asyncBuildKey(T obj);

    <T extends CouchbaseDocument> Single<T> asyncCreate(T obj);
    <T extends CouchbaseDocument> Single<T> asyncSave(T obj);

    <T extends CouchbaseDocument> Single<T> asyncUpdate(T obj);
    <T extends CouchbaseDocument> Single<T> asyncDelete(T obj);

    <T extends CouchbaseDocument> Single<T> asyncValidate(T doc);

    Single<CouchbaseUniqueKey> asyncGetUniqueKey(String internalKey);
    <T extends CouchbaseDocument> String buildUniqueKey(T doc,String value, String nameSpace) throws DaoException;
    <T extends CouchbaseDocument> Single<T> asyncAddOrUpdateUniqueKey(T doc, String value, String nameSpace);
    Single<Boolean> asyncRemoveUniqueKey(String internalKey);

    DateTime getCurrentDate();

    String getKeyPrefix();

    SessionType getSessionType();

    IUser getUser();
    <TKEY,TVALUE,T extends CouchbaseDocument> IViewQuery<TKEY,TVALUE,T> initViewQuery(Class<T> forClass, String viewName) throws DaoException;
    <TKEY,TVALUE,T extends CouchbaseDocument> IViewQueryResult<TKEY,TVALUE,T> executeQuery(IViewQuery<TKEY, TVALUE, T> query) throws DaoException,StorageException;
    <TKEY,TVALUE,T extends CouchbaseDocument> Single<IViewAsyncQueryResult<TKEY,TVALUE,T>> executeAsyncQuery(IViewQuery<TKEY, TVALUE, T> query);

    void reset(); //Clean cache

    ICouchbaseSession getTemporaryReadOnlySession();
    ICouchbaseSession toParentSession();

    enum SessionType{
        READ_ONLY,
        CALC_ONLY,
        READ_WRITE
    }
}
