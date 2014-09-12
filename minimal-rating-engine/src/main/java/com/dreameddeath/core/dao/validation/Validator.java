package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public interface Validator<T> {
    public void validate(T value, BaseCouchbaseDocumentElement parent) throws ValidationException;
}
