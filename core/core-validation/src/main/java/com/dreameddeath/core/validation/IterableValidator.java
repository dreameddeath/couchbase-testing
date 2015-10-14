/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.validation;

import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.validation.exception.ValidationFailedException;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 29/08/2014.
 */
public class IterableValidator implements Validator<Iterable<?>> {
    private Member field;
    private List<Validator<Object>> validationRules = new ArrayList<Validator<Object>>();

    public IterableValidator(Member field){
        this.field = field;
    }

    public void addRule(Validator<Object> validator){
        validationRules.add(validator);
    }

    @Override
    public void validate(ValidatorContext ctxt,Iterable value) throws ValidationException{
        List<ValidationException> iterableExceptions=null;

        Iterator iter =value.iterator();
        Long pos=0L;
        while(iter.hasNext()){
            Object obj = iter.next();
            List<ValidationException> eltErrors=null;
            for(Validator<Object> validator:validationRules){
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
            throw new ValidationFailedException(ctxt.head(),(AccessibleObject)field,"Errors in iterable",iterableExceptions);
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
