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

import com.dreameddeath.core.dao.exception.validation.ValidationFailure;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.validation.exception.ValidationCompositeFailure;
import rx.Observable;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public Observable<? extends ValidationFailure> asyncValidate(ValidatorContext ctxt, Property<T> elt){
        final T obj = elt.get();
        List<Observable<? extends ValidationFailure>> eltErrors=
                validationRules.stream()
                        .map(validator->validator.asyncValidate(ctxt,obj))
                        .collect(Collectors.toList());

        return Observable.merge(eltErrors)
                .reduce(new ValidationCompositeFailure(ctxt.head(), (AccessibleObject) field, "Errors in Property"),
                        (global,res)->global.addChildElement(res))
                .filter(ValidationCompositeFailure::hasError);

    }
}
