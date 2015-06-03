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
import com.couchbase.client.deps.com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Created by Christophe Jeunesse on 02/06/2015.
 */
public class ElasticSearchSnapshotStorage {
    private String _bucketName;
    private int _partition;
    private Long _startSequence;
    private Long _endSequence;

    public ElasticSearchSnapshotStorage(SnapshotMarkerMessage message){
        _bucketName = message.bucket();
        _partition = message.partition();
        _startSequence = message.startSequenceNumber();
        _endSequence = message.endSequenceNumber();
    }

    public ElasticSearchSnapshotStorage(){}

    @JsonGetter("bucketName")
    public String getBucketName() {
        return _bucketName;
    }

    @JsonSetter("bucketName")
    public void setBucketName(String bucketName) {
        this._bucketName = bucketName;
    }

    @JsonGetter("partition")
    public int getPartition() {
        return _partition;
    }

    @JsonSetter("partition")
    public void setPartition(int partition) {
        this._partition = partition;
    }

    @JsonGetter("startSeq")
    public Long getStartSequence() {
        return _startSequence;
    }

    @JsonSetter("startSeq")
    public void setStartSequence(Long startSequence) {
        this._startSequence = startSequence;
    }

    @JsonGetter("endSeq")
    public Long getEndSequence() {
        return _endSequence;
    }

    @JsonSetter("endSeq")
    public void setEndSequence(Long endSequence) {
        this._endSequence = endSequence;
    }
}
