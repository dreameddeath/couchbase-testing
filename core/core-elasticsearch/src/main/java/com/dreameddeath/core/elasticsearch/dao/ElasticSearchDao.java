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

import com.dreameddeath.core.elasticsearch.ElasticSearchClient;
import com.dreameddeath.core.elasticsearch.IElasticSearchMapper;
import com.dreameddeath.core.elasticsearch.exception.ElasticSearchDaoException;
import com.dreameddeath.core.elasticsearch.search.ElasticSearchSearchQueryBuilder;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Christophe Jeunesse on 26/05/2015.
 */
public class ElasticSearchDao<T extends CouchbaseDocument> {
    private String  _bucketName;
    private ElasticSearchClient _client;
    private IElasticSearchMapper _mapper;
    private ITranscoder<T> _transcoder;

    public ElasticSearchDao(String bucketName,ElasticSearchClient client,IElasticSearchMapper mapper,ITranscoder<T> transcoder){
        _client = client;
        _mapper = mapper;
        _bucketName = bucketName;
        _transcoder = transcoder;
    }

    protected T decode(GetResponse response)throws ElasticSearchDaoException{
        if(response.isExists()){
            T foundDoc = _transcoder.decode(response.getSourceAsBytes());
            foundDoc.getBaseMeta().setKey(response.getId());
            return foundDoc;
        }
        else{
            throw new ElasticSearchDaoException("The document <"+response.getId()+"> hasn't been found in "+response.getIndex()+"/"+response.getType());
        }
    }

    public T get(String key){
        return asyncGet(key).toBlocking().single();
    }

    public Observable<T> asyncGet(String key){
        Observable<GetResponse> responseObservable = _client.get(_mapper.documentIndexBuilder(_bucketName, key), _mapper.documentTypeBuilder(_bucketName, key), key);
        return responseObservable.map(this::decode);
    }

    public ElasticSearchQuery newQuery(){
        return new ElasticSearchQuery();
    }

    public ElasticSearchResult search(ElasticSearchQuery query){
        return asyncSearch(query).toBlocking().single();
    }


    public Observable<ElasticSearchResult> asyncSearch(ElasticSearchQuery query){
        return query._query.executeAsObservable().map(ElasticSearchResult::new);
    }

    public class ElasticSearchResult{
        private SearchResponse _esResult;
        private List<ElasticSearchResultHit> _elasticSearchResultHitList =null;
        public ElasticSearchResult(SearchResponse esResult){
            _esResult = esResult;
        }

        public long getTotalHitCount(){
            return _esResult.getHits().getTotalHits();
        }

        public List<ElasticSearchResultHit> getList(){
            if(_elasticSearchResultHitList ==null){
                synchronized (this){
                    if(_elasticSearchResultHitList ==null) {
                        _elasticSearchResultHitList = new ArrayList<>(_esResult.getHits().hits().length);
                        for (SearchHit hit : _esResult.getHits().hits()) {
                            _elasticSearchResultHitList.add(new ElasticSearchResultHit(hit));
                        }
                    }
                }
            }
            return _elasticSearchResultHitList;
        }
    }

    public class ElasticSearchResultHit {
        private SearchHit _hit;
        private boolean _mappingAttempted;
        private T _obj;

        public ElasticSearchResultHit(SearchHit hit){
            _hit = hit;
            _mappingAttempted = false;
        }

        public float getScore(){
            return _hit.score();
        }

        public String getKey(){
            return _hit.getId();
        }

        public T get(){
            if(!_mappingAttempted){
                synchronized (this){
                    if(!_mappingAttempted) {
                        if (!_hit.isSourceEmpty()){
                            _obj = _transcoder.decode(_hit.source());
                        }
                        _mappingAttempted=true;
                    }
                }
            }
            return _obj;
        }
    }

    public class ElasticSearchQuery{
        private ElasticSearchSearchQueryBuilder _query;

        public ElasticSearchQuery(){
            _query = new ElasticSearchSearchQueryBuilder(_client);
            _query.setIndices(_mapper.documentIndexBuilder(_bucketName, _transcoder.getBaseClass()));
            _query.setTypes(_mapper.documentTypeBuilder(_bucketName, _transcoder.getBaseClass()));
        }

        public ElasticSearchQuery setSearchType(SearchType searchType) {
            _query.setSearchType(searchType);
            return this;
        }

        public ElasticSearchQuery setQuery(QueryBuilder builder) {
            _query.setQuery(builder);
            return this;
        }

        public ElasticSearchQuery setQuery(String queryStr) {
            _query.setQuery(queryStr);
            return this;
        }

        public ElasticSearchQuery addFields(String... fields) {
            _query.addFields(fields);
            return this;
        }

        public ElasticSearchQuery setSize(int size) {
            _query.setSize(size);
            return this;
        }


        public ElasticSearchQuery setPostFilter(FilterBuilder builder) {
            _query.setPostFilter(builder);
            return this;
        }

        public ElasticSearchQuery setPostFilter(String postFilterStr) {
            _query.setPostFilter(postFilterStr);
            return this;
        }

        public ElasticSearchQuery setFetchSource(boolean activate){
            _query.setFetchSource(activate);
            return this;
        }

    }
}
