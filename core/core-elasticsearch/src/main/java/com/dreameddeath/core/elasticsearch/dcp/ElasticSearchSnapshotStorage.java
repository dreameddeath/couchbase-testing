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

import com.couchbase.client.core.message.dcp.SnapshotMarkerMessage;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Created by Christophe Jeunesse on 02/06/2015.
 */
public class ElasticSearchSnapshotStorage {
    private String bucketName;
    private int partition;
    private Long startSequence;
    private Long endSequence;

    public ElasticSearchSnapshotStorage(SnapshotMarkerMessage message){
        bucketName = message.bucket();
        partition = message.partition();
        startSequence = message.startSequenceNumber();
        endSequence = message.endSequenceNumber();
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

    @JsonGetter("startSeq")
    public Long getStartSequence() {
        return startSequence;
    }

    @JsonSetter("startSeq")
    public void setStartSequence(Long startSequence) {
        this.startSequence = startSequence;
    }

    @JsonGetter("endSeq")
    public Long getEndSequence() {
        return endSequence;
    }

    @JsonSetter("endSeq")
    public void setEndSequence(Long endSequence) {
        this.endSequence = endSequence;
    }
}
