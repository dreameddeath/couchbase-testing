package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ceaj8230 on 30/08/2014.
 */
public class ValidatorFactory {
    private ValidatorCache _cache = new ValidatorCache();


    public <T> Validator<T> getValidator(Class<T> clazz){
        Validator<BaseCouchbaseDocumentElement> validator = (Validator<BaseCouchbaseDocumentElement>)_cache.get(clazz);
        if(validator==null){
            if(BaseCouchbaseDocumentElement.class.isAssignableFrom(clazz)){
                validator = (Validator<BaseCouchbaseDocumentElement>)new CouchbaseDocumentElementValidator(clazz,this);
                _cache.put((Class<BaseCouchbaseDocumentElement>)clazz,validator);
            }
        }
        return (Validator<T>)validator;
    }

    public <T extends BaseCouchbaseDocumentElement> Validator<T> getValidator(T elt){
        return getValidator((Class<T>)elt.getClass());
    }

    public static class ValidatorCache extends ConcurrentHashMap<Class<? extends BaseCouchbaseDocumentElement>, Validator<? extends BaseCouchbaseDocumentElement>>{

    }
}
