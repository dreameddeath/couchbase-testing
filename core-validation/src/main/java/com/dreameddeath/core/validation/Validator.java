package com.dreameddeath.core.validation;

import com.dreameddeath.core.exception.ValidationException;
import com.dreameddeath.core.model.common.RawCouchbaseDocumentElement;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public interface Validator<T> {
    public void validate(T value, RawCouchbaseDocumentElement parent) throws ValidationException;
}
