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

package com.dreameddeath.core.query.service;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.query.model.v1.QuerySearch;
import com.dreameddeath.core.user.IUser;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 19/12/2016.
 */
public interface IQueryService<T> {
    Single<T> asyncGet(String key, ICouchbaseSession session);
    Observable<T> asyncSearch(QuerySearch search, ICouchbaseSession session);
    Single<T> asyncGet(String key,IUser user);
    Observable<T> asyncSearch(QuerySearch search, IUser user);

}
