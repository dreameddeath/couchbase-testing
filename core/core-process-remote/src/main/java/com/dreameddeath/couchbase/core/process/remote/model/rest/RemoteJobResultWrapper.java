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

package com.dreameddeath.couchbase.core.process.remote.model.rest;

import com.fasterxml.jackson.annotation.*;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 26/02/2016.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility= JsonAutoDetect.Visibility.NONE,fieldVisibility= JsonAutoDetect.Visibility.NONE,isGetterVisibility = JsonAutoDetect.Visibility.NONE,setterVisibility = JsonAutoDetect.Visibility.NONE,creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class RemoteJobResultWrapper<T> {
    private T result;
    private StateInfo jobStateInfo;
    private UUID jobId;


    @JsonCreator
    public RemoteJobResultWrapper(T result){
        this.result = result;
    }

    protected RemoteJobResultWrapper(){}

    @JsonValue
    public T getResult(){return result;}

    public void setJodId(UUID jobId){
        this.jobId = jobId;
    }

    @JsonIgnore
    public UUID getJobId(){
        return jobId;
    }

    @JsonIgnore
    public StateInfo getJobStateInfo() {
        return jobStateInfo;
    }

    public void setJobStateInfo(StateInfo jobStateInfo) {
        this.jobStateInfo = jobStateInfo;
    }
}
