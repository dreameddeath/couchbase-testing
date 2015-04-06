/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.exception.dao;

import com.dreameddeath.core.model.document.BaseCouchbaseDocument;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;

/**
 * Created by ceaj8230 on 02/09/2014.
 */
public class ReadOnlyException extends DaoException {
    public ReadOnlyException(BaseCouchbaseDocument doc){
        super("Trying to update the document  <"+doc.getClass().getName()+">"+((doc.getBaseMeta().getKey()!=null)?" withKey <"+doc.getBaseMeta().getKey()+">":"")+" while being in a read only session");
    }

    public ReadOnlyException(Class docClass){
        super("Trying to update the document  <"+docClass.getName()+"> while being in a read only session");
    }

    public ReadOnlyException(CouchbaseUniqueKey uniqueKey){
        super("Trying to update the unique <"+uniqueKey.getBaseMeta().getKey()+"> while being in a read only session");
    }

    public ReadOnlyException(String counterKey){
        super("Trying to update the counter key <"+counterKey+"> while being in a read only session");
    }
}
