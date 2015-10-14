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
import com.dreameddeath.core.model.property.Property;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 01/09/2014.
 */
public class PropertyValidator<T> implements Validator<Property<T>>{
    private Member field;
    private List<Validator<Object>> validationRules = new ArrayList<Validator<Object>>();

    public PropertyValidator(Member field){
        this.field = field;
    }
    public void addRule(Validator<Object> validator){
        validationRules.add(validator);
    }


    public void validate(Property<T> elt,BaseCouchbaseDocumentElement parent)throws ValidationException{
        T obj=elt.get();
        List<ValidationException> eltErrors=null;
        for(Validator<Object> validator:validationRules){
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
            throw new ValidationException(parent,(AccessibleObject)field,"Errors in Property",eltErrors);
        }
    }
}
