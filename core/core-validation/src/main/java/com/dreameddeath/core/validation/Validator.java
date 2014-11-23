package com.dreameddeath.core.validation;

import com.dreameddeath.core.exception.validation.ValidationException;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public interface Validator<T> {
    public void validate(ValidatorContext context,T value) throws ValidationException;
}
