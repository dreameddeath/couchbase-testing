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

package com.dreameddeath.core.elasticsearch.dcp;

import com.couchbase.client.core.message.dcp.MutationMessage;
import com.couchbase.client.core.message.dcp.RemoveMessage;
import com.couchbase.client.core.message.dcp.SnapshotMarkerMessage;
import com.dreameddeath.core.couchbase.dcp.exception.HandlerException;
import com.dreameddeath.core.couchbase.dcp.impl.AbstractDCPFlowHandler;
import com.dreameddeath.core.elasticsearch.ElasticSearchClient;
import com.dreameddeath.core.elasticsearch.IElasticSearchMapper;
import com.dreameddeath.core.elasticsearch.exception.JsonEncodingException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.mapper.IDocumentInfoMapper;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Christophe Jeunesse on 02/06/2015.
 */
public class ElasticSearchDcpFlowHandler extends AbstractDCPFlowHandler {
    public static final String DCP_FLOW_INDEX_NAME = "$$dcp$$";
    public static final String DCP_FLOW_TYPE_NAME = "snapshot";
    private ElasticSearchClient client;
    private boolean autoCreateIndexes;
    private final Set<String> checkedIndexes = new TreeSet<>();
    private IElasticSearchMapper mapper;

    public ElasticSearchDcpFlowHandler(ElasticSearchClient client,IElasticSearchMapper mapper,ITranscoder transcoder){
        this(client,mapper,transcoder,false);
    }

    public ElasticSearchDcpFlowHandler(ElasticSearchClient client,IElasticSearchMapper mapper,Map<String,ITranscoder> transcodersMap){
        this(client,mapper,transcodersMap,false);
    }

    public ElasticSearchDcpFlowHandler(ElasticSearchClient client,IElasticSearchMapper mapper,IDocumentInfoMapper docMapper){
        this(client,mapper,docMapper,false);
    }

    public ElasticSearchDcpFlowHandler(ElasticSearchClient client,IElasticSearchMapper mapper,ITranscoder transcoder,boolean autoCreateIndexes){
        super(transcoder);
        this.client = client;
        this.mapper = mapper;
        this.autoCreateIndexes = autoCreateIndexes;
    }

    public ElasticSearchDcpFlowHandler(ElasticSearchClient client,IElasticSearchMapper mapper,Map<String,ITranscoder> transcodersMap,boolean autoCreateIndexes){
        super(transcodersMap);
        this.client = client;
        this.mapper = mapper;
        this.autoCreateIndexes = autoCreateIndexes;
    }

    public ElasticSearchDcpFlowHandler(ElasticSearchClient client,IElasticSearchMapper mapper,IDocumentInfoMapper docMapper,boolean autoCreateIndexes){
        super(docMapper);
        this.client = client;
        this.mapper = mapper;
        this.autoCreateIndexes = autoCreateIndexes;
    }

    protected void createIndexIfNeeded(String indexName){
        if(autoCreateIndexes && !checkedIndexes.contains(indexName)){
            synchronized (checkedIndexes) {
                boolean result = client.isIndexExists(indexName);
                if (!result) {
                    client.createIndex(indexName);
                }
                checkedIndexes.add(indexName);
            }
        }
    }


    protected String snapshotIdBuilder(String bucketName,short partition){
        return bucketName + "/" + partition;
    }

    protected String documentIndexBuilder(String bucketName,String key){
        return mapper.documentIndexBuilder(bucketName,key);
    }

    protected String documentTypeBuilder(String bucketName,String key){
        return mapper.documentTypeBuilder(bucketName, key);
    }

    @Override
    public LastSnapshotReceived getLastSnapshot(String bucketName, short partition) {
        try {
            GetResponse response = client.getInternalClient().prepareGet(DCP_FLOW_INDEX_NAME, DCP_FLOW_INDEX_NAME, snapshotIdBuilder(bucketName, partition)).execute().get();
            if(response.isExists()){
                ElasticSearchSnapshotStorage storage = client.getObjectMapper().readValue(response.getSourceAsBytes(),ElasticSearchSnapshotStorage.class);
                return new LastSnapshotReceived(storage.getStartSequence(),storage.getEndSequence());
            }
        }
        catch(Exception e){
            //TODO improve errors management
        }
        return null;
    }

    @Override
    public void manageSnapshotMessage(SnapshotMarkerMessage message) {
        ElasticSearchSnapshotStorage doc = new ElasticSearchSnapshotStorage(message);
        try {
            byte[] serialized = client.getObjectMapper().writeValueAsBytes(doc);
            createIndexIfNeeded(DCP_FLOW_INDEX_NAME);
            UpdateRequest upsertRequest = new UpdateRequest(DCP_FLOW_INDEX_NAME, DCP_FLOW_TYPE_NAME, snapshotIdBuilder(message.bucket(),message.partition())).source(serialized).upsert(serialized);
            client.getInternalClient().update(upsertRequest).get();
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void manageMutationMessage(MutationMessage message, CouchbaseDocument mappedObject) {
        try {
            String indexName = documentIndexBuilder(message.bucket(), message.key());
            createIndexIfNeeded(indexName);
            UpdateResponse responseUpdate = client.upsert(
                    indexName,
                    documentTypeBuilder(message.bucket(), message.key()),
                    mappedObject
            ).toBlocking().single();
            responseUpdate.isCreated();
        }
        catch(JsonEncodingException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void manageDeletionMessage(RemoveMessage message) {
        client.delete(
                documentIndexBuilder(message.bucket(),message.key()),
                documentTypeBuilder(message.bucket(), message.key()),
                message.key()
        ).toBlocking().single();
    }

    @Override
    public void manageException(HandlerException message) {

    }
}
