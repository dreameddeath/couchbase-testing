package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.annotation.Unique;
import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

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
    public void validate(T value, CouchbaseDocumentElement parent) throws ValidationException{
        if(value!=null){
            parent.getParentDocument().getSession().addUniqueKey(parent.getParentDocument(),value,_annotation.nameSpace());
        }

    }
}
