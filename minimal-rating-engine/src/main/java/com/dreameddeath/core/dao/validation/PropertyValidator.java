package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ceaj8230 on 01/09/2014.
 */
public class PropertyValidator<T> implements Validator<Property<T>>{
    private Member _field;
    private List<Validator<Object>> _validationRules = new ArrayList<Validator<Object>>();

    public PropertyValidator(Member field){
        _field = field;
    }
    public void addRule(Validator<Object> validator){
        _validationRules.add(validator);
    }


    public void validate(Property<T> elt,CouchbaseDocumentElement parent)throws ValidationException{
        T obj=elt.get();
        List<ValidationException> eltErrors=null;
        for(Validator<Object> validator:_validationRules){
            try {
                validator.validate(obj, parent);
            }
            catch(ValidationException e){
                if(eltErrors==null){
                    eltErrors = new ArrayList<ValidationException>();
                }
                eltErrors.add(e);
            }
        }
        if(eltErrors!=null) {
            throw new ValidationException(parent,(AccessibleObject)_field,"Errors in Property",eltErrors);
        }
    }
}
