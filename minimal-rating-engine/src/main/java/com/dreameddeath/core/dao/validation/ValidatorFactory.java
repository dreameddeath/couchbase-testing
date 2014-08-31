package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ceaj8230 on 30/08/2014.
 */
public class ValidatorFactory {
    private ValidatorCache _cache = new ValidatorCache();


    public <T extends Object> Validator<T> getValidator(Class<T> clazz){
        Validator<CouchbaseDocumentElement> validator = (Validator<CouchbaseDocumentElement>)_cache.get(clazz);
        if(validator==null){
            if(CouchbaseDocumentElement.class.isAssignableFrom(clazz)){
                validator = (Validator<CouchbaseDocumentElement>)new CouchbaseDocumentElementValidator(clazz,this);
                _cache.put((Class<CouchbaseDocumentElement>)clazz,validator);
            }
        }
        return (Validator<T>)validator;
    }

    public <T extends CouchbaseDocumentElement> Validator<T> getValidator(T elt){
        return getValidator((Class<T>)elt.getClass());
    }

    public static class ValidatorCache extends ConcurrentHashMap<Class<? extends CouchbaseDocumentElement>, Validator<? extends CouchbaseDocumentElement>>{

    }
}
