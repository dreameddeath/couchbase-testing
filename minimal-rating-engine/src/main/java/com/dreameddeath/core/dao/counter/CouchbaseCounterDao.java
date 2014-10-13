package com.dreameddeath.core.dao.counter;


import com.dreameddeath.core.storage.CouchbaseBucketWrapper;

/**
 * Created by ceaj8230 on 02/09/2014.
 */
public class CouchbaseCounterDao {
    private CouchbaseBucketWrapper _client;
    private String _keyPattern;
    private Long _defaultValue;
    private Long _modulus;
    private Integer _expiration;

    private CallingMode _mode;

    protected CouchbaseBucketWrapper getClientWrapper(){
        return _client;
    }

    public CouchbaseCounterDao(CouchbaseBucketWrapper client,String key, Long defaultValue, Long modulus, Integer expiration){
        _client = client;
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
        this(builder.getClient(), builder.getKeyPattern(), builder.getDefaultValue(), builder.getModulus(), builder.getExpiration().intValue());
    }

    public String getKeyPattern(){
        return _keyPattern;
    }

    public long getCounter(String key,boolean isCalcOnly) {
        return incrCounter(key,0,isCalcOnly);
    }

    public long incrCounter(String key, long by,boolean isCalcOny) {
        long result;

        if(isCalcOny){
                result = _client.getBucket().counter(key, 0).content();
                if(result<0){
                    result=_defaultValue;
                }
                result+=by;
        }
        else{
            switch (_mode) {
                case WITH_DEFAULT:
                    result = _client.getBucket().counter(key, by, _defaultValue).content();
                    break;
                case WITH_DEFAULT_AND_EXPIRATION:
                    result = _client.getBucket().counter(key, by, _defaultValue, _expiration).content();
                    break;
                default:
                    result = _client.getBucket().counter(key, by).content();
            }
        }
        if (_modulus != null) {
            return result % _modulus;
        } else {
            return result;
        }
    }

    public long decrCounter(String key, long by,boolean isCalcOny) {
        if(isCalcOny){
            long result;
            result = _client.getBucket().counter(key, 0).content();
            if(result<0){
                result=_defaultValue;
            }
            result-=by;

            return result;
        }
        else {
            switch (_mode) {
                case WITH_DEFAULT:
                    return _client.getBucket().counter(key,-by, _defaultValue).content();
                case WITH_DEFAULT_AND_EXPIRATION:
                    return _client.getBucket().counter(key, -by, _defaultValue, _expiration).content();
                default:
                    return _client.getBucket().counter(key,-by).content();
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
        private CouchbaseBucketWrapper _client;

        public Builder withKeyPattern(String key){
            _keyPattern = key;
            return this;
        }

        public Builder withDefaultValue(Long defaultVal){
            _defaultValue = defaultVal;
            return this;
        }

        public Builder withExpiration(Long expiration){
            _expiration = expiration;
            return this;
        }

        public Builder withModulus(Long modulus){
            _modulus = modulus;
            return this;
        }

        public Builder withClient(CouchbaseBucketWrapper client){
            _client = client;
            return this;
        }

        public String getKeyPattern(){return _keyPattern;}
        public Long getDefaultValue(){return _defaultValue;}
        public Long getExpiration(){return _expiration;}
        public Long getModulus(){return _modulus;}
        public CouchbaseBucketWrapper getClient(){return _client;}
    }
}
