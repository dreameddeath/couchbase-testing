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

import com.dreameddeath.core.date.DateTimeServiceFactory;
import com.dreameddeath.core.elasticsearch.dao.ElasticSearchDaoFactory;
import com.dreameddeath.core.model.mapper.IDocumentInfoMapper;
import com.dreameddeath.core.user.IUser;

/**
 * Created by Christophe Jeunesse on 09/07/2015.
 */
public class ElasticSearchSessionFactory {
    private final ElasticSearchDaoFactory elasticSearchDaoFactory;
    private final DateTimeServiceFactory dateTimeServiceFactory;


    public ElasticSearchSessionFactory(Builder builder){
        elasticSearchDaoFactory = builder.daoFactoryBuilder.build();
        dateTimeServiceFactory = builder.dateTimeServiceFactory;
    }

    public DateTimeServiceFactory getDateTimeServiceFactory() {
        return dateTimeServiceFactory;
    }

    public ElasticSearchDaoFactory getElasticSearchDaoFactory() {
        return elasticSearchDaoFactory;
    }

    public ElasticSearchSession newSession(IUser user){
        return new ElasticSearchSession(this,user);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private ElasticSearchDaoFactory.Builder daoFactoryBuilder;
        private DateTimeServiceFactory dateTimeServiceFactory;

        public Builder(){
            daoFactoryBuilder=ElasticSearchDaoFactory.builder();
            dateTimeServiceFactory = new DateTimeServiceFactory();
        }


        public Builder withDocumentInfoMappper(IDocumentInfoMapper mapper){
            daoFactoryBuilder.withDocumentInfoMappper(mapper);
            return this;
        }

        public ElasticSearchSessionFactory build(){
            return new ElasticSearchSessionFactory(this);
        }
    }
}
