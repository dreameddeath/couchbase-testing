package com.dreameddeath.core.validation;

import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.exception.ValidationException;
import com.dreameddeath.core.model.common.RawCouchbaseDocumentElement;

import java.lang.reflect.Field;

/**
 * Created by ceaj8230 on 06/08/2014.
 */
public class NotNullValidator<T> implements Validator<T> {
    final private Field _field;
    public NotNullValidator(Field field,NotNull ann){
        _field = field;
    }
    public void validate(T value,RawCouchbaseDocumentElement parent) throws ValidationException{
        if(value==null){
            throw new ValidationException(parent,_field,"The field should be set");
        }
    }
}
