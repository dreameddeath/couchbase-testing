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

import com.dreameddeath.core.couchbase.dcp.impl.AbstractDCPFlowHandler;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Created by Christophe Jeunesse on 02/06/2015.
 */
public class ElasticSearchSnapshotStorage {
    private String bucketName;
    private int partition;
    private Long sequence;

    public ElasticSearchSnapshotStorage(AbstractDCPFlowHandler.SnapshotMessage message){
        bucketName = message.getBucketName();
        partition = message.getPartition();
        sequence = message.getSequenceNumber();
    }

    public ElasticSearchSnapshotStorage(){}

    @JsonGetter("bucketName")
    public String getBucketName() {
        return bucketName;
    }

    @JsonSetter("bucketName")
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    @JsonGetter("partition")
    public int getPartition() {
        return partition;
    }

    @JsonSetter("partition")
    public void setPartition(int partition) {
        this.partition = partition;
    }

    @JsonGetter("sequence")
    public Long getSequence() {
        return sequence;
    }

    @JsonSetter("sequence")
    public void setSequence(Long sequence) {
        this.sequence= sequence;
    }

}
