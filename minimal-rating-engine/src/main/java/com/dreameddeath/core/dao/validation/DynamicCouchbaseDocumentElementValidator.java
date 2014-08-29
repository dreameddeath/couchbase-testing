package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by ceaj8230 on 30/08/2014.
 */
public class DynamicCouchbaseDocumentElementValidator implements Validator<CouchbaseDocumentElement>{
    final private Field _field;
    final Map<Class<? extends CouchbaseDocumentElement>,Validator<? extends CouchbaseDocumentElement>> _cache;

    public DynamicCouchbaseDocumentElementValidator(Field field,Map<Class<? extends CouchbaseDocumentElement>,Validator<? extends CouchbaseDocumentElement>> cache){
        _field=field;
        _cache = cache;
    }

    public void validate(CouchbaseDocumentElement elt,CouchbaseDocumentElement parent) throws ValidationException{
        Validator<CouchbaseDocumentElement> validator = (Validator<CouchbaseDocumentElement>)_cache.get(elt.getClass());
        if(validator==null){
            validator = new CouchbaseDocumentElementValidator(elt.getClass(),_cache);
            _cache.put(elt.getClass(),validator);
        }
        validator.validate(elt,parent);
    }
}
