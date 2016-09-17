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

package com.dreameddeath.core.service.interceptor.context;

import com.dreameddeath.core.context.*;
import com.dreameddeath.core.log.MDCUtils;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.http.HttpHeaderUtils;
import com.dreameddeath.core.service.interceptor.PropertyUtils;
import com.dreameddeath.core.service.interceptor.client.IClientInterceptor;
import com.dreameddeath.core.service.interceptor.client.IClientRequestContextWrapper;
import com.dreameddeath.core.service.interceptor.client.IClientResponseContextWrapper;
import com.dreameddeath.core.service.interceptor.rest.filter.ContextClientFilter;
import com.dreameddeath.core.user.IUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Christophe Jeunesse on 15/09/2016.
 */
public class ClientContextInterceptor implements IClientInterceptor {
    private final static Logger LOG= LoggerFactory.getLogger(ContextClientFilter.class);
    private IContextFactory factory;

    @Autowired
    public void setGlobalContextFactory(IContextFactory factory){
        this.factory = factory;
    }

    @Override
    public boolean processIncomingMessage(IClientRequestContextWrapper incomingContext){
        IGlobalContext.Builder requestContextBuilder= IGlobalContext.builder();
        ICallerContext.Builder callerContextBuilder= ICallerContext.builder();
        IExternalCallerContext.Builder externalContextBuilder=null;
        IUserContext.Builder userContextBuilder=null;
        //Retrieve Context from caller
        IGlobalContext providedContext = incomingContext.getProperty(IServiceClient.CONTEXT_PROPERTY,IGlobalContext.class);

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
        IUser user=incomingContext.getProperty(PropertyUtils.PROPERTY_USER_PARAM_NAME,IUser.class);
        String userToken=incomingContext.getProperty(PropertyUtils.PROPERTY_USER_TOKEN_PARAM_NAME,String.class);
        if(user!=null || userToken!=null) {
            userContextBuilder = IUserContext.builder().withUser(user).withToken(userToken);
        }

        IGlobalContext requestContext = factory.buildContext(requestContextBuilder
                .withCallerContextBuilder(callerContextBuilder)
                .withUserContextBuilder(userContextBuilder)
                .withExternalContextBuilder(externalContextBuilder));

        incomingContext.setHeader(HttpHeaderUtils.HTTP_CONTEXT_HEADER, factory.encode(requestContext));
        incomingContext.setProperty(PropertyUtils.PROPERTY_START_TIME_NANO_PARAM_NAME,System.nanoTime());
        return true;
    }

    @Override
    public boolean processOutgoingMessage(IClientRequestContextWrapper incomingContext, IClientResponseContextWrapper outgoingContext){
        Long startTime = incomingContext.getProperty(PropertyUtils.PROPERTY_START_TIME_NANO_PARAM_NAME,Long.class);
        double duration=0;
        if(startTime!=null){
            duration = (System.nanoTime()-duration)*1.0/1_0000_0000;
        }
        String traceId=outgoingContext.getHeader(HttpHeaderUtils.HTTP_CALLEE_TRACE_ID);

        LOG.info("Response received in {} ns for callee trace id <{}>",duration,traceId);
        return true;
    }
}
