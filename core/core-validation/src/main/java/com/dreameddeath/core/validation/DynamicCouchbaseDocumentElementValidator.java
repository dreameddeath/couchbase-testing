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
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

import java.lang.reflect.Field;

/**
 * Created by Christophe Jeunesse on 30/08/2014.
 */
public class DynamicCouchbaseDocumentElementValidator implements Validator<Object>{
    final private Field _field;
    final ValidatorFactory _factory;

    public DynamicCouchbaseDocumentElementValidator(Field field,ValidatorFactory factory){
        _field=field;
        _factory = factory;
    }

    public void validate(ValidatorContext ctxt,Object elt) throws ValidationException{
        if(elt instanceof CouchbaseDocumentElement){
            Validator<CouchbaseDocumentElement> validator = _factory.getValidator((CouchbaseDocumentElement)elt);
            validator.validate(ctxt,(CouchbaseDocumentElement)elt);
        }
    }
    /*public void validate(Object elt,RawCouchbaseDocumentElement parent) throws ValidationFailedException {
        if(elt instanceof RawCouchbaseDocumentElement)
            _factory.getValidator((RawCouchbaseDocumentElement)elt).validate((RawCouchbaseDocumentElement)elt,parent);
    }*/
}
