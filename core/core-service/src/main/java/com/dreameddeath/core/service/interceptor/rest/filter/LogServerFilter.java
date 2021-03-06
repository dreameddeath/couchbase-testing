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

package com.dreameddeath.core.service.interceptor.rest.filter;

import com.dreameddeath.core.context.IGlobalContext;
import com.dreameddeath.core.log.MDCUtils;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.http.HttpHeaderUtils;
import com.dreameddeath.core.service.interceptor.PropertyUtils;
import com.dreameddeath.core.user.IUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 14/09/2016.
 */
@Priority(3)
public class LogServerFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOG = LoggerFactory.getLogger(LogServerFilter.class);

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        containerRequestContext.setProperty(PropertyUtils.PROPERTY_MDC_CONTEXT, MDCUtils.getMdcContext());

        IGlobalContext requestContext = (IGlobalContext)containerRequestContext.getProperty(PropertyUtils.PROPERTY_GLOBAL_CONTEXT_PARAM_NAME);
        String callerTraceId=null;
        if(requestContext!=null) {
            MDCUtils.setTraceId(requestContext.currentTraceId());
            MDCUtils.setGlobalTraceId(requestContext.globalTraceId());
            if(requestContext.callerCtxt()!=null) {
                callerTraceId=requestContext.callerCtxt().traceId();
            }
        }

        IUser foundUser = (IUser)containerRequestContext.getProperty(IServiceClient.USER_PROPERTY);
        if(foundUser!=null) {
            MDCUtils.setUserId(foundUser.getUserId());
        }

        LOG.info("Processing request from caller trace id <{}> with request <{}> <{}>", callerTraceId,containerRequestContext.getMethod(),containerRequestContext.getUriInfo().getRequestUri().toString());
        containerRequestContext.setProperty(PropertyUtils.PROPERTY_START_TIME_NANO_PARAM_NAME,System.nanoTime());
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        Long startTime = (Long)containerRequestContext.getProperty(PropertyUtils.PROPERTY_START_TIME_NANO_PARAM_NAME);
        if(startTime!=null){
            double duration=(System.nanoTime()-startTime)*1.0/1_000_000;
            LOG.info("Processing Duration <{}> ms",duration);
            containerResponseContext.getHeaders().put(HttpHeaderUtils.HTTP_CALLEE_DURATION, Collections.singletonList(duration+""));
        }
        MDCUtils.setContextMap((Map<String,String>)containerRequestContext.getProperty(PropertyUtils.PROPERTY_MDC_CONTEXT));
    }
}
