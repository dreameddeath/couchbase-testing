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

package com.dreameddeath.core.session.impl;

import com.dreameddeath.core.date.IDateTimeService;
import com.dreameddeath.core.elasticsearch.IElasticSearchSession;
import com.dreameddeath.core.elasticsearch.dao.ElasticSearchQuery;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;
import com.dreameddeath.core.user.IUser;

/**
 * Created by Christophe Jeunesse on 09/07/2015.
 */
public class ElasticSearchSession implements IElasticSearchSession{
    final private ElasticSearchSessionFactory sessionFactory;
    final private IDateTimeService dateTimeService;
    final private IUser user;

    public ElasticSearchSession(ElasticSearchSessionFactory factory, IUser user){
        sessionFactory = factory;
        this.user = user;
        dateTimeService = sessionFactory.getDateTimeServiceFactory().getService();
    }

    @Override
    public <T extends CouchbaseDocument> ElasticSearchQuery<T> newElasticSearchQuery(Class<T> targetClass) throws MappingNotFoundException{
        return sessionFactory.getElasticSearchDaoFactory().getDaoForClass(targetClass).newQuery();
    }
}
