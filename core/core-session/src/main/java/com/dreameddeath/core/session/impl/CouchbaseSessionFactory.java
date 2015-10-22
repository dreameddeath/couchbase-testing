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

import com.dreameddeath.core.dao.factory.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.factory.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.factory.CouchbaseUniqueKeyDaoFactory;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.date.DateTimeServiceFactory;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.validation.ValidatorFactory;


/**
 * Created by Christophe Jeunesse on 02/09/2014.
 */
public class CouchbaseSessionFactory {
    private final CouchbaseDocumentDaoFactory documentDaoFactory;
    private final CouchbaseCounterDaoFactory counterDaoFactory;
    private final CouchbaseUniqueKeyDaoFactory uniqueKeyDaoFactory;
    private final DateTimeServiceFactory dateTimeServiceFactory;
    private final ValidatorFactory validatorFactory;

    public CouchbaseSessionFactory(Builder builder){
        documentDaoFactory =  builder.daoDocFactoryBuilder.build();
        counterDaoFactory = documentDaoFactory.getCounterDaoFactory();
        uniqueKeyDaoFactory = documentDaoFactory.getUniqueKeyDaoFactory();
        dateTimeServiceFactory = builder.getDateTimeServiceFactory();
        validatorFactory = builder.getValidatorFactory();

    }

    public CouchbaseDocumentDaoFactory getDocumentDaoFactory(){ return documentDaoFactory;}
    public CouchbaseCounterDaoFactory getCounterDaoFactory(){ return counterDaoFactory;}
    public CouchbaseUniqueKeyDaoFactory getUniqueKeyDaoFactory(){ return uniqueKeyDaoFactory;}
    public DateTimeServiceFactory getDateTimeServiceFactory(){ return dateTimeServiceFactory;}
    public ValidatorFactory getValidatorFactory(){ return validatorFactory;}

    public ICouchbaseSession newSession(CouchbaseSession.SessionType type, IUser user){
        return new CouchbaseSession(this,type,user);
    }
    public ICouchbaseSession newSession(CouchbaseSession.SessionType type, IUser user,String keyPrefix){
        return new CouchbaseSession(this,type,user,keyPrefix);
    }



    public ICouchbaseSession newReadOnlySession(IUser user){return newSession(CouchbaseSession.SessionType.READ_ONLY,user);}
    public ICouchbaseSession newReadWriteSession(IUser user){return newSession(CouchbaseSession.SessionType.READ_WRITE,user);}
    public ICouchbaseSession newCalcOnlySession(IUser user){return newSession(CouchbaseSession.SessionType.CALC_ONLY,user);}
    public ICouchbaseSession newReadOnlySession(IUser user,String keyPrefix){return newSession(CouchbaseSession.SessionType.READ_ONLY,user,keyPrefix);}
    public ICouchbaseSession newReadWriteSession(IUser user,String keyPrefix){return newSession(CouchbaseSession.SessionType.READ_WRITE,user,keyPrefix);}
    public ICouchbaseSession newCalcOnlySession(IUser user,String keyPrefix){return newSession(CouchbaseSession.SessionType.CALC_ONLY,user,keyPrefix);}



    public static class Builder{
        private CouchbaseDocumentDaoFactory.Builder daoDocFactoryBuilder;
        private DateTimeServiceFactory dateTimeServiceFactory;
        private ValidatorFactory validatorFactory;

        public Builder(){
            daoDocFactoryBuilder = CouchbaseDocumentDaoFactory.builder();
            dateTimeServiceFactory = new DateTimeServiceFactory();
            validatorFactory = new ValidatorFactory();
        }

        public CouchbaseDocumentDaoFactory.Builder getDocumentDaoFactoryBuilder(){ return daoDocFactoryBuilder;}
        public DateTimeServiceFactory getDateTimeServiceFactory(){ return dateTimeServiceFactory;}
        public ValidatorFactory getValidatorFactory(){ return validatorFactory;}

        public Builder setDocumentDaoFactoryBuilder(CouchbaseDocumentDaoFactory.Builder daoDocFactoryBuilder){ this.daoDocFactoryBuilder = daoDocFactoryBuilder;return this;}
        public Builder setDateTimeServiceFactory(DateTimeServiceFactory serviceFactory){ dateTimeServiceFactory=serviceFactory;return this;}
        public Builder setValidatorFactory(ValidatorFactory validatorFactory){ this.validatorFactory=validatorFactory;return this;}

        public CouchbaseSessionFactory build(){
            return new CouchbaseSessionFactory(this);
        }
    }
}
