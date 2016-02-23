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

import com.dreameddeath.couchbase.core.process.remote.model.TestDocJobUpdate;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Christophe Jeunesse on 15/01/2016.
 */
public class TestDocJobUpdateResult {
    @JsonProperty
    public Integer resultIncrValue;
    @JsonProperty
    public StateInfo processState;


    public TestDocJobUpdateResult(){}

    public TestDocJobUpdateResult(TestDocJobUpdate job){
        resultIncrValue = job.resultIncrValue;
        processState= new StateInfo(job.getStateInfo());
    }
}
