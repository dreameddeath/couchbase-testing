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

package com.dreameddeath.core.session.impl;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.couchbase.exception.StorageObservableException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.DaoObservableException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.exception.validation.ValidationObservableException;
import com.dreameddeath.core.dao.session.IBlockingCouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 20/06/2016.
 */
public class BlockingCouchbaseSession implements IBlockingCouchbaseSession {
    private ICouchbaseSession parentSession;

    public BlockingCouchbaseSession(ICouchbaseSession parentSession){
        this.parentSession = parentSession;
    }

    @Override
    public long blockingGetCounter(String key) throws DaoException,StorageException {
        return parentSession.asyncGetCounter(key).toBlocking().single();
    }

    @Override
    public long blockingIncrCounter(String key, long byVal) throws DaoException,StorageException {
        return parentSession.asyncIncrCounter(key,byVal).toBlocking().single();
    }

    @Override
    public long blockingDecrCounter(String key, long byVal) throws DaoException,StorageException {
        return parentSession.asyncDecrCounter(key,byVal).toBlocking().single();
    }

    @Override
    public <T extends CouchbaseDocument> T blockingCreate(T obj) throws ValidationException,DaoException,StorageException {
        return manageAsyncWriteResult(obj,parentSession.asyncCreate(obj));
    }

    @Override
    public <T extends CouchbaseDocument> T blockingBuildKey(T obj) throws DaoException,StorageException {
        try {
            return parentSession.asyncBuildKey(obj).toBlocking().single();
        }
        catch(DaoObservableException e){
            throw e.getCause();
        }
        catch (StorageObservableException e){
            throw e.getCause();
        }
    }

    @Override
    public CouchbaseDocument blockingGet(String key) throws DaoException,StorageException {
        return manageAsyncReadResult(parentSession.asyncGet(key));
    }

    @Override
    public <T extends CouchbaseDocument> T blockingGet(String key, Class<T> targetClass) throws DaoException,StorageException {
        return manageAsyncReadResult(parentSession.asyncGet(key, targetClass));
    }

    @Override
    public <T extends CouchbaseDocument> T blockingGetFromUID(String uid, Class<T> targetClass) throws DaoException,StorageException {
        return blockingGet(parentSession.getKeyFromUID(uid,targetClass), targetClass);
    }

    @Override
    public <T extends CouchbaseDocument> T blockingGetFromKeyParams(Class<T> targetClass, Object... params) throws DaoException, StorageException {
        return manageAsyncReadResult(parentSession.asyncGetFromKeyParams(targetClass,params));
    }


    @Override
    public <T extends CouchbaseDocument> T blockingRefresh(T doc) throws DaoException, StorageException {
        return manageAsyncReadResult(parentSession.asyncRefresh(doc));
    }

    @Override
    public <T extends CouchbaseDocument> T blockingUpdate(T obj)throws ValidationException,DaoException,StorageException {
        return manageAsyncWriteResult(obj,parentSession.asyncUpdate(obj));
    }


    @Override
    public <T extends CouchbaseDocument> T blockingDelete(T obj)throws ValidationException,DaoException,StorageException {
        return manageAsyncWriteResult(obj,parentSession.asyncDelete(obj));
    }

    @Override
    public <T extends CouchbaseDocument> T blockingSave(T obj) throws ValidationException,DaoException,StorageException {
        return manageAsyncWriteResult(obj,parentSession.asyncSave(obj));
    }

    @Override
    public void blockingAddOrUpdateUniqueKey(CouchbaseDocument doc, String value, String nameSpace)throws ValidationException,DaoException,StorageException,DuplicateUniqueKeyException {
        manageAsyncWriteResult(doc,parentSession.asyncAddOrUpdateUniqueKey(doc, value, nameSpace));
    }

    @Override
    public CouchbaseUniqueKey blockingGetUniqueKey(String internalKey)throws DaoException,StorageException {
        return manageAsyncReadResult(parentSession.asyncGetUniqueKey(internalKey));
    }

    @Override
    public void blockingRemoveUniqueKey(String internalKey) throws DaoException,StorageException {
        try {
            parentSession.asyncRemoveUniqueKey(internalKey).toBlocking().single();
        }
        catch(DaoObservableException e){
            throw e.getCause();
        }
        catch (StorageObservableException e){
            throw e.getCause();
        }
    }


    @Override
    public <T extends CouchbaseDocument>  T blockingValidate(T doc) throws ValidationException {
        return manageAsyncValidationResult(doc,parentSession.asyncValidate(doc));
    }



    public <T extends CouchbaseDocument> T manageAsyncValidationResult(final T obj, Observable<T> obs)throws ValidationException {
        try{
            return obs.toBlocking().single();
        }
        catch(ValidationObservableException e){
            throw e.getCause();
        }
    }

    public <T extends CouchbaseDocument> T manageAsyncReadResult(Observable<T> obs)throws DaoException,StorageException {
        try{
            return obs.toBlocking().single();
        }
        catch(DaoObservableException e){
            throw e.getCause();
        }
        catch (StorageObservableException e){
            throw e.getCause();
        }
    }

    public <T extends CouchbaseDocument> T manageAsyncWriteResult(final T obj, Observable<T> obs)throws ValidationException,DaoException,StorageException {
        try{
            return obs.toBlocking().single();
        }
        catch(DaoObservableException e){
            throw e.getCause();
        }
        catch(ValidationObservableException e){
            throw e.getCause();
        }
        catch (StorageObservableException e){
            throw e.getCause();
        }
    }

}
