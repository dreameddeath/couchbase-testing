package com.dreameddeath.core.validation;

import com.dreameddeath.core.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.exception.ValidationFailedException;
import com.dreameddeath.core.exception.validation.ValidationException;
import com.dreameddeath.core.model.common.RawCouchbaseDocument;
import com.dreameddeath.core.annotation.Unique;
import com.dreameddeath.core.model.property.HasParent;

import java.lang.reflect.Field;

/**
 * Created by Christophe Jeunesse on 06/08/2014.
 */
public class UniqueValidator<T> implements Validator<T> {
    private Unique _annotation;
    private Field _field;
    public UniqueValidator(Field field,Unique ann){
        _annotation = ann;
        _field = field;
    }

    @Override
    public void validate(ValidatorContext ctxt,T value) throws ValidationException{
        if(value!=null){
            try {
                ctxt.getSession().addOrUpdateUniqueKey(HasParent.Helper.getParentDocument((RawCouchbaseDocument)ctxt.head()), value, _annotation.nameSpace());
            }
            catch(DuplicateUniqueKeyException e){
                throw new ValidationFailedException(ctxt.head(),_field,"Duplicate Exception for value" ,e);
            }
            catch(Exception e){
                throw new ValidationFailedException(ctxt.head(),_field,"Other Exception",e);
            }
        }
    }
}
