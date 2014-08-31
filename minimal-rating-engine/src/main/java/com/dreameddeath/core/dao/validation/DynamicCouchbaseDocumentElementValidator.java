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
    final ValidatorFactory _factory;

    public DynamicCouchbaseDocumentElementValidator(Field field,ValidatorFactory factory){
        _field=field;
        _factory = factory;
    }

    public void validate(CouchbaseDocumentElement elt,CouchbaseDocumentElement parent) throws ValidationException{
        _factory.getValidator(elt).validate(elt,parent);
    }
}
