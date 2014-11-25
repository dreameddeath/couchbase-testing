package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ceaj8230 on 29/08/2014.
 */
public class IterableValidator implements Validator<Iterable<?>> {
    private Member _field;
    private List<Validator<Object>> _validationRules = new ArrayList<Validator<Object>>();

    public IterableValidator(Member field){
        _field = field;
    }

    public void addRule(Validator<Object> validator){
        _validationRules.add(validator);
    }

    public void validate(Iterable value, BaseCouchbaseDocumentElement parent) throws ValidationException{
        List<ValidationException> iterableExceptions=null;

        Iterator iter =value.iterator();
        Long pos=0L;
        while(iter.hasNext()){
            Object obj = iter.next();
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
            if(eltErrors!=null){
                if(iterableExceptions==null){
                    iterableExceptions = new ArrayList<ValidationException>();
                }
                iterableExceptions.add(new ValidationException(parent,pos,"",eltErrors));
            }

            ++pos;
        }
        if(iterableExceptions!=null){
            throw new ValidationException(parent,(AccessibleObject)_field,"Errors in iterable",iterableExceptions);
        }
    }

}
