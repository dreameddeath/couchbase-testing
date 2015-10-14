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

package com.dreameddeath.core.elasticsearch.dao;

import com.dreameddeath.core.elasticsearch.search.ElasticSearchSearchQueryBuilder;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import rx.Observable;

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
        query.setQuery(queryStr);
        return this;
    }

    public ElasticSearchQuery<T> addFields(String... fields) {
        query.addFields(fields);
        return this;
    }

    public ElasticSearchQuery<T> setSize(int size) {
        query.setSize(size);
        return this;
    }


    public ElasticSearchQuery<T> setPostFilter(FilterBuilder builder) {
        query.setPostFilter(builder);
        return this;
    }

    public ElasticSearchQuery<T> setPostFilter(String postFilterStr) {
        query.setPostFilter(postFilterStr);
        return this;
    }

    public ElasticSearchQuery<T> setFetchSource(boolean activate) {
        query.setFetchSource(activate);
        return this;
    }

    public ElasticSearchResult<T> search() {
        return this.asyncSearch().toBlocking().single();
    }


    public Observable<ElasticSearchResult<T>> asyncSearch() {
        return this.query.executeAsObservable().map(result -> new ElasticSearchResult<>(elasticSearchDao, result));
    }

}
