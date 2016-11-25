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

package com.dreameddeath.testing.couchbase;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 23/11/2016.
 */
public interface ICouchbaseOnWriteListener {
    default void onBeforeCounterWrite(CouchbaseBucketSimulator.ImpactMode mode,String key,Long before,Long after) throws StorageException {};
    default void onAfterCounterWrite(CouchbaseBucketSimulator.ImpactMode mode,String key,Long before,Long after) throws StorageException {};
    default <T extends CouchbaseDocument> void onBeforeWrite(CouchbaseBucketSimulator.ImpactMode mode,T inputDoc) throws StorageException {};
    default <T extends CouchbaseDocument> void onAfterWrite(CouchbaseBucketSimulator.ImpactMode mode,T newDoc)throws StorageException {};


}
