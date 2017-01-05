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

package com.dreameddeath.core.query.service;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.query.model.v1.QuerySearch;
import com.dreameddeath.core.user.IUser;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 29/12/2016.
 */
public abstract class AbstractStandardQueryService<TDOC extends CouchbaseDocument,T> implements IQueryService<T>{

    private ICouchbaseSessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(ICouchbaseSessionFactory factory){
        this.sessionFactory = factory;
    }

    @Override
    public Observable<T> asyncGet(String key, ICouchbaseSession session) {
        return session.<TDOC>asyncGet(key).map(this::mapToPublic);
    }

    @Override
    public Observable<T> asyncSearch(QuerySearch search, ICouchbaseSession session) {
        return null;
    }

    @Override
    public Observable<T> asyncGet(String key, IUser user) {
        return asyncGet(key,sessionFactory.newSession(ICouchbaseSession.SessionType.READ_ONLY,user));
    }

    @Override
    public Observable<T> asyncSearch(QuerySearch search, IUser user) {
        return asyncSearch(search,sessionFactory.newSession(ICouchbaseSession.SessionType.READ_ONLY,user));
    }

    protected abstract T mapToPublic(TDOC doc);
}
