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

package com.dreameddeath.core.elasticsearch.dcp;

import com.couchbase.client.core.message.dcp.MutationMessage;
import com.couchbase.client.core.message.dcp.RemoveMessage;
import com.dreameddeath.core.couchbase.dcp.exception.HandlerException;
import com.dreameddeath.core.couchbase.dcp.impl.AbstractDCPFlowHandler;
import com.dreameddeath.core.elasticsearch.ElasticSearchClient;
import com.dreameddeath.core.elasticsearch.IElasticSearchMapper;
import com.dreameddeath.core.elasticsearch.exception.JsonEncodingException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.google.common.base.Preconditions;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Christophe Jeunesse on 02/06/2015.
 */
public class ElasticSearchDcpFlowHandler extends AbstractDCPFlowHandler {
    private final static Logger LOG = LoggerFactory.getLogger(ElasticSearchDcpFlowHandler.class);
    public static final String DCP_FLOW_INDEX_NAME = "$$dcp$$";
    public static final String DCP_FLOW_TYPE_NAME = "snapshot";
    private ElasticSearchClient client;
    private boolean autoCreateIndexes;
    private final Set<String> checkedIndexes = new TreeSet<>();
    private IElasticSearchMapper mapper;

    public static Builder builder(){
        return new Builder();
    }

    public ElasticSearchDcpFlowHandler(Builder builder){
        super(builder);
        Preconditions.checkNotNull(builder.client,"A client must be given");
        Preconditions.checkNotNull(builder.mapper,"A mapper must be given");
        this.client = builder.client;
        this.autoCreateIndexes = builder.autoCreateIndex;
        this.mapper = builder.mapper;
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
                return new LastSnapshotReceived(storage.getSequence());
            }
        }
        catch(Throwable e){
            LOG.error("Error during Last Snaphot reading ",e);
            //TODO improve errors management
        }
        return null;
    }

    @Override
    public void manageSnapshotMessage(SnapshotMessage message) {
        ElasticSearchSnapshotStorage doc = new ElasticSearchSnapshotStorage(message);
        try {
            byte[] serialized = client.getObjectMapper().writeValueAsBytes(doc);
            createIndexIfNeeded(DCP_FLOW_INDEX_NAME);
            UpdateRequest upsertRequest = new UpdateRequest(DCP_FLOW_INDEX_NAME, DCP_FLOW_TYPE_NAME, snapshotIdBuilder(message.getBucketName(),message.getPartition())).doc(serialized).upsert(serialized);
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
            client.upsert(
                    indexName,
                    documentTypeBuilder(message.bucket(), message.key()),
                    mappedObject
            ).blockingGet();
            //TODO see management of errors
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
        ).blockingGet();
    }

    @Override
    public void manageException(HandlerException message) {

    }

    public static class Builder extends AbstractDCPFlowHandler.Builder<Builder>{
        private ElasticSearchClient client;
        private IElasticSearchMapper mapper;
        private boolean autoCreateIndex = false;

        public Builder withClient(ElasticSearchClient client) {
            this.client = client;
            return this;
        }

        public Builder withMapper(IElasticSearchMapper mapper) {
            this.mapper = mapper;
            return this;
        }

        public Builder withAutoCreateIndex(boolean autoCreateIndex) {
            this.autoCreateIndex = autoCreateIndex;
            return this;
        }

        public ElasticSearchDcpFlowHandler build(){
            return new ElasticSearchDcpFlowHandler(this);
        }
    }
}
