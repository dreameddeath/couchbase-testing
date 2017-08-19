/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.validation.utils;

import com.dreameddeath.core.dao.exception.DuplicateUniqueKeyDaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.exception.validation.ValidationFailure;
import com.dreameddeath.core.validation.exception.ValidationCompositeFailure;

import java.util.Optional;

/**
 * Created by Christophe Jeunesse on 17/11/2016.
 */
public class ValidationExceptionUtils {
    public static Optional<DuplicateUniqueKeyDaoException> findUniqueKeyException(ValidationFailure validationFailure){
        if (validationFailure instanceof ValidationCompositeFailure) {
            DuplicateUniqueKeyDaoException duplicateUniqueKeyDaoException = ((ValidationCompositeFailure) validationFailure).findException(DuplicateUniqueKeyDaoException.class);
            if (duplicateUniqueKeyDaoException != null) {
                return Optional.of(duplicateUniqueKeyDaoException);
            }
        }
        return Optional.empty();
    }

    public static Optional<DuplicateUniqueKeyDaoException> findUniqueKeyException(ValidationException validationException){
        return findUniqueKeyException(validationException.getFailure());
    }


    public static Optional<DuplicateUniqueKeyDaoException> findUniqueKeyException(Throwable throwable){
        if(throwable instanceof ValidationException){
            return findUniqueKeyException((ValidationException) throwable);
        }
        else if(throwable instanceof DuplicateUniqueKeyDaoException){
            return Optional.of((DuplicateUniqueKeyDaoException)throwable);
        }
        return Optional.empty();
    }
}
