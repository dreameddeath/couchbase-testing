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

package com.dreameddeath.core.dao.exception.dao;

import com.dreameddeath.core.model.business.CouchbaseDocument;

/**
 * Created by ceaj8230 on 02/09/2014.
 */
public class DaoNotFoundException extends DaoException {
    public DaoNotFoundException(Class docClass){
        super("The dao for doc Class "+docClass.getName()+" hasn't been found");
    }

    public DaoNotFoundException(CouchbaseDocument doc){
        this(doc.getClass());
    }

    public DaoNotFoundException(String key, Type type){
        super("The dao for "+type.toString()+ " with key <"+key+"> hasn't been found");
    }

    public enum Type{
        DOC,
        COUNTER,
        KEY
    }
}
