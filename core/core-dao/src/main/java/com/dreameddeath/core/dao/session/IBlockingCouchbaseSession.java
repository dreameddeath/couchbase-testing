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

package com.dreameddeath.core.dao.session;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;

/**
 * Created by Christophe Jeunesse on 20/06/2016.
 */
public interface IBlockingCouchbaseSession {
    <T extends CouchbaseDocument> T blockingGet(String key) throws DaoException,StorageException;
    <T extends CouchbaseDocument> T blockingGet(String key, Class<T> targetClass) throws DaoException,StorageException;
    <T extends CouchbaseDocument> T blockingRefresh(T doc) throws DaoException,StorageException;
    <T extends CouchbaseDocument> T blockingGetFromUID(String uid, Class<T> targetClass) throws DaoException,StorageException;
    <T extends CouchbaseDocument> T blockingGetFromKeyParams(Class<T> targetClass, Object ...params) throws DaoException,StorageException;

    <T extends CouchbaseDocument> T blockingBuildKey(T obj) throws DaoException,StorageException;
    <T extends CouchbaseDocument> T blockingCreate(T obj) throws ValidationException,DaoException,StorageException;
    <T extends CouchbaseDocument> T blockingSave(T obj) throws ValidationException,DaoException,StorageException;
    <T extends CouchbaseDocument> T blockingUpdate(T obj) throws ValidationException,DaoException,StorageException;
    <T extends CouchbaseDocument> T blockingDelete(T obj) throws ValidationException,DaoException,StorageException;
    <T extends CouchbaseDocument> T blockingValidate(T doc) throws ValidationException;

    CouchbaseUniqueKey blockingGetUniqueKey(String internalKey) throws DaoException,StorageException;
    void blockingAddOrUpdateUniqueKey(CouchbaseDocument doc, String value, String nameSpace) throws ValidationException,DaoException,StorageException,DuplicateUniqueKeyException;
    void blockingRemoveUniqueKey(String internalKey) throws DaoException,ValidationException,StorageException;


    long blockingGetCounter(String key) throws DaoException,StorageException;
    long blockingIncrCounter(String key, long byVal) throws DaoException,StorageException;
    long blockingDecrCounter(String key, long byVal) throws DaoException,StorageException;
}
