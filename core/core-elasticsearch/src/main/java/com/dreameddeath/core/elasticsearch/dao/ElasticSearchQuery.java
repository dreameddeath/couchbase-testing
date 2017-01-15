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

package com.dreameddeath.core.elasticsearch.dao;

import com.dreameddeath.core.elasticsearch.search.ElasticSearchSearchQueryBuilder;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import io.reactivex.Single;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * Created by Christophe Jeunesse on 19/07/2015.
 */
public class ElasticSearchQuery<T extends CouchbaseDocument> {
    private final ElasticSearchDao<T> elasticSearchDao;
    private ElasticSearchSearchQueryBuilder query;

    ElasticSearchQuery(ElasticSearchDao<T> dao) {
        elasticSearchDao = dao;
        query = new ElasticSearchSearchQueryBuilder(elasticSearchDao.getClient());
        query.setIndices(elasticSearchDao.getMapper().documentIndexBuilder(elasticSearchDao.getBucketName(), elasticSearchDao.getTranscoder().getBaseClass()));
        query.setTypes(elasticSearchDao.getMapper().documentTypeBuilder(elasticSearchDao.getBucketName(), elasticSearchDao.getTranscoder().getBaseClass()));
    }

    public ElasticSearchQuery<T> setSearchType(SearchType searchType) {
        query.setSearchType(searchType);
        return this;
    }

    public ElasticSearchQuery<T> setQuery(QueryBuilder builder) {
        query.setQuery(builder);
        return this;
    }

    public ElasticSearchQuery<T> setQuery(String queryStr) {
        query.setQuery(QueryBuilders.queryStringQuery(queryStr));
        return this;
    }

    public ElasticSearchQuery<T> addFields(String... fields) {
        query.storedFields(fields);
        return this;
    }

    public ElasticSearchQuery<T> storedFields(String... fields) {
        query.storedFields(fields);
        return this;
    }

    public ElasticSearchQuery<T> addDocValueField(String... fields) {
        for(String field:fields) {
            query.addDocValueField(field);
        }
        return this;
    }


    public ElasticSearchQuery<T> setSize(int size) {
        query.setSize(size);
        return this;
    }


    public ElasticSearchQuery<T> setPostFilter(QueryBuilder builder) {
        query.setPostFilter(builder);
        return this;
    }

    public ElasticSearchQuery<T> setPostFilter(String postFilterStr) {
        query.setPostFilter(QueryBuilders.queryStringQuery(postFilterStr));
        return this;
    }

    public ElasticSearchQuery<T> setFetchSource(boolean activate) {
        query.setFetchSource(activate);
        return this;
    }

    public ElasticSearchResult<T> search() {
        return this.asyncSearch().blockingGet();
    }


    public Single<ElasticSearchResult<T>> asyncSearch() {
        return this.query.executeAsync().map(result -> new ElasticSearchResult<>(elasticSearchDao, result));
    }

}
