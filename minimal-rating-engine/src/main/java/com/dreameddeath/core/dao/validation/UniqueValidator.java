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

import com.dreameddeath.core.annotation.Unique;
import com.dreameddeath.core.couchbase.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.dao.exception.dao.ValidationException;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;

import java.lang.reflect.Field;

/**
 * Created by Christophe Jeunesse on 06/08/2014.
 */
public class UniqueValidator<T> implements Validator<T> {
    private Unique _annotation;
    private Field _field;
    public UniqueValidator(Field field,Unique ann){
        _annotation = ann;
        _field = field;
    }

    public void validate(T value, BaseCouchbaseDocumentElement parent) throws ValidationException{
        if(value!=null){
            try {
                parent.getParentDocument().getBaseMeta().getSession().addOrUpdateUniqueKey((CouchbaseDocument)parent.getParentDocument(), value, _annotation.nameSpace());
            }
            catch(DuplicateUniqueKeyException e){
                throw new ValidationException(parent,_field,"Duplicate Exception for value" ,e);
            }
            catch(Exception e){
                throw new ValidationException(parent,_field,"Other Exception",e);
            }
        }
    }
}
