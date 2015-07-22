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
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import org.elasticsearch.action.get.GetResponse;
import rx.Observable;


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

    public static <T extends CouchbaseDocument> Builder<T> builder(){
        return new Builder<>();
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

    public ElasticSearchQuery<T> newQuery(){
        return new ElasticSearchQuery<>(this);
    }

    public ElasticSearchResult<T> search(ElasticSearchQuery<T> query){
        return query.search();
    }


    public Observable<ElasticSearchResult<T>> asyncSearch(ElasticSearchQuery<T> query){
        return query.asyncSearch();
    }


    public static class Builder<T extends CouchbaseDocument>{
        private String _bucketName;
        private ElasticSearchClient _client;
        private IElasticSearchMapper _mapper;
        private ITranscoder<T> _transcoder;

        public Builder withBucketName(String name){
            _bucketName = name;
            return this;
        }

        public Builder withClient(ElasticSearchClient client){
            _client = client;
            return this;
        }

        public Builder withMapper(IElasticSearchMapper mapper){
            _mapper = mapper;
            return this;
        }

        public Builder withTranscoder(ITranscoder<T> transcoder){
            _transcoder = transcoder;
            return this;
        }

        public ElasticSearchDao<T> build(){
            return new ElasticSearchDao<>(this._bucketName,this._client,this._mapper,this._transcoder);
        }
    }

    protected IElasticSearchMapper getMapper() {
        return _mapper;
    }

    protected ElasticSearchClient getClient() {
        return _client;
    }

    protected ITranscoder<T> getTranscoder() {
        return _transcoder;
    }

    protected String getBucketName() {
        return _bucketName;
    }
}
