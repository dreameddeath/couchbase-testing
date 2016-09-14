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
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.service.http.HttpHeaderUtils;
import com.dreameddeath.core.user.IUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 12/01/2016.
 */
@Priority(2)
public class ContextServerFilter implements ContainerRequestFilter, ContainerResponseFilter{
    private final static Logger LOG= LoggerFactory.getLogger(ContextServerFilter.class);

    private IContextFactory contextFactory;

    @Autowired
    public void setGlobalContextTranscoder(IContextFactory contextFactory){
        this.contextFactory = contextFactory;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        IGlobalContext requestContext=null;
        String globalTraceId=null;
        IUser user = (IUser)containerRequestContext.getProperty(FilterUtils.PROPERTY_USER_PARAM_NAME);
        String userToken = (String)containerRequestContext.getProperty(FilterUtils.PROPERTY_USER_TOKEN_PARAM_NAME);
        ICallerContext.Builder callerContextBuilder=null;
        IExternalCallerContext.Builder externalCallerContextBuilder=null;


        String contextToken = containerRequestContext.getHeaderString(HttpHeaderUtils.HTTP_CONTEXT_HEADER);
        if (StringUtils.isNotEmpty(contextToken)) {
            IGlobalContext callerGlobalContext = contextFactory.decode(contextToken);
            callerContextBuilder=ICallerContext.builder().from(callerGlobalContext.callerCtxt());
            externalCallerContextBuilder=IExternalCallerContext.builder().from(callerGlobalContext.externalCtxt());
            globalTraceId=callerGlobalContext.globalTraceId();
        }

        if(callerContextBuilder==null){
            String traceId = containerRequestContext.getHeaderString(HttpHeaderUtils.HTTP_HEADER_TRACE_ID);
            if(traceId!=null){
                callerContextBuilder = ICallerContext.builder().withTraceId(traceId);
                if(globalTraceId==null) {
                    globalTraceId = traceId;
                }
            }
        }
        IUserContext.Builder userBuilder=null;
        if(user!=null || userToken!=null){
            userBuilder=IUserContext.builder().withUser(user).withToken(userToken);
        }
        requestContext=contextFactory.buildContext(
                IGlobalContext.builder().withGlobalTraceId(globalTraceId)
                .withCallerContextBuilder(callerContextBuilder)
                .withExternalContextBuilder(externalCallerContextBuilder)
                .withUserContextBuilder(userBuilder)
        );
        containerRequestContext.setProperty(FilterUtils.PROPERTY_GLOBAL_CONTEXT_PARAM_NAME, requestContext);
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        IGlobalContext context = (IGlobalContext)containerRequestContext.getProperty(FilterUtils.PROPERTY_GLOBAL_CONTEXT_PARAM_NAME);
        if(context!=null){
            containerResponseContext.getHeaders().add(HttpHeaderUtils.HTTP_CALLEE_TRACE_ID,context.currentTraceId());
        }
    }
}
