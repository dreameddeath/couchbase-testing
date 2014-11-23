package com.dreameddeath.core.validation;

import com.dreameddeath.core.exception.ValidationFailedException;
import com.dreameddeath.core.annotation.NotNull;

import java.lang.reflect.Field;

/**
 * Created by ceaj8230 on 06/08/2014.
 */
public class NotNullValidator<T> implements Validator<T> {
    final private Field _field;
    public NotNullValidator(Field field,NotNull ann){
        _field = field;
    }

    public void validate(ValidatorContext ctxt,T value) throws ValidationFailedException {
        if(value==null){
            throw new ValidationFailedException(ctxt.head(),_field,"The field should be set");
        }
    }
}
