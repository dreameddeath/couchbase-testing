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

package com.dreameddeath.core.elasticsearch;


import com.dreameddeath.core.elasticsearch.exception.JsonEncodingException;
import com.dreameddeath.core.elasticsearch.search.ElasticSearchSearchQueryBuilder;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import rx.AsyncEmitter;
import rx.Observable;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 25/05/2015.
 */
public class ElasticSearchClient implements Closeable {
    private final Node node;
    private final Client client;
    private final boolean hasClientOwnerShip;
    private final ObjectMapper objectMapper;


    public ElasticSearchClient(Client client,ObjectMapper mapper,boolean hasClientOwnerShip){
        this.node=null;
        this.client = client;
        objectMapper = mapper;
        this.hasClientOwnerShip=hasClientOwnerShip;
    }

    public ElasticSearchClient(Node node, ObjectMapper mapper){
        this.node = node;
        this.client = node.client();
        objectMapper = mapper;
        this.hasClientOwnerShip=true;
    }


    public boolean isIndexExists(String indexName){
        IndicesExistsResponse response = getInternalClient().admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet();
        return response.isExists();
    }

    public void createIndex(String indexName){
        synchronized (this) {
            getInternalClient().admin().indices().prepareCreate(indexName).execute().actionGet();
            //TODO check and manage error
            getInternalClient().admin().cluster().prepareHealth(indexName).setWaitForActiveShards(1).execute().actionGet();
        }
    }

    public void syncIndexes(){
        synchronized (this) {
            getInternalClient().admin().indices().prepareRefresh().execute().actionGet();
            //TODO check and manage error
        }
    }



    public ElasticSearchClient(String clusterName,ObjectMapper mapper){
        this(NodeBuilder.nodeBuilder().client(true).clusterName(clusterName).build(),mapper);
    }

    public Observable<GetResponse> get(String indexName,String type,String key){
        ListenableActionFuture<GetResponse> asyncRes = client.prepareGet(indexName, type, key).execute();
        return fromListenableFuture(asyncRes);//TODO manage common errors
    }


    public Observable<IndexResponse> create(String indexName,String type,CouchbaseDocument doc) throws JsonEncodingException{
        try {
            byte[] encodedStr = objectMapper.writeValueAsBytes(doc);
            ListenableActionFuture<IndexResponse> asyncRes = client.prepareIndex(indexName, type, doc.getBaseMeta().getKey())
                    .setSource(encodedStr)
                    //.setVersion(doc.getBaseMeta().getCas()).setVersionType(VersionType.EXTERNAL)
                    .execute();
            return fromListenableFuture(asyncRes);//TODO manage common errors
        }
        catch(JsonProcessingException e){
            throw new JsonEncodingException(e);
        }
    }


    public Observable<UpdateResponse> update(String indexName,String type,CouchbaseDocument doc) throws JsonEncodingException{
        try {
            byte[] encodedStr = objectMapper.writeValueAsBytes(doc);
            ListenableActionFuture<UpdateResponse> asyncRes = client.prepareUpdate(indexName, type, doc.getBaseMeta().getKey())
                    .setDoc(encodedStr)
                    .setVersion(doc.getBaseMeta().getCas()).setVersionType(VersionType.EXTERNAL)
                    .execute();
            return fromListenableFuture(asyncRes);//TODO manage common errors
        }
        catch(JsonProcessingException e){
            throw new JsonEncodingException(e);
        }
    }

    public Observable<UpdateResponse> upsert(String indexName,String type,CouchbaseDocument doc) throws JsonEncodingException{
        try {
            byte[] encodedStr = objectMapper.writeValueAsBytes(doc);
            ListenableActionFuture<UpdateResponse> asyncRes = client.prepareUpdate(indexName, type, doc.getBaseMeta().getKey())
                    .setDoc(encodedStr)
                    //.setVersion(doc.getBaseMeta().getCas()).setVersionType(VersionType.EXTERNAL)
                    .setUpsert(
                            new IndexRequest(indexName, type, doc.getBaseMeta().getKey())
                                    .source(encodedStr)
                            //.version(doc.getBaseMeta().getCas()).versionType(VersionType.EXTERNAL)
                    )
                    .execute();
            return fromListenableFuture(asyncRes);
        }
        catch(JsonProcessingException e){
            throw new JsonEncodingException(e);
        }
    }


    public Observable<DeleteResponse> delete(String indexName,String type,String key){
        ListenableActionFuture<DeleteResponse> asyncRes = client.prepareDelete(indexName, type, key).execute();
        return fromListenableFuture(asyncRes);//TODO manage common errors
    }


    public ElasticSearchSearchQueryBuilder newSearchQuery(){
        return new ElasticSearchSearchQueryBuilder(this);
    }

    public Observable<SearchResponse> search(ElasticSearchSearchQueryBuilder builder){
        return builder.executeAsObservable();
    }

    public Observable<SearchResponse> search(String[] indexes,String[] types,String query){
        ListenableActionFuture<SearchResponse> asyncRes = client.prepareSearch(indexes).setTypes(types).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(query).execute();
        return fromListenableFuture(asyncRes);//TODO manage common errors
    }

    public static <T> Observable<T> fromListenableFuture(ListenableActionFuture<T> listenableActionFuture){
        return Observable.fromAsync(emitter->{
                    ActionListener<T> callback = new ActionListener<T>(){
                        @Override
                        public void onResponse(T t) {
                            emitter.onNext(t);
                            emitter.onCompleted();
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            emitter.onError(throwable);
                        }
                    };
                    listenableActionFuture.addListener(callback);
                },
                AsyncEmitter.BackpressureMode.ERROR
        );
    }

    public ObjectMapper getObjectMapper(){
        return objectMapper;
    }

    public Client getInternalClient() {
        return client;
    }

    @Override
    public void close() throws IOException {
        if(node!=null){
            node.close();
        }
        if(client!=null && hasClientOwnerShip){
            client.close();
        }
    }
}
