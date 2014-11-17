package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;

import java.lang.reflect.Field;

/**
 * Created by ceaj8230 on 06/08/2014.
 */
public class NotNullValidator<T> implements Validator<T> {
    final private Field _field;
    public NotNullValidator(Field field,NotNull ann){
        _field = field;
    }
    public void validate(T value,BaseCouchbaseDocumentElement parent) throws ValidationException{
        if(value==null){
            throw new ValidationException(parent,_field,"The field should be set");
        }
    }
}