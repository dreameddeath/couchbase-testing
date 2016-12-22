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

package com.dreameddeath.core.dao.counter;


import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.couchbase.exception.StorageObservableException;
import com.dreameddeath.core.couchbase.impl.WriteParams;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.DaoObservableException;
import com.dreameddeath.core.dao.exception.InconsistentStateException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.utils.KeyPattern;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 02/09/2014.
 */
public class CouchbaseCounterDao{
    private ICouchbaseBucket client;
    private CouchbaseDocumentDao baseDao;
    private BlockingCouchbaseCounterDao blockingDao;
    private final KeyPattern keyPattern;
    private Long baseValue;
    private Long modulus;
    private Integer expiration;
    private CallingMode mode;

    public void setBaseDao(CouchbaseDocumentDao dao){baseDao=dao;}

    public void setClient(ICouchbaseBucket client){this.client = client;}
    public ICouchbaseBucket getClient(){
        if(client!=null) return client;
        else return baseDao.getClient();
    }

    public CouchbaseCounterDao(String key, Long baseValue, Long modulus, Integer expiration){
        if(key!=null){
            this.keyPattern=new KeyPattern(key);
        }
        else{
            this.keyPattern = null;
        }
        this.baseValue = baseValue;
        this.modulus = modulus;
        this.expiration = expiration;
        if((expiration==null) &&(baseValue ==null)){
            mode = CallingMode.BASE;
        }
        else if(expiration==null){
            mode = CallingMode.WITH_DEFAULT;
        }
        else{
            mode = CallingMode.WITH_DEFAULT_AND_EXPIRATION;
        }
        this.blockingDao = new BlockingCouchbaseCounterDao();
    }

    public CouchbaseCounterDao(Builder builder){
        this(builder.getKeyPattern(), builder.getBaseValue(), builder.getModulus(), builder.getExpiration().intValue());
        baseDao = builder.getBaseDao();
    }

    public String getKeyPattern(){
        return (keyPattern!=null)?keyPattern.getKeyPatternStr():null;
    }


    public BlockingCouchbaseCounterDao toBlockingDao() {
        return blockingDao;
    }

    public Observable<Long> asyncGetCounter(ICouchbaseSession session, String key, boolean isCalcOnly) {
        return asyncIncrCounter(session,key,0,isCalcOnly);
    }


    public Observable<Long> asyncIncrCounter(ICouchbaseSession session,String key,long by,boolean isCalcOnly){
        if(baseDao.isReadOnly() && by!=0){
            throw new DaoObservableException(new InconsistentStateException(null,"Cannot update counter <"+key+"> in readonly mode"));
        }
        Observable<Long> result;

        if(isCalcOnly){
            if(session.getKeyPrefix()!=null){
                result = getClient().asyncCounter(key, 0L, WriteParams.create().with(session.getKeyPrefix()));
            }
            else {
                result = getClient().asyncCounter(key, 0L);
            }
            result=result.map(val->((val<0)? baseValue :val)+by);
        }
        else{
            switch (mode) {
                case WITH_DEFAULT:
                    if(session.getKeyPrefix()!=null) {
                        result = getClient().asyncCounter(key, by, baseValue+by);
                    }
                    else{
                        result = getClient().asyncCounter(key, by, baseValue+by, WriteParams.create().with(session.getKeyPrefix()));
                    }
                    break;
                case WITH_DEFAULT_AND_EXPIRATION:
                    if(session.getKeyPrefix()!=null) {
                        result = getClient().asyncCounter(key, by, baseValue+by, expiration);
                    }
                    else{
                        result = getClient().asyncCounter(key, by, baseValue+by, expiration, WriteParams.create().with(session.getKeyPrefix()));
                    }
                    break;
                default:
                    if(session.getKeyPrefix()!=null) {
                        result = getClient().asyncCounter(key, by);
                    }
                    else{
                        result = getClient().asyncCounter(key, by, WriteParams.create().with(session.getKeyPrefix()));
                    }
            }
        }

        if (modulus != null) {
            return result.map(val->val%modulus);
        } else {
            return result;
        }

    }


    public Observable<Long> asyncDecrCounter(ICouchbaseSession session,String key,long by,boolean isCalcOnly){
        if(baseDao.isReadOnly() && by!=0){
            throw new DaoObservableException(new InconsistentStateException(null,"Cannot update counter <"+key+"> in readonly mode"));
        }
        Observable<Long> result;

        if(isCalcOnly){
            if(session.getKeyPrefix()!=null){
                result = getClient().asyncCounter(key, 0L, WriteParams.create().with(session.getKeyPrefix()));
            }
            else {
                result = getClient().asyncCounter(key, 0L);
            }
            result=result.map(val->((val<0)? baseValue :val)-by);
        }
        else{
            switch (mode) {
                case WITH_DEFAULT:
                    if(session.getKeyPrefix()!=null) {
                        result = getClient().asyncCounter(key, -by, baseValue);
                    }
                    else{
                        result = getClient().asyncCounter(key, -by, baseValue, WriteParams.create().with(session.getKeyPrefix()));
                    }
                    break;
                case WITH_DEFAULT_AND_EXPIRATION:
                    if(session.getKeyPrefix()!=null) {
                        result = getClient().asyncCounter(key, -by, baseValue, expiration);
                    }
                    else{
                        result = getClient().asyncCounter(key, -by, baseValue, expiration, WriteParams.create().with(session.getKeyPrefix()));
                    }
                    break;
                default:
                    if(session.getKeyPrefix()!=null) {
                        result = getClient().asyncCounter(key, -by);
                    }
                    else{
                        result = getClient().asyncCounter(key, -by, WriteParams.create().with(session.getKeyPrefix()));
                    }
            }
        }
        return result;
    }

    public enum CallingMode {
        BASE,
        WITH_DEFAULT,
        WITH_DEFAULT_AND_EXPIRATION
    }

    public static class Builder{
        private String keyPattern=null;
        private Long baseValue=0L;
        private Long expiration=0L;
        private Long modulus=null;
        private ICouchbaseBucket client;
        private CouchbaseDocumentDao baseDao;

        public Builder withKeyPattern(String key){
            keyPattern = key;
            return this;
        }

        public Builder withBaseValue(Long baseValue){
            this.baseValue = baseValue;
            return this;
        }

        public Builder withBaseValue(long baseValue){
            this.baseValue = baseValue;
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
            if(modulus!=null && modulus==0L){
                this.modulus=null;
            }
            else {
                this.modulus = modulus;
            }
            return this;
        }

        public Builder withModulus(long modulus){
            if(modulus==0L){
                this.modulus=null;
            }
            else {
                this.modulus = modulus;
            }
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
        public Long getBaseValue(){return baseValue;}
        public Long getExpiration(){return expiration;}
        public Long getModulus(){return modulus;}
        public ICouchbaseBucket getClient(){return client;}
        public CouchbaseDocumentDao getBaseDao(){return baseDao;}

        public CouchbaseCounterDao build(){
            return new CouchbaseCounterDao(this);
        }
    }

    public class BlockingCouchbaseCounterDao {

        public long blockingIncrCounter(ICouchbaseSession session, String key, long by, boolean isCalcOny, CouchbaseCounterDao parentCouchbaseCounterDao) throws DaoException,StorageException {
            try {
                return parentCouchbaseCounterDao.asyncIncrCounter(session, key, by, isCalcOny).toBlocking().single();
            }
            catch(DaoObservableException e){
                throw e.getCause();
            }
            catch(StorageObservableException e){
                throw e.getCause();
            }
        }

        public long blockingDecrCounter(ICouchbaseSession session, String key, long by, boolean isCalcOny, CouchbaseCounterDao parentCouchbaseCounterDao) throws DaoException,StorageException {
            try {
                return parentCouchbaseCounterDao.asyncDecrCounter(session, key, by, isCalcOny).toBlocking().single();
            }
            catch(DaoObservableException e){
                throw e.getCause();
            }
            catch(StorageObservableException e){
                throw e.getCause();
            }
        }

        public Long blockingGetCounter(ICouchbaseSession session, String key, boolean isCalcOnly, CouchbaseCounterDao parentCouchbaseCounterDao) throws DaoException,StorageException {
            try {
                return parentCouchbaseCounterDao.asyncGetCounter(session, key, isCalcOnly).toBlocking().single();
            }
            catch(DaoObservableException e){
                throw e.getCause();
            }
            catch(StorageObservableException e){
                throw e.getCause();
            }
        }
    }
}
