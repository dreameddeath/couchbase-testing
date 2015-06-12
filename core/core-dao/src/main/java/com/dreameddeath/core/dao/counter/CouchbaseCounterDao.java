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
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;

/**
 * Created by Christophe Jeunesse on 02/09/2014.
 */
public class CouchbaseCounterDao{
    private ICouchbaseBucket _client;
    private CouchbaseDocumentDao _baseDao;
    private String _keyPattern;
    private Long _defaultValue;
    private Long _modulus;
    private Integer _expiration;

    private CallingMode _mode;

    public void setBaseDao(CouchbaseDocumentDao dao){_baseDao=dao;}
    public void setClient(ICouchbaseBucket client){_client = client;}
    public ICouchbaseBucket getClient(){
        if(_client!=null) return _client;
        else return _baseDao.getClient();
    }

    public CouchbaseCounterDao(String key, Long defaultValue, Long modulus, Integer expiration){
        _keyPattern = key;
        _defaultValue = defaultValue;
        _modulus = modulus;
        _expiration = expiration;
        if((expiration==null) &&(defaultValue==null)){
            _mode = CallingMode.BASE;
        }
        else if(expiration==null){
            _mode = CallingMode.WITH_DEFAULT;
        }
        else{
            _mode = CallingMode.WITH_DEFAULT_AND_EXPIRATION;
        }
    }

    public CouchbaseCounterDao(Builder builder){
        this(builder.getKeyPattern(), builder.getDefaultValue(), builder.getModulus(), builder.getExpiration().intValue());
        _baseDao = builder.getBaseDao();
    }

    public String getKeyPattern(){
        return _keyPattern;
    }

    public Long getCounter(String key,boolean isCalcOnly) throws StorageException {
        return incrCounter(key,0,isCalcOnly);
    }

    public long incrCounter(String key, long by,boolean isCalcOny) throws StorageException {
        long result;

        if(isCalcOny){
                result = getClient().counter(key, 0L);
                if(result<0){
                    result=_defaultValue;
                }
                result+=by;
        }
        else{
            switch (_mode) {
                case WITH_DEFAULT:
                    result = getClient().counter(key, by, _defaultValue);
                    break;
                case WITH_DEFAULT_AND_EXPIRATION:
                    result = getClient().counter(key, by, _defaultValue, _expiration);
                    break;
                default:
                    result = getClient().counter(key, by);
            }
        }
        if (_modulus != null) {
            return result % _modulus;
        } else {
            return result;
        }
    }

    public long decrCounter(String key, long by,boolean isCalcOny) throws StorageException {
        if(isCalcOny){
            long result;
            result = getClient().counter(key, 0L);
            if(result<0){
                result=_defaultValue;
            }
            result-=by;

            return result;
        }
        else {
            switch (_mode) {
                case WITH_DEFAULT:
                    return getClient().counter(key,-by, _defaultValue);
                case WITH_DEFAULT_AND_EXPIRATION:
                    return getClient().counter(key, -by, _defaultValue, _expiration);
                default:
                    return getClient().counter(key,-by);
            }
        }
    }

    public enum CallingMode {
        BASE,
        WITH_DEFAULT,
        WITH_DEFAULT_AND_EXPIRATION
    }

    public static class Builder{
        private String _keyPattern;
        private Long _defaultValue;
        private Long _expiration=0L;
        private Long _modulus;
        private ICouchbaseBucket _client;
        private CouchbaseDocumentDao _baseDao;

        public Builder withKeyPattern(String key){
            _keyPattern = key;
            return this;
        }

        public Builder withDefaultValue(Long defaultVal){
            _defaultValue = defaultVal;
            return this;
        }

        public Builder withDefaultValue(long defaultVal){
            _defaultValue = defaultVal;
            return this;
        }

        public Builder withExpiration(Long expiration){
            _expiration = expiration;
            return this;
        }

        public Builder withExpiration(long expiration){
            _expiration = expiration;
            return this;
        }

        public Builder withModulus(Long modulus){
            _modulus = modulus;
            return this;
        }

        public Builder withModulus(long modulus){
            _modulus = modulus;
            return this;
        }

        public Builder withClient(ICouchbaseBucket client){
            _client = client;
            return this;
        }

        public Builder withBaseDao(CouchbaseDocumentDao dao){
            _baseDao = dao;
            return this;
        }

        public String getKeyPattern(){return _keyPattern;}
        public Long getDefaultValue(){return _defaultValue;}
        public Long getExpiration(){return _expiration;}
        public Long getModulus(){return _modulus;}
        public ICouchbaseBucket getClient(){return _client;}
        public CouchbaseDocumentDao getBaseDao(){return _baseDao;}

        public CouchbaseCounterDao build(){
            return new CouchbaseCounterDao(this);
        }
    }
}
