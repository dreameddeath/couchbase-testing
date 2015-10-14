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

package com.dreameddeath.core;

import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDaoFactory;
import com.dreameddeath.core.date.DateTimeServiceFactory;
import com.dreameddeath.core.user.User;


/**
 * Created by Christophe Jeunesse on 02/09/2014.
 */
public class CouchbaseSessionFactory {
    private final BaseCouchbaseDocumentDaoFactory documentDaoFactory;
    private final CouchbaseCounterDaoFactory counterDaoFactory;
    private final CouchbaseUniqueKeyDaoFactory uniqueKeyDaoFactory;
    private final DateTimeServiceFactory dateTimeServiceFactory;

    public CouchbaseSessionFactory(BaseCouchbaseDocumentDaoFactory docDaoFactory,CouchbaseCounterDaoFactory counterDaoFactory,CouchbaseUniqueKeyDaoFactory uniqueDaoFactory,DateTimeServiceFactory dateTimeServiceFactory){
        documentDaoFactory =  docDaoFactory;
        this.counterDaoFactory = counterDaoFactory;
        uniqueKeyDaoFactory = uniqueDaoFactory;
        this.dateTimeServiceFactory = dateTimeServiceFactory;
    }

    public BaseCouchbaseDocumentDaoFactory getDocumentDaoFactory(){ return documentDaoFactory;}
    public CouchbaseCounterDaoFactory getCounterDaoFactory(){ return counterDaoFactory;}
    public CouchbaseUniqueKeyDaoFactory getUniqueKeyDaoFactory(){ return uniqueKeyDaoFactory;}
    public DateTimeServiceFactory getDateTimeServiceFactory(){ return dateTimeServiceFactory;}

    public CouchbaseSession newSession(CouchbaseSession.SessionType type, User user){
        return new CouchbaseSession(this,type,user);
    }

    public CouchbaseSession newReadOnlySession(User user){return newSession(CouchbaseSession.SessionType.READ_ONLY,user);}
    public CouchbaseSession newReadWriteSession(User user){return newSession(CouchbaseSession.SessionType.READ_WRITE,user);}
    public CouchbaseSession newCalcOnlySession(User user){return newSession(CouchbaseSession.SessionType.CALC_ONLY,user);}

}
