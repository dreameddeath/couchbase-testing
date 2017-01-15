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

package com.dreameddeath.core.elasticsearch;


import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFoundException;
import com.dreameddeath.core.elasticsearch.config.ElasticSearchConfigProperties;
import com.dreameddeath.core.elasticsearch.exception.JsonEncodingException;
import com.dreameddeath.core.elasticsearch.search.ElasticSearchSearchQueryBuilder;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import io.reactivex.Single;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 25/05/2015.
 */
public class ElasticSearchClient implements Closeable {
    private final Client client;
    private final boolean hasClientOwnerShip;
    private final ObjectMapper objectMapper;

    public ElasticSearchClient(Client client,ObjectMapper mapper,boolean hasClientOwnerShip){
        this.client = client;
        this.objectMapper = mapper;
        this.hasClientOwnerShip=hasClientOwnerShip;
    }

    public ElasticSearchClient(String clusterName,ObjectMapper mapper) throws ConfigPropertyValueNotFoundException{
        this(buildClient(clusterName),mapper,true);
    }

    /*public ElasticSearchClient(Client client, ObjectMapper mapper){
        this(client,mapper,false);
    }*/


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

    private static Client buildClient(String clusterName) throws ConfigPropertyValueNotFoundException{
        List<String> connectionParams = ElasticSearchConfigProperties.ELASTICSEARCH_CLUSTER_ADDRESSES.getProperty(clusterName).getMandatoryValue("No connection addresses found for cluster <{}>",clusterName);
        Long defaultPort = ElasticSearchConfigProperties.ELASTICSEARCH_DEFAULT_PORT.get();

        Settings settings=Settings.builder().put("cluster.name", clusterName).build();
        TransportClient client = new PreBuiltTransportClient(settings);
        for(String connectionParam:connectionParams) {
            String[] connectionParts = connectionParam.split(":");
            Preconditions.checkArgument(connectionParts.length==1 || connectionParts.length==2,"The cluster %s has wrong formatted address <%s>",clusterName,connectionParam);
            String host = connectionParts[0];
            Long port = defaultPort;
            if(connectionParts.length>1){
                try {
                    port = Long.parseLong(connectionParts[1]);
                }
                catch(NumberFormatException e){
                    throw new IllegalArgumentException("The cluster <"+clusterName+"> has a wrong address format with port <"+connectionParam+">");
                }
            }
            Preconditions.checkArgument(port>0 ,"The cluster %s hasn't a well defined port %s",clusterName,port);
            Preconditions.checkArgument(StringUtils.isNotEmpty(host) ,"The cluster %s hasn't a well defined host %s",clusterName,host);

            try {
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port.intValue()));
            }
            catch(UnknownHostException e){
                throw new IllegalArgumentException("The cluster <"+clusterName+"> has a wrong address format with port <"+connectionParam+">");
            }
        }
        return client;
    }

    public Single<GetResponse> get(String indexName,String type,String key){
        ListenableActionFuture<GetResponse> asyncRes = client
                .prepareGet(indexName, type, key)
                .execute();
        return fromListenableFuture(asyncRes);//TODO manage common errors
    }


    public Single<IndexResponse> create(String indexName,String type,CouchbaseDocument doc) throws JsonEncodingException{
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


    public Single<UpdateResponse> update(String indexName,String type,CouchbaseDocument doc) throws JsonEncodingException{
        try {
            byte[] encodedStr = objectMapper.writeValueAsBytes(doc);
            ListenableActionFuture<UpdateResponse> asyncRes = client.prepareUpdate(indexName, type, doc.getBaseMeta().getKey())
                    .setDoc(encodedStr)
                    //.setVersion(doc.getBaseMeta().getCas()).setVersionType(VersionType.EXTERNAL)
                    .execute();
            return fromListenableFuture(asyncRes);//TODO manage common errors
        }
        catch(JsonProcessingException e){
            throw new JsonEncodingException(e);
        }
    }

    public Single<UpdateResponse> upsert(String indexName, String type, CouchbaseDocument doc) throws JsonEncodingException{
        try {
            byte[] encodedStr = objectMapper.writeValueAsBytes(doc);
            ListenableActionFuture<UpdateResponse> asyncRes = client.prepareUpdate(indexName, type, doc.getBaseMeta().getKey())
                    .setDoc(encodedStr)
                    //.setVersion(doc.getBaseMeta().getCas())
                    //.setVersionType(VersionType.EXTERNAL)
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


    public Single<DeleteResponse> delete(String indexName,String type,String key){
        ListenableActionFuture<DeleteResponse> asyncRes = client
                .prepareDelete(indexName, type, key)
                .execute();
        return fromListenableFuture(asyncRes);//TODO manage common errors
    }


    public ElasticSearchSearchQueryBuilder newSearchQuery(){
        return new ElasticSearchSearchQueryBuilder(this);
    }

    public static <T> Single<T> fromListenableFuture(ListenableActionFuture<T> listenableActionFuture){
        return Single.fromPublisher(emitter->{
                    ActionListener<T> callback = new ActionListener<T>(){
                        @Override
                        public void onResponse(T t) {
                            emitter.onNext(t);
                            emitter.onComplete();
                        }

                        @Override
                        public void onFailure(Exception throwable) {
                            emitter.onError(throwable);
                        }
                    };
                    listenableActionFuture.addListener(callback);
                }
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
        if(client!=null && hasClientOwnerShip){
            client.close();
        }
    }
}
