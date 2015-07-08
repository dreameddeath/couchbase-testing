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

import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDaoFactory;
import com.dreameddeath.core.date.DateTimeServiceFactory;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.validation.ValidatorFactory;


/**
 * Created by Christophe Jeunesse on 02/09/2014.
 */
public class CouchbaseSessionFactory {
    private final CouchbaseDocumentDaoFactory _documentDaoFactory;
    private final CouchbaseCounterDaoFactory _counterDaoFactory;
    private final CouchbaseUniqueKeyDaoFactory _uniqueKeyDaoFactory;
    private final DateTimeServiceFactory _dateTimeServiceFactory;
    private final ValidatorFactory _validatorFactory;

    /*public CouchbaseSessionFactory(CouchbaseDocumentDaoFactory docDaoFactory, CouchbaseCounterDaoFactory counterDaoFactory, CouchbaseUniqueKeyDaoFactory uniqueDaoFactory, DateTimeServiceFactory dateTimeServiceFactory,ValidatorFactory validatorFactory){
        _documentDaoFactory =  docDaoFactory;
        _counterDaoFactory = counterDaoFactory;
        _uniqueKeyDaoFactory = uniqueDaoFactory;
        _dateTimeServiceFactory = dateTimeServiceFactory;
        _validatorFactory = validatorFactory;
    }
*/
    public CouchbaseSessionFactory(Builder builder){
        _documentDaoFactory =  builder._daoDocFactoryBuilder.build();
        _counterDaoFactory = _documentDaoFactory.getCounterDaoFactory();
        _uniqueKeyDaoFactory = _documentDaoFactory.getUniqueKeyDaoFactory();
        _dateTimeServiceFactory = builder.getDateTimeServiceFactory();
        _validatorFactory = builder.getValidatorFactory();

    }

    public CouchbaseDocumentDaoFactory getDocumentDaoFactory(){ return _documentDaoFactory;}
    public CouchbaseCounterDaoFactory getCounterDaoFactory(){ return _counterDaoFactory;}
    public CouchbaseUniqueKeyDaoFactory getUniqueKeyDaoFactory(){ return _uniqueKeyDaoFactory;}
    public DateTimeServiceFactory getDateTimeServiceFactory(){ return _dateTimeServiceFactory;}
    public ValidatorFactory getValidatorFactory(){ return _validatorFactory;}

    public CouchbaseSession newSession(CouchbaseSession.SessionType type, IUser user){
        return new CouchbaseSession(this,type,user);
    }

    public CouchbaseSession newReadOnlySession(IUser user){return newSession(CouchbaseSession.SessionType.READ_ONLY,user);}
    public CouchbaseSession newReadWriteSession(IUser user){return newSession(CouchbaseSession.SessionType.READ_WRITE,user);}
    public CouchbaseSession newCalcOnlySession(IUser user){return newSession(CouchbaseSession.SessionType.CALC_ONLY,user);}


    public static class Builder{
        private CouchbaseDocumentDaoFactory.Builder _daoDocFactoryBuilder;
        private DateTimeServiceFactory _dateTimeServiceFactory;
        private ValidatorFactory _validatorFactory;

        public Builder(){
            _daoDocFactoryBuilder = CouchbaseDocumentDaoFactory.builder();
            _dateTimeServiceFactory = new DateTimeServiceFactory();
            _validatorFactory = new ValidatorFactory();
        }

        public CouchbaseDocumentDaoFactory.Builder getDocumentDaoFactoryBuilder(){ return _daoDocFactoryBuilder;}
        public DateTimeServiceFactory getDateTimeServiceFactory(){ return _dateTimeServiceFactory;}
        public ValidatorFactory getValidatorFactory(){ return _validatorFactory;}

        public Builder setDocumentDaoFactoryBuilder(CouchbaseDocumentDaoFactory.Builder daoDocFactoryBuilder){ _daoDocFactoryBuilder = daoDocFactoryBuilder;return this;}
        public Builder setDateTimeServiceFactory(DateTimeServiceFactory serviceFactory){ _dateTimeServiceFactory=serviceFactory;return this;}
        public Builder setValidatorFactory(ValidatorFactory validatorFactory){ _validatorFactory=validatorFactory;return this;}

        public CouchbaseSessionFactory build(){
            return new CouchbaseSessionFactory(this);
        }
    }
}
