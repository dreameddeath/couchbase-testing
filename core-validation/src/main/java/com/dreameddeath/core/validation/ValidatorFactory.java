package com.dreameddeath.core.validation;

import com.dreameddeath.core.model.common.RawCouchbaseDocumentElement;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ceaj8230 on 30/08/2014.
 */
public class ValidatorFactory {
    private ValidatorCache _cache = new ValidatorCache();


    public <T> Validator<T> getValidator(Class<T> clazz){
        Validator<RawCouchbaseDocumentElement> validator = (Validator<RawCouchbaseDocumentElement>)_cache.get(clazz);
        if(validator==null){
            if(RawCouchbaseDocumentElement.class.isAssignableFrom(clazz)){
                validator = (Validator<RawCouchbaseDocumentElement>)new CouchbaseDocumentElementValidator(clazz,this);
                _cache.put((Class<RawCouchbaseDocumentElement>)clazz,validator);
            }
        }
        return (Validator<T>)validator;
    }

    public <T extends RawCouchbaseDocumentElement> Validator<T> getValidator(T elt){
        return getValidator((Class<T>)elt.getClass());
    }

    public static class ValidatorCache extends ConcurrentHashMap<Class<? extends RawCouchbaseDocumentElement>, Validator<? extends RawCouchbaseDocumentElement>>{

    }
}
