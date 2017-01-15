/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.validation;

import com.dreameddeath.core.dao.exception.validation.ValidationFailure;
import com.dreameddeath.core.validation.exception.ValidationCompositeFailure;
import io.reactivex.Maybe;
import io.reactivex.Observable;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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
    public Maybe<? extends ValidationFailure> asyncValidate(ValidatorContext ctxt, Iterable value){
        List<Observable<? extends ValidationFailure>> iterableExceptions=new ArrayList<>();

        Iterator iter =value.iterator();
        Long pos=0L;
        while(iter.hasNext()){
            final Long currPos=pos;
            Object obj = iter.next();
            List<Observable<? extends ValidationFailure>> eltErrors=
                    validationRules.stream()
                            .map(validator->validator.asyncValidate(ctxt,obj))
                            .map(Maybe::toObservable)
                            .collect(Collectors.toList());
            iterableExceptions.add(Observable.merge(eltErrors)
                    .reduce(new ValidationCompositeFailure(ctxt.head(),currPos,""),
                            ValidationCompositeFailure::addChildElement)
                    .filter(ValidationCompositeFailure::hasError)
                    .toObservable()
            );
            ++pos;
        }
        return Observable.merge(iterableExceptions)
                .reduce(new ValidationCompositeFailure(ctxt.head(),"error in iterable"),
                        ValidationCompositeFailure::addChildElement)
                .filter(ValidationCompositeFailure::hasError);
    }

}
