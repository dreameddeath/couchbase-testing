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

package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.model.view.IViewAsyncQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.dao.session.IBlockingCouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.model.exception.IllegalMethodCall;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.core.user.IUser;
import io.reactivex.Single;
import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 28/12/2015.
 */
public class DummySession implements ICouchbaseSession {

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncGetFromUID(String uid, Class<T> targetClass)  {
        return Single.error(new IllegalMethodCall());
    }


    @Override
    public <T extends CouchbaseDocument> Single<T> asyncGetFromKeyParams(Class<T> targetClass, Object... params) {
        return Single.error(new IllegalMethodCall());
    }

    @Override
    public <T extends CouchbaseDocument> String getKeyFromKeyParams(Class<T> targetClass, Object... params)  {
        return "";
    }

    @Override
    public Single<Long> asyncGetCounter(String key) {
        return Single.error(new IllegalMethodCall());
    }

    @Override
    public Single<Long> asyncIncrCounter(String key, long byVal) {
        return Single.error(new IllegalMethodCall());
    }

    @Override
    public Single<Long> asyncDecrCounter(String key, long byVal) {
        return Single.error(new IllegalMethodCall());
    }


    @Override
    public <T extends CouchbaseDocument> Single<T> asyncGet(String key)  {
        return Single.error(new IllegalMethodCall());
    }

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncGet(String key, Class<T> targetClass)  {
        return Single.error(new IllegalMethodCall());
    }


    @Override
    public <T extends CouchbaseDocument> Single<T> asyncRefresh(T doc)  {
        return Single.just(doc);
    }

    @Override
    public <T extends CouchbaseDocument> String getKeyFromUID(String uid, Class<T> targetClass)  {
        return "";
    }

    @Override
    public <T extends CouchbaseDocument> T newEntity(Class<T> clazz) {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> T attachEntity(T entity) {
        return entity;
    }

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncBuildKey(T obj)  {
        return Single.just(obj);
    }

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncCreate(T obj)  {
        return Single.just(obj);
    }

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncSave(T obj)  {
        return Single.just(obj);
    }

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncUpdate(T obj)  {
        return Single.just(obj);
    }


    @Override
    public <T extends CouchbaseDocument> Single<T> asyncDelete(T obj)  {
        return Single.just(obj);
    }

    @Override
    public <T extends CouchbaseDocument> Single<T> asyncValidate(T doc) {
        return Single.just(doc);
    }



    @Override
    public Single<CouchbaseUniqueKey> asyncGetUniqueKey(String internalKey) {
        return Single.error(new IllegalMethodCall());
    }


    @Override
    public <T extends CouchbaseDocument> Single<T> asyncAddOrUpdateUniqueKey(T doc, String value, String nameSpace) {
        return Single.just(doc);
    }


    @Override
    public Single<Boolean> asyncRemoveUniqueKey(String internalKey) {
        return Single.error(new IllegalMethodCall());
    }

    @Override
    public DateTime getCurrentDate() {
        return DateTime.now();
    }

    @Override
    public String getKeyPrefix() {
        return null;
    }

    @Override
    public <TKEY,TVALUE,T extends CouchbaseDocument> IViewQuery<TKEY,TVALUE,T> initViewQuery(Class<T> forClass, String viewName)  {
        return null;
    }

    @Override
    public <TKEY, TVALUE, T extends CouchbaseDocument> IViewQueryResult<TKEY, TVALUE, T> executeQuery(IViewQuery<TKEY, TVALUE, T> query)  {
        return null;
    }

    @Override
    public <TKEY, TVALUE, T extends CouchbaseDocument> Single<IViewAsyncQueryResult<TKEY, TVALUE, T>> executeAsyncQuery(IViewQuery<TKEY, TVALUE, T> query)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> String buildUniqueKey(T doc,String value, String nameSpace) throws DaoException{
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public SessionType getSessionType() {
        return null;
    }

    @Override
    public IUser getUser() {
        return AnonymousUser.INSTANCE;
    }

    @Override
    public ICouchbaseSession getTemporaryReadOnlySession() {
        return this;
    }

    @Override
    public ICouchbaseSession toParentSession() {
        return this;
    }

    @Override
    public IBlockingCouchbaseSession toBlocking(int timeout, TimeUnit unit){
        return new IBlockingCouchbaseSession() {
            @Override
            public <T extends CouchbaseDocument> T blockingGet(String key) throws DaoException, StorageException {
                return null;
            }

            @Override
            public <T extends CouchbaseDocument> T blockingGet(String key, Class<T> targetClass) throws DaoException, StorageException {
                return null;
            }

            @Override
            public <T extends CouchbaseDocument> T blockingRefresh(T doc) throws DaoException, StorageException {
                return doc;
            }

            @Override
            public <T extends CouchbaseDocument> T blockingGetFromUID(String uid, Class<T> targetClass) throws DaoException, StorageException {
                return null;
            }

            @Override
            public <T extends CouchbaseDocument> T blockingGetFromKeyParams(Class<T> targetClass, Object... params) throws DaoException, StorageException {
                return null;
            }

            @Override
            public <T extends CouchbaseDocument> T blockingBuildKey(T obj) throws DaoException, StorageException {
                return obj;
            }

            @Override
            public <T extends CouchbaseDocument> T blockingCreate(T obj) throws ValidationException, DaoException, StorageException {
                return obj;
            }

            @Override
            public <T extends CouchbaseDocument> T blockingSave(T obj) throws ValidationException, DaoException, StorageException {
                return obj;
            }

            @Override
            public <T extends CouchbaseDocument> T blockingUpdate(T obj) throws ValidationException, DaoException, StorageException {
                return obj;
            }

            @Override
            public <T extends CouchbaseDocument> T blockingDelete(T obj) throws ValidationException, DaoException, StorageException {
                return obj;
            }

            @Override
            public <T extends CouchbaseDocument> T blockingValidate(T doc) throws ValidationException {
                return doc;
            }

            @Override
            public CouchbaseUniqueKey blockingGetUniqueKey(String internalKey) throws DaoException, StorageException {
                return null;
            }

            @Override
            public void blockingAddOrUpdateUniqueKey(CouchbaseDocument doc, String value, String nameSpace) throws ValidationException, DaoException, StorageException, DuplicateUniqueKeyException {

            }

            @Override
            public void blockingRemoveUniqueKey(String internalKey) throws DaoException, ValidationException, StorageException {

            }

            @Override
            public long blockingGetCounter(String key) throws DaoException, StorageException {
                return 0;
            }

            @Override
            public long blockingIncrCounter(String key, long byVal) throws DaoException, StorageException {
                return 0;
            }

            @Override
            public long blockingDecrCounter(String key, long byVal) throws DaoException, StorageException {
                return 0;
            }


        };
    }

    @Override
    public String getDomain() {
        return null;
    }
}
