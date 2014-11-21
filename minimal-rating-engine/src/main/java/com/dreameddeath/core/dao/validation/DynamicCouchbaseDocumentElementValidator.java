package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;

import java.lang.reflect.Field;

/**
 * Created by ceaj8230 on 30/08/2014.
 */
@SuppressWarnings("ALL")
public class DynamicCouchbaseDocumentElementValidator implements Validator<Object>{
    final private Field _field;
    final ValidatorFactory _factory;

    public DynamicCouchbaseDocumentElementValidator(Field field,ValidatorFactory factory){
        _field=field;
        _factory = factory;
    }

    public void validate(Object elt,BaseCouchbaseDocumentElement parent) throws ValidationException{
        if(elt instanceof BaseCouchbaseDocumentElement)
            _factory.getValidator((BaseCouchbaseDocumentElement)elt).validate((BaseCouchbaseDocumentElement)elt,parent);
    }
}
