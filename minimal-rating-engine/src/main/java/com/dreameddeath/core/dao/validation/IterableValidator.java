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

package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.dao.exception.dao.ValidationException;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 29/08/2014.
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
