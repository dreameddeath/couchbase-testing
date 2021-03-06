/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.session.impl;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.session.IBlockingCouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import io.reactivex.Single;

import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 20/06/2016.
 */
public class BlockingCouchbaseSession implements IBlockingCouchbaseSession {
    private final ICouchbaseSession parentSession;
    private final int timeout;
    private final TimeUnit timeoutUnit;

    public BlockingCouchbaseSession(ICouchbaseSession parentSession,int timeout,TimeUnit unit){
        this.parentSession = parentSession;
        this.timeout = timeout;
        this.timeoutUnit = unit;
    }

    @Override
    public long blockingGetCounter(String key) throws DaoException,StorageException {
        return manageAsyncCounterResult(parentSession.asyncGetCounter(key));
    }


    @Override
    public long blockingIncrCounter(String key, long byVal) throws DaoException,StorageException {
        return manageAsyncCounterResult(parentSession.asyncIncrCounter(key,byVal));
    }


    @Override
    public long blockingDecrCounter(String key, long byVal) throws DaoException,StorageException {
        return manageAsyncCounterResult(parentSession.asyncDecrCounter(key,byVal));
    }

    @Override
    public <T extends CouchbaseDocument> T blockingCreate(T obj) throws ValidationException,DaoException,StorageException {
        return manageAsyncWriteResult(obj,parentSession.asyncCreate(obj));
    }

    @Override
    public <T extends CouchbaseDocument> T blockingBuildKey(T obj) throws DaoException,StorageException {
        try {
            return parentSession.asyncBuildKey(obj).blockingGet();
        }
        catch(RuntimeException e){
            Throwable eCause=e.getCause();
            if(eCause!=null){
                if(eCause instanceof DaoException){
                    throw (DaoException)eCause;
                }
                if(eCause instanceof StorageException){
                    throw (StorageException)eCause;
                }
            }
            throw e;
        }
    }

    @Override
    public <T extends CouchbaseDocument> T blockingGet(String key) throws DaoException,StorageException {
        return manageAsyncReadResult(parentSession.<T>asyncGet(key));
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
            parentSession.asyncRemoveUniqueKey(internalKey).blockingGet();
        }
        catch(RuntimeException e){
            Throwable eCause=e.getCause();
            if(eCause!=null){
                if(eCause instanceof DaoException){
                    throw (DaoException)eCause;
                }
                if(eCause instanceof StorageException){
                    throw (StorageException)eCause;
                }
            }
            throw e;
        }
    }


    @Override
    public <T extends CouchbaseDocument>  T blockingValidate(T doc) throws ValidationException {
        return manageAsyncValidationResult(doc,parentSession.asyncValidate(doc));
    }

    public <T extends CouchbaseDocument> T manageAsyncValidationResult(final T obj, Single<T> obs)throws ValidationException {
        try{
            return obs.blockingGet();
        }
        catch(RuntimeException e){
            Throwable eCause=e.getCause();
            if(eCause!=null){
                if(eCause instanceof ValidationException){
                    throw (ValidationException) eCause;
                }
            }
            throw e;
        }

    }

    public <T extends CouchbaseDocument> T manageAsyncReadResult(Single<T> obs)throws DaoException,StorageException {
        try{
            return obs.blockingGet();
        }
        catch(RuntimeException e){
            Throwable eCause=e.getCause();
            if(eCause!=null){
                if(eCause instanceof DaoException){
                    throw (DaoException)eCause;
                }
                if(eCause instanceof StorageException){
                    throw (StorageException)eCause;
                }
            }
            throw e;
        }
    }

    public long manageAsyncCounterResult(Single<Long> obs)throws DaoException,StorageException {
        try{
            return obs.blockingGet();
        }
        catch(RuntimeException e){
            Throwable eCause=e.getCause();
            if(eCause!=null){
                if(eCause instanceof DaoException){
                    throw (DaoException)eCause;
                }
                if(eCause instanceof StorageException){
                    throw (StorageException)eCause;
                }
            }
            throw e;
        }
    }


    public <T extends CouchbaseDocument> T manageAsyncWriteResult(final T obj, Single<T> obs)throws ValidationException,DaoException,StorageException {
        try{
            return obs.blockingGet();
        }
        catch(RuntimeException e){
            Throwable eCause=e.getCause();
            if(eCause!=null){
                if(eCause instanceof DaoException){
                    throw (DaoException)eCause;
                }
                else if(eCause instanceof StorageException){
                    throw (StorageException)eCause;
                }
                else if(eCause instanceof ValidationException){
                    throw (ValidationException) eCause;
                }
            }
            throw e;
        }
    }

}
