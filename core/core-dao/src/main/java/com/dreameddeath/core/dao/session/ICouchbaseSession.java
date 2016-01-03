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

package com.dreameddeath.core.dao.session;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.model.view.IViewAsyncQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.user.IUser;
import org.joda.time.DateTime;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 20/11/2014.
 */
public interface ICouchbaseSession {
    long getCounter(String key) throws DaoException,StorageException;
    long incrCounter(String key, long byVal) throws DaoException,StorageException;
    long decrCounter(String key, long byVal) throws DaoException,StorageException;

    Observable<Long> asyncGetCounter(String key) throws DaoException;
    Observable<Long> asyncIncrCounter(String key, long byVal)throws DaoException;
    Observable<Long> asyncDecrCounter(String key, long byVal)throws DaoException;


    CouchbaseDocument get(String key) throws DaoException,StorageException;
    Observable<CouchbaseDocument> asyncGet(String key)throws DaoException;
    <T extends CouchbaseDocument> T get(String key, Class<T> targetClass) throws DaoException,StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncGet(String key, Class<T> targetClass)throws DaoException;


    <T extends CouchbaseDocument> T getFromUID(String uid, Class<T> targetClass) throws DaoException,StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncGetFromUID(String uid, Class<T> targetClass) throws DaoException,StorageException;
    <T extends CouchbaseDocument> String getKeyFromUID(String uid, Class<T> targetClass) throws DaoException;
    <T extends CouchbaseDocument> T getFromKeyParams(Class<T> targetClass,Object ...params) throws DaoException,StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncGetFromKeyParams(Class<T> targetClass,Object ...params) throws DaoException,StorageException;
    <T extends CouchbaseDocument> String getKeyFromKeyParams(Class<T> targetClass,Object ...params) throws DaoException;



    <T extends CouchbaseDocument> T newEntity(Class<T> clazz);
    <T extends CouchbaseDocument> T attachEntity(T entity);

    <T extends CouchbaseDocument> T buildKey(T obj) throws DaoException,StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncBuildKey(T obj)throws DaoException;

    <T extends CouchbaseDocument> T create(T obj) throws ValidationException,DaoException,StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncCreate(T obj)throws DaoException;
    <T extends CouchbaseDocument> T save(T obj) throws ValidationException,DaoException,StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncSave(T obj)throws DaoException;

    <T extends CouchbaseDocument> T update(T obj) throws ValidationException,DaoException,StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncUpdate(T obj)throws DaoException;
    <T extends CouchbaseDocument> T delete(T obj) throws ValidationException,DaoException,StorageException;
    <T extends CouchbaseDocument> Observable<T> asyncDelete(T obj)throws DaoException;

    void validate(CouchbaseDocument doc) throws ValidationException;

    CouchbaseUniqueKey getUniqueKey(String internalKey) throws DaoException,StorageException;
    void addOrUpdateUniqueKey(CouchbaseDocument doc, Object value, String nameSpace) throws ValidationException,DaoException,StorageException,DuplicateUniqueKeyException;
    void removeUniqueKey(String internalKey) throws DaoException,ValidationException,StorageException;

    DateTime getCurrentDate();

    String getKeyPrefix();

    SessionType getSessionType();
    IUser getUser();
    <TKEY,TVALUE,T extends CouchbaseDocument> IViewQuery<TKEY,TVALUE,T> initViewQuery(Class<T> forClass, String viewName) throws DaoException;
    <TKEY,TVALUE,T extends CouchbaseDocument> IViewQueryResult<TKEY,TVALUE,T> executeQuery(IViewQuery<TKEY, TVALUE, T> query) throws DaoException,StorageException;
    <TKEY,TVALUE,T extends CouchbaseDocument> Observable<IViewAsyncQueryResult<TKEY,TVALUE,T>> executeAsyncQuery(IViewQuery<TKEY, TVALUE, T> query) throws DaoException,StorageException;

    void reset(); //Clean cache

    enum SessionType{
        READ_ONLY,
        CALC_ONLY,
        READ_WRITE
    }
}
