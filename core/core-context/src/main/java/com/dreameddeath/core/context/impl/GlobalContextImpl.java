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

import com.dreameddeath.core.context.ICallerContext;
import com.dreameddeath.core.context.IExternalCallerContext;
import com.dreameddeath.core.context.IGlobalContext;
import com.dreameddeath.core.context.IUserContext;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Christophe Jeunesse on 12/09/2016.
 */
public class GlobalContextImpl implements IGlobalContext{
    private final transient String currentTraceId;
    private final String globalTraceId;
    private final ICallerContext callerContext;
    private final IExternalCallerContext externalCallerContext;
    private final IUserContext userContext;


    @JsonCreator
    public GlobalContextImpl(@JsonProperty("globalTraceId")String globalTraceId,@JsonProperty("caller") CallerContextImpl callerContext, @JsonProperty("extern") ExternalContextImpl externalCallerContext, @JsonProperty("user") UserContextImpl userContext) {
        this(GlobalContextFactoryImpl.buildDefaultTraceId(),globalTraceId,callerContext,externalCallerContext,userContext);
    }

    public GlobalContextImpl(String traceId,String globalTraceId,ICallerContext callerContext, IExternalCallerContext externalCallerContext, IUserContext userContext) {
        this.currentTraceId =traceId;
        this.globalTraceId=globalTraceId!=null?globalTraceId:traceId;
        this.callerContext = callerContext;
        this.externalCallerContext = externalCallerContext;
        this.userContext = userContext;
    }
    
    @Override @JsonIgnore
    public String currentTraceId() {
        return currentTraceId;
    }

    @Override @JsonProperty("globalTraceId")
    public String globalTraceId() {
        return globalTraceId;
    }

    @Override @JsonProperty("caller")
    public ICallerContext callerCtxt() {
        return callerContext;
    }

    @Override @JsonProperty("extern")
    public IExternalCallerContext externalCtxt() {
        return externalCallerContext;
    }

    @Override @JsonProperty("user")
    public IUserContext userCtxt() {
        return userContext;
    }


}
