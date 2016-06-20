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

package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.utils.KeyPattern;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 17/05/2015.
 */
public interface IDaoWithKeyPattern<T> {
    KeyPattern getKeyPattern();
    IBlockingDaoWithKeyPattern<T> toBlocking();
    String getKeyFromParams(Object ...params);
    Observable<T> asyncGetFromKeyParams(ICouchbaseSession session, Object ...params);

    interface IBlockingDaoWithKeyPattern<T>{
        T getFromKeyParams(ICouchbaseSession session, Object ...params) throws DaoException,StorageException;
    }
}
