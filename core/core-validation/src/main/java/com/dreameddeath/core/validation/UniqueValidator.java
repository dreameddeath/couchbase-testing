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
import com.dreameddeath.core.dao.model.IHasUniqueKeysRef;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.model.property.HasParent;
import com.dreameddeath.core.validation.annotation.Unique;
import com.dreameddeath.core.validation.exception.ValidationFailedException;

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
    public void validate(ValidatorContext ctxt,T value) throws ValidationException{
        if(value!=null){
            try {
                String valueStr=value.toString();
                CouchbaseDocument root = HasParent.Helper.getParentDocument(ctxt.head());
                if((root instanceof IHasUniqueKeysRef) && ((IHasUniqueKeysRef)root).isInDbKey(valueStr)){
                    ((IHasUniqueKeysRef) root).addDocUniqKeys(valueStr);
                    return;
                }
                else {
                    ctxt.getSession().addOrUpdateUniqueKey(root, valueStr, annotation.nameSpace());
                }
            }
            catch(DuplicateUniqueKeyException e){
                throw new ValidationFailedException(ctxt.head(),field,"Duplicate Exception for value" ,e);
            }
            catch(Exception e){
                throw new ValidationFailedException(ctxt.head(),field,"Other Exception",e);
            }
        }
    }
}
