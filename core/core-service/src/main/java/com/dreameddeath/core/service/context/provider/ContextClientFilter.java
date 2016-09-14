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


import com.dreameddeath.core.context.*;
import com.dreameddeath.core.log.MDCUtils;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.http.HttpHeaderUtils;
import com.dreameddeath.core.user.IUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 12/01/2016.
 */
@Priority(2)
public class ContextClientFilter implements ClientRequestFilter,ClientResponseFilter {
    private final static Logger LOG= LoggerFactory.getLogger(ContextClientFilter.class);
    private IContextFactory factory;

    @Autowired
    public void setGlobalContextFactory(IContextFactory factory){
        this.factory = factory;
    }

    @Override
    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        IGlobalContext.Builder requestContextBuilder= IGlobalContext.builder();
        ICallerContext.Builder callerContextBuilder= ICallerContext.builder();
        IExternalCallerContext.Builder externalContextBuilder=null;
        IUserContext.Builder userContextBuilder=null;
        //Retrieve Context from caller
        IGlobalContext providedContext = (IGlobalContext) clientRequestContext.getProperty(IServiceClient.CONTEXT_PROPERTY);

        if(providedContext!=null){
            callerContextBuilder.withTraceId(providedContext.currentTraceId());
            requestContextBuilder.withGlobalTraceId(providedContext.globalTraceId());
            if(providedContext.externalCtxt()!=null) {
                externalContextBuilder = IExternalCallerContext.builder().from(providedContext.externalCtxt());
            }
        }
        if(callerContextBuilder.getTraceId()==null){
            if(MDCUtils.getTraceId()!=null) {
                callerContextBuilder.withTraceId(MDCUtils.getTraceId());
            }
        }
        if(requestContextBuilder.getGlobalTraceId()==null){
            if(MDCUtils.getGlobalTraceId()!=null) {
                requestContextBuilder.withGlobalTraceId(MDCUtils.getGlobalTraceId());
            }
            else{
                requestContextBuilder.withGlobalTraceId(callerContextBuilder.getTraceId());
            }
        }
        //init user Context builder for user filter
        IUser user=(IUser)clientRequestContext.getProperty(FilterUtils.PROPERTY_USER_PARAM_NAME);
        String userToken=(String)clientRequestContext.getProperty(FilterUtils.PROPERTY_USER_TOKEN_PARAM_NAME);
        if(user!=null || userToken!=null) {
            userContextBuilder = IUserContext.builder().withUser(user).withToken(userToken);
        }

        IGlobalContext requestContext = factory.buildContext(requestContextBuilder
                .withCallerContextBuilder(callerContextBuilder)
                .withUserContextBuilder(userContextBuilder)
                .withExternalContextBuilder(externalContextBuilder));

        clientRequestContext.getHeaders().add(HttpHeaderUtils.HTTP_CONTEXT_HEADER, factory.encode(requestContext));
        clientRequestContext.setProperty(FilterUtils.PROPERTY_START_TIME_NANO_PARAM_NAME,System.nanoTime());
    }

    @Override
    public void filter(ClientRequestContext clientRequestContext, ClientResponseContext clientResponseContext) throws IOException {
        Long startTime = (Long)clientRequestContext.getProperty(FilterUtils.PROPERTY_START_TIME_NANO_PARAM_NAME);
        long duration=0;
        if(startTime!=null){
            duration = System.nanoTime()-duration;
        }
        String traceId=clientResponseContext.getHeaderString(HttpHeaderUtils.HTTP_CALLEE_TRACE_ID);

        LOG.info("Response {} received in {} ns for callee trace id <{}>",clientResponseContext.getStatus(),duration,traceId);
    }
}
