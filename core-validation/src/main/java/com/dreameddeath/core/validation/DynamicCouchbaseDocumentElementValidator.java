package com.dreameddeath.core.validation;

import com.dreameddeath.core.exception.ValidationException;
import com.dreameddeath.core.model.common.RawCouchbaseDocumentElement;

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

    public void validate(Object elt,RawCouchbaseDocumentElement parent) throws ValidationException{
        if(elt instanceof RawCouchbaseDocumentElement)
            _factory.getValidator((RawCouchbaseDocumentElement)elt).validate((RawCouchbaseDocumentElement)elt,parent);
    }
}
