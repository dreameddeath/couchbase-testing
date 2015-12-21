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

package com.dreameddeath.infrastructure.plugin.couchbase.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by Christophe Jeunesse on 20/12/2015.
 */
public class CouchbaseMetrics {
    @JsonProperty("runtime")
    private Map<String,Object> runtime=null;
    @JsonProperty("latency")
    private Map<String,Object> latency=null;

    public Map<String, Object> getRuntime() {
        return runtime;
    }

    public void setRuntime(Map<String, Object> runtime) {
        this.runtime = runtime;
    }

    public Map<String, Object> getLatency() {
        return latency;
    }

    public void setLatency(Map<String, Object> latency) {
        this.latency = latency;
    }
}
