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

package com.dreameddeath.core.context.impl;

import com.dreameddeath.core.context.IExternalCallerContext;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Christophe Jeunesse on 12/09/2016.
 */
public class ExternalContextImpl implements IExternalCallerContext {
    private final String traceId;

    @JsonCreator
    public ExternalContextImpl(@JsonProperty("traceId") String traceId) {
        this.traceId = traceId;
    }

    public ExternalContextImpl(Builder builder){
        this(builder.getTraceId());
    }

    @Override @JsonProperty("traceId")
    public String getTraceId() {
        return null;
    }
}