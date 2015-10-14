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

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.HasParent;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 30/08/2014.
 */
public class ValidatorFactory {
    private ValidatorCache cache = new ValidatorCache();


    public <T extends HasParent> Validator<T> getValidator(Class<T> clazz){
        Validator<T> validator = (Validator<T>)cache.get(clazz);
        if(validator==null){
            if(CouchbaseDocument.class.isAssignableFrom(clazz)){
                validator = (Validator<T>)new RawCouchbaseDocumentValidator(clazz,this);
                cache.put(clazz,validator);
            }
            else if(CouchbaseDocumentElement.class.isAssignableFrom(clazz)){
                validator = (Validator<T>)new CouchbaseDocumentElementValidator(clazz,this);
                cache.put(clazz,validator);
            }
        }
        return validator;
    }

    public <T extends CouchbaseDocumentElement> Validator<T> getValidator(T elt){
        return getValidator((Class<T>)elt.getClass());
    }

    public static class ValidatorCache extends ConcurrentHashMap<Class<? extends HasParent>, Validator<? extends HasParent>>{

    }
}
