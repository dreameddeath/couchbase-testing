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

package com.dreameddeath.core.dao.counter;


import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.couchbase.impl.WriteParams;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.InconsistentStateException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;

/**
 * Created by Christophe Jeunesse on 02/09/2014.
 */
public class CouchbaseCounterDao{
    private ICouchbaseBucket client;
    private CouchbaseDocumentDao baseDao;
    private String keyPattern;
    private Long defaultValue;
    private Long modulus;
    private Integer expiration;

    private CallingMode mode;

    public void setBaseDao(CouchbaseDocumentDao dao){baseDao=dao;}
    public void setClient(ICouchbaseBucket client){this.client = client;}
    public ICouchbaseBucket getClient(){
        if(client!=null) return client;
        else return baseDao.getClient();
    }

    public CouchbaseCounterDao(String key, Long defaultValue, Long modulus, Integer expiration){
        this.keyPattern = key;
        this.defaultValue = defaultValue;
        this.modulus = modulus;
        this.expiration = expiration;
        if((expiration==null) &&(defaultValue==null)){
            mode = CallingMode.BASE;
        }
        else if(expiration==null){
            mode = CallingMode.WITH_DEFAULT;
        }
        else{
            mode = CallingMode.WITH_DEFAULT_AND_EXPIRATION;
        }
    }

    public CouchbaseCounterDao(Builder builder){
        this(builder.getKeyPattern(), builder.getDefaultValue(), builder.getModulus(), builder.getExpiration().intValue());
        baseDao = builder.getBaseDao();
    }

    public String getKeyPattern(){
        return keyPattern;
    }

    public Long getCounter(ICouchbaseSession session,String key,boolean isCalcOnly) throws DaoException,StorageException {
        return incrCounter(session,key,0,isCalcOnly);
    }

    public long incrCounter(ICouchbaseSession session,String key, long by,boolean isCalcOny) throws DaoException,StorageException {
        long result;
        if(baseDao.isReadOnly() && by!=0){
            throw new InconsistentStateException(null,"Cannot update counter <"+key+"> in readonly mode");
        }

        if(isCalcOny){
                if(session.getKeyPrefix()!=null){
                    result = getClient().counter(key, 0L, WriteParams.create().with(session.getKeyPrefix()));
                }
                else {
                    result = getClient().counter(key, 0L);
                }
                if(result<0){
                    result=defaultValue;
                }
                result+=by;
        }
        else{
            switch (mode) {
                case WITH_DEFAULT:
                    if(session.getKeyPrefix()!=null) {
                        result = getClient().counter(key, by, defaultValue);
                    }
                    else{
                        result = getClient().counter(key, by, defaultValue, WriteParams.create().with(session.getKeyPrefix()));
                    }
                    break;
                case WITH_DEFAULT_AND_EXPIRATION:
                    if(session.getKeyPrefix()!=null) {
                        result = getClient().counter(key, by, defaultValue, expiration);
                    }
                    else{
                        result = getClient().counter(key, by, defaultValue, expiration, WriteParams.create().with(session.getKeyPrefix()));
                    }
                    break;
                default:
                    if(session.getKeyPrefix()!=null) {
                        result = getClient().counter(key, by);
                    }
                    else{
                        result = getClient().counter(key, by, WriteParams.create().with(session.getKeyPrefix()));
                    }
            }
        }
        if (modulus != null) {
            return result % modulus;
        } else {
            return result;
        }
    }

    public long decrCounter(ICouchbaseSession session,String key, long by,boolean isCalcOny) throws DaoException,StorageException {
        if(baseDao.isReadOnly() && by!=0){
            throw new InconsistentStateException(null,"Cannot update counter <"+key+"> in readonly mode");
        }
        if(isCalcOny){
            long result;
            if(session.getKeyPrefix()!=null) {
                result = getClient().counter(key, 0L);
            }
            else{
                result = getClient().counter(key, 0L, WriteParams.create().with(session.getKeyPrefix()));
            }
            if(result<0){
                result=defaultValue;
            }
            result-=by;

            return result;
        }
        else {
            switch (mode) {
                case WITH_DEFAULT:
                    if(session.getKeyPrefix()!=null) {
                        return getClient().counter(key, -by, defaultValue);
                    }
                    else{
                        return getClient().counter(key, -by, defaultValue,WriteParams.create().with(session.getKeyPrefix()));
                    }
                case WITH_DEFAULT_AND_EXPIRATION:
                    if(session.getKeyPrefix()!=null) {
                        return getClient().counter(key, -by, defaultValue, expiration);
                    }
                    else{
                        return getClient().counter(key, -by, defaultValue, expiration, WriteParams.create().with(session.getKeyPrefix()));
                    }
                default:
                    if(session.getKeyPrefix()!=null) {
                        return getClient().counter(key, -by);
                    }
                    else{
                        return getClient().counter(key, -by, WriteParams.create().with(session.getKeyPrefix()));
                    }
            }
        }
    }

    public enum CallingMode {
        BASE,
        WITH_DEFAULT,
        WITH_DEFAULT_AND_EXPIRATION
    }

    public static class Builder{
        private String keyPattern;
        private Long defaultValue;
        private Long expiration=0L;
        private Long modulus;
        private ICouchbaseBucket client;
        private CouchbaseDocumentDao baseDao;

        public Builder withKeyPattern(String key){
            keyPattern = key;
            return this;
        }

        public Builder withDefaultValue(Long defaultVal){
            defaultValue = defaultVal;
            return this;
        }

        public Builder withDefaultValue(long defaultVal){
            defaultValue = defaultVal;
            return this;
        }

        public Builder withExpiration(Long expiration){
            this.expiration = expiration;
            return this;
        }

        public Builder withExpiration(long expiration){
            this.expiration = expiration;
            return this;
        }

        public Builder withModulus(Long modulus){
            this.modulus = modulus;
            return this;
        }

        public Builder withModulus(long modulus){
            this.modulus = modulus;
            return this;
        }

        public Builder withClient(ICouchbaseBucket client){
            this.client = client;
            return this;
        }

        public Builder withBaseDao(CouchbaseDocumentDao dao){
            baseDao = dao;
            return this;
        }

        public String getKeyPattern(){return keyPattern;}
        public Long getDefaultValue(){return defaultValue;}
        public Long getExpiration(){return expiration;}
        public Long getModulus(){return modulus;}
        public ICouchbaseBucket getClient(){return client;}
        public CouchbaseDocumentDao getBaseDao(){return baseDao;}

        public CouchbaseCounterDao build(){
            return new CouchbaseCounterDao(this);
        }
    }
}
