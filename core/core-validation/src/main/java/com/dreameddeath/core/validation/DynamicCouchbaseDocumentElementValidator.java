package com.dreameddeath.core.validation;

import com.dreameddeath.core.exception.validation.ValidationException;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

import java.lang.reflect.Field;

/**
 * Created by ceaj8230 on 30/08/2014.
 */
public class DynamicCouchbaseDocumentElementValidator implements Validator<Object>{
    final private Field _field;
    final ValidatorFactory _factory;

    public DynamicCouchbaseDocumentElementValidator(Field field,ValidatorFactory factory){
        _field=field;
        _factory = factory;
    }

    public void validate(ValidatorContext ctxt,Object elt) throws ValidationException{
        if(elt instanceof CouchbaseDocumentElement){
            Validator<CouchbaseDocumentElement> validator = _factory.getValidator((CouchbaseDocumentElement)elt);
            validator.validate(ctxt,(CouchbaseDocumentElement)elt);
        }
    }
    /*public void validate(Object elt,RawCouchbaseDocumentElement parent) throws ValidationFailedException {
        if(elt instanceof RawCouchbaseDocumentElement)
            _factory.getValidator((RawCouchbaseDocumentElement)elt).validate((RawCouchbaseDocumentElement)elt,parent);
    }*/
}
