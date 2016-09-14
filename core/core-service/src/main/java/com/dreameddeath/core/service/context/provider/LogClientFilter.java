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

package com.dreameddeath.core.service.context.provider;


import com.dreameddeath.core.service.http.HttpHeaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 12/01/2016.
 */
@Priority(1000)
public class LogClientFilter implements ClientRequestFilter,ClientResponseFilter {
    private final static Logger LOG= LoggerFactory.getLogger(LogClientFilter.class);

    @Override
    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        clientRequestContext.setProperty(FilterUtils.PROPERTY_START_TIME_NANO_PARAM_NAME,System.nanoTime());
    }

    @Override
    public void filter(ClientRequestContext clientRequestContext, ClientResponseContext clientResponseContext) throws IOException {
        Long startTime = (Long)clientRequestContext.getProperty(FilterUtils.PROPERTY_START_TIME_NANO_PARAM_NAME);
        double duration=0;
        if(startTime!=null){
            duration = (System.nanoTime()-duration)*1.0/1_000_000;
        }
        String traceId=clientResponseContext.getHeaderString(HttpHeaderUtils.HTTP_CALLEE_TRACE_ID);
        LOG.info("Response {} received in {} ms for callee trace id <{}>",clientResponseContext.getStatus(),duration,traceId);
    }
}
