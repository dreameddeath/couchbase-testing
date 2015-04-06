/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;

import java.lang.reflect.Field;

/**
 * Created by ceaj8230 on 30/08/2014.
 */
@SuppressWarnings("ALL")
public class DynamicCouchbaseDocumentElementValidator implements Validator<Object>{
    final private Field _field;
    final ValidatorFactory _factory;

    public DynamicCouchbaseDocumentElementValidator(Field field,ValidatorFactory factory){
        _field=field;
        _factory = factory;
    }

    public void validate(Object elt,BaseCouchbaseDocumentElement parent) throws ValidationException{
        if(elt instanceof BaseCouchbaseDocumentElement)
            _factory.getValidator((BaseCouchbaseDocumentElement)elt).validate((BaseCouchbaseDocumentElement)elt,parent);
    }
}
