package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.annotation.Unique;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.exception.storage.DuplicateKeyException;
import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;

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

    public void validate(T value, BaseCouchbaseDocumentElement parent) throws ValidationException{
        if(value!=null){
            try {
                parent.getParentDocument().getSession().addOrUpdateUniqueKey(parent.getParentDocument(), value, _annotation.nameSpace());
            }
            catch(DuplicateKeyException e){
                throw new ValidationException(parent,_field,"Duplicate Exception for value" ,e);
            }
            catch(Exception e){
                throw new ValidationException(parent,_field,"Other Exception",e);
            }
        }
    }
}
