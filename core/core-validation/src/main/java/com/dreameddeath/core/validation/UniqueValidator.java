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

import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.DaoObservableException;
import com.dreameddeath.core.dao.exception.DuplicateUniqueKeyDaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationFailure;
import com.dreameddeath.core.dao.model.IHasUniqueKeysRef;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.HasParent;
import com.dreameddeath.core.validation.annotation.Unique;
import com.dreameddeath.core.validation.exception.ValidationCompositeFailure;
import rx.Observable;

import java.lang.reflect.Field;

/**
 * Created by Christophe Jeunesse on 06/08/2014.
 */
public class UniqueValidator<T> implements Validator<T> {
    private Unique annotation;
    private Field field;
    public UniqueValidator(Field field,Unique ann){
        annotation = ann;
        this.field = field;
    }

    @Override
    public Observable<? extends ValidationFailure> asyncValidate(ValidatorContext ctxt, T value){
        if(value!=null){
            try {
                String valueStr = value.toString();
                CouchbaseDocument root = HasParent.Helper.getParentDocument(ctxt.head());
                String uniqueKey = ctxt.getSession().buildUniqueKey(root, valueStr,annotation.nameSpace());
                if ((root instanceof IHasUniqueKeysRef) && ((IHasUniqueKeysRef) root).isInDbKey(uniqueKey)) {
                    ((IHasUniqueKeysRef) root).addDocUniqKeys(uniqueKey);
                    return Observable.empty();
                } else {
                    return ctxt.getSession().asyncAddOrUpdateUniqueKey(root, valueStr, annotation.nameSpace())
                            .filter(foundKey -> false)
                            .map(foundKey -> new ValidationCompositeFailure(root, "Shouldn't occur"))
                            .onErrorResumeNext(throwable -> {
                                if (throwable instanceof DaoObservableException && throwable.getCause() instanceof DuplicateUniqueKeyDaoException) {
                                    return Observable.just(new ValidationCompositeFailure(ctxt.head(), field, "Duplicate Exception for value", throwable.getCause()));
                                }
                                return Observable.just(new ValidationCompositeFailure(ctxt.head(), field, "Other Exception", throwable));
                            });
                }
            }
            catch(DaoException e){
                throw new DaoObservableException(e);
            }
        }
        return Observable.empty();
    }
}
