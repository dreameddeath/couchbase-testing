/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.validation.utils;

import com.dreameddeath.core.dao.exception.DaoObservableException;
import com.dreameddeath.core.dao.exception.DuplicateUniqueKeyDaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.exception.validation.ValidationFailure;
import com.dreameddeath.core.dao.exception.validation.ValidationObservableException;
import com.dreameddeath.core.validation.exception.ValidationCompositeFailure;

/**
 * Created by Christophe Jeunesse on 17/11/2016.
 */
public class ValidationExceptionUtils {
    public static DuplicateUniqueKeyDaoException findUniqueKeyException(ValidationFailure validationFailure){
        if (validationFailure instanceof ValidationCompositeFailure) {
            DuplicateUniqueKeyDaoException duplicateUniqueKeyDaoException = ((ValidationCompositeFailure) validationFailure).findException(DuplicateUniqueKeyDaoException.class);
            if (duplicateUniqueKeyDaoException != null) {
                return duplicateUniqueKeyDaoException;
            }
        }
        return null;
    }

    public static DuplicateUniqueKeyDaoException findUniqueKeyException(ValidationException validationException){
        return findUniqueKeyException(validationException.getFailure());
    }


    public static DuplicateUniqueKeyDaoException findUniqueKeyException(ValidationObservableException validationObsException){
        return findUniqueKeyException(validationObsException.getCause().getFailure());
    }

    public static DuplicateUniqueKeyDaoException findUniqueKeyException(Throwable throwable){
        if(throwable instanceof ValidationObservableException) {
            return findUniqueKeyException((ValidationObservableException)throwable);
        }
        else if(throwable instanceof ValidationException){
            return findUniqueKeyException((ValidationException) throwable);
        }
        else if(throwable instanceof DuplicateUniqueKeyDaoException){
            return (DuplicateUniqueKeyDaoException)throwable;
        }
        else if(throwable instanceof DaoObservableException && throwable.getCause()!=null && throwable.getCause() instanceof DuplicateUniqueKeyDaoException){
            return (DuplicateUniqueKeyDaoException)throwable.getCause();
        }
        return null;
    }
}
