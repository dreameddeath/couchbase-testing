package com.dreameddeath.core.validation;

import com.dreameddeath.core.exception.ValidationFailedException;
import com.dreameddeath.core.exception.validation.ValidationException;

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

    @Override
    public void validate(ValidatorContext ctxt,Iterable value) throws ValidationException{
        List<ValidationException> iterableExceptions=null;

        Iterator iter =value.iterator();
        Long pos=0L;
        while(iter.hasNext()){
            Object obj = iter.next();
            List<ValidationException> eltErrors=null;
            for(Validator<Object> validator:_validationRules){
                try {
                    validator.validate(ctxt,obj);
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
                iterableExceptions.add(new ValidationFailedException(ctxt.head(),pos,"",eltErrors));
            }

            ++pos;
        }
        if(iterableExceptions!=null){
            throw new ValidationFailedException(ctxt.head(),(AccessibleObject)_field,"Errors in iterable",iterableExceptions);
        }
    }

    /*public void validate(Iterable value, RawCouchbaseDocumentElement parent) throws ValidationFailedException {
        List<ValidationFailedException> iterableExceptions=null;

        Iterator iter =value.iterator();
        Long pos=0L;
        while(iter.hasNext()){
            Object obj = iter.next();
            List<ValidationFailedException> eltErrors=null;
            for(Validator<Object> validator:_validationRules){
                try {
                    validator.validate(obj, parent);
                }
                catch(ValidationFailedException e){
                    if(eltErrors==null){
                        eltErrors = new ArrayList<ValidationFailedException>();
                    }
                    eltErrors.add(e);
                }
            }
            if(eltErrors!=null){
                if(iterableExceptions==null){
                    iterableExceptions = new ArrayList<ValidationFailedException>();
                }
                iterableExceptions.add(new ValidationFailedException(parent,pos,"",eltErrors));
            }

            ++pos;
        }
        if(iterableExceptions!=null){
            throw new ValidationFailedException(parent,(AccessibleObject)_field,"Errors in iterable",iterableExceptions);
        }
    }*/

}