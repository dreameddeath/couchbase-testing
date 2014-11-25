package com.dreameddeath.core.validation;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.HasParent;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ceaj8230 on 30/08/2014.
 */
public class ValidatorFactory {
    private ValidatorCache _cache = new ValidatorCache();


    public <T extends HasParent> Validator<T> getValidator(Class<T> clazz){
        Validator<T> validator = (Validator<T>)_cache.get(clazz);
        if(validator==null){
            if(CouchbaseDocument.class.isAssignableFrom(clazz)){
                validator = (Validator<T>)new RawCouchbaseDocumentValidator(clazz,this);
                _cache.put(clazz,validator);
            }
            else if(CouchbaseDocumentElement.class.isAssignableFrom(clazz)){
                validator = (Validator<T>)new CouchbaseDocumentElementValidator(clazz,this);
                _cache.put(clazz,validator);
            }
        }
        return validator;
    }

    public <T extends CouchbaseDocumentElement> Validator<T> getValidator(T elt){
        return getValidator((Class<T>)elt.getClass());
    }

    public static class ValidatorCache extends ConcurrentHashMap<Class<? extends HasParent>, Validator<? extends HasParent>>{

    }
}
