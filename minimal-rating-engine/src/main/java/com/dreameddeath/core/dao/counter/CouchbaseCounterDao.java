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


import com.dreameddeath.core.couchbase.CouchbaseBucketWrapper;

/**
 * Created by Christophe Jeunesse on 02/09/2014.
 */
public class CouchbaseCounterDao {
    private CouchbaseBucketWrapper client;
    private String keyPattern;
    private Long defaultValue;
    private Long modulus;
    private Integer expiration;

    private CallingMode mode;

    protected CouchbaseBucketWrapper getClientWrapper(){
        return client;
    }

    public CouchbaseCounterDao(CouchbaseBucketWrapper client,String key, Long defaultValue, Long modulus, Integer expiration){
        this.client = client;
        keyPattern = key;
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
        this(builder.getClient(), builder.getKeyPattern(), builder.getDefaultValue(), builder.getModulus(), builder.getExpiration().intValue());
    }

    public String getKeyPattern(){
        return keyPattern;
    }

    public long getCounter(String key,boolean isCalcOnly) {
        return incrCounter(key,0,isCalcOnly);
    }

    public long incrCounter(String key, long by,boolean isCalcOny) {
        long result;

        if(isCalcOny){
                result = client.getBucket().counter(key, 0).content();
                if(result<0){
                    result=defaultValue;
                }
                result+=by;
        }
        else{
            switch (mode) {
                case WITH_DEFAULT:
                    result = client.getBucket().counter(key, by, defaultValue).content();
                    break;
                case WITH_DEFAULT_AND_EXPIRATION:
                    result = client.getBucket().counter(key, by, defaultValue, expiration).content();
                    break;
                default:
                    result = client.getBucket().counter(key, by).content();
            }
        }
        if (modulus != null) {
            return result % modulus;
        } else {
            return result;
        }
    }

    public long decrCounter(String key, long by,boolean isCalcOny) {
        if(isCalcOny){
            long result;
            result = client.getBucket().counter(key, 0).content();
            if(result<0){
                result=defaultValue;
            }
            result-=by;

            return result;
        }
        else {
            switch (mode) {
                case WITH_DEFAULT:
                    return client.getBucket().counter(key,-by, defaultValue).content();
                case WITH_DEFAULT_AND_EXPIRATION:
                    return client.getBucket().counter(key, -by, defaultValue, expiration).content();
                default:
                    return client.getBucket().counter(key,-by).content();
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
        private CouchbaseBucketWrapper client;

        public Builder withKeyPattern(String key){
            keyPattern = key;
            return this;
        }

        public Builder withDefaultValue(Long defaultVal){
            defaultValue = defaultVal;
            return this;
        }

        public Builder withExpiration(Long expiration){
            this.expiration = expiration;
            return this;
        }

        public Builder withModulus(Long modulus){
            this.modulus = modulus;
            return this;
        }

        public Builder withClient(CouchbaseBucketWrapper client){
            this.client = client;
            return this;
        }

        public String getKeyPattern(){return keyPattern;}
        public Long getDefaultValue(){return defaultValue;}
        public Long getExpiration(){return expiration;}
        public Long getModulus(){return modulus;}
        public CouchbaseBucketWrapper getClient(){return client;}
    }
}
