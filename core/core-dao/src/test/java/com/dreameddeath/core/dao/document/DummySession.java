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
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.model.view.IViewAsyncQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.user.IUser;
import org.joda.time.DateTime;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 28/12/2015.
 */
public class DummySession implements ICouchbaseSession {
    @Override
    public long getCounter(String key)  {
        return 0;
    }

    @Override
    public long incrCounter(String key, long byVal)  {
        return 0;
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGetFromUID(String uid, Class<T> targetClass)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> T getFromKeyParams(Class<T> targetClass, Object... params)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGetFromKeyParams(Class<T> targetClass, Object... params) {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> String getKeyFromKeyParams(Class<T> targetClass, Object... params)  {
        return null;
    }

    @Override
    public long decrCounter(String key, long byVal)  {
        return 0;
    }

    @Override
    public Observable<Long> asyncGetCounter(String key) {
        return null;
    }

    @Override
    public Observable<Long> asyncIncrCounter(String key, long byVal) {
        return null;
    }

    @Override
    public Observable<Long> asyncDecrCounter(String key, long byVal) {
        return null;
    }

    @Override
    public CouchbaseDocument get(String key)  {
        return null;
    }

    @Override
    public Observable<CouchbaseDocument> asyncGet(String key)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> T get(String key, Class<T> targetClass)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGet(String key, Class<T> targetClass)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> T getFromUID(String uid, Class<T> targetClass)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> T refresh(T doc)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncRefresh(T doc)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> String getKeyFromUID(String uid, Class<T> targetClass)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> T newEntity(Class<T> clazz) {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> T attachEntity(T entity) {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> T buildKey(T obj)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncBuildKey(T obj)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> T create(T obj) throws ValidationException, DaoException, StorageException {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncCreate(T obj)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> T save(T obj)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncSave(T obj)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> T update(T obj) throws ValidationException, DaoException, StorageException {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncUpdate(T obj)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> T delete(T obj)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncDelete(T obj)  {
        return null;
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncValidate(T doc) {
        return Observable.just(doc);
    }

    @Override
    public CouchbaseDocument validate(CouchbaseDocument doc) {
        return doc;
    }

    @Override
    public CouchbaseUniqueKey getUniqueKey(String internalKey) {
        return null;
    }

    @Override
    public Observable<CouchbaseUniqueKey> asyncGetUniqueKey(String internalKey) {
        return null;
    }

    @Override
    public void addOrUpdateUniqueKey(CouchbaseDocument doc, String value, String nameSpace) {

    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAddOrUpdateUniqueKey(T doc, String value, String nameSpace) {
        return null;
    }

    @Override
    public void removeUniqueKey(String internalKey)  {

    }

    @Override
    public Observable<Boolean> asyncRemoveUniqueKey(String internalKey) {
        return null;
    }

    @Override
    public DateTime getCurrentDate() {
        return null;
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
    public <TKEY, TVALUE, T extends CouchbaseDocument> Observable<IViewAsyncQueryResult<TKEY, TVALUE, T>> executeAsyncQuery(IViewQuery<TKEY, TVALUE, T> query)  {
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
        return null;
    }

    @Override
    public void setTemporaryReadOnlyMode(boolean active) {

    }

    @Override
    public boolean isTemporaryReadOnlyMode() {
        return false;
    }
}
