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
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.service.http.HttpHeaderUtils;
import com.dreameddeath.core.service.interceptor.PropertyUtils;
import com.dreameddeath.core.service.interceptor.server.IServerInterceptor;
import com.dreameddeath.core.service.interceptor.server.IServerRequestContextWrapper;
import com.dreameddeath.core.service.interceptor.server.IServerResponseContextWrapper;
import com.dreameddeath.core.user.IUser;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Christophe Jeunesse on 15/09/2016.
 */
public class ServerContextInterceptor implements IServerInterceptor {
    private IContextFactory contextFactory;

    @Autowired
    public void setGlobalContextTranscoder(IContextFactory contextFactory){
        this.contextFactory = contextFactory;
    }

    @Override
    public boolean processIncomingMessage(IServerRequestContextWrapper incomingMessageContext){
        IGlobalContext requestContext=null;
        String globalTraceId=null;
        IUser user = incomingMessageContext.getProperty(PropertyUtils.PROPERTY_USER_PARAM_NAME,IUser.class);
        String userToken = incomingMessageContext.getProperty(PropertyUtils.PROPERTY_USER_TOKEN_PARAM_NAME,String.class);
        ICallerContext.Builder callerContextBuilder=null;
        IExternalCallerContext.Builder externalCallerContextBuilder=null;


        String contextToken = incomingMessageContext.getHeader(HttpHeaderUtils.HTTP_CONTEXT_HEADER);
        if (StringUtils.isNotEmpty(contextToken)) {
            IGlobalContext callerGlobalContext = contextFactory.decode(contextToken);
            callerContextBuilder=ICallerContext.builder().from(callerGlobalContext.callerCtxt());
            externalCallerContextBuilder=IExternalCallerContext.builder().from(callerGlobalContext.externalCtxt());
            globalTraceId=callerGlobalContext.globalTraceId();
        }

        if(callerContextBuilder==null){
            String traceId = incomingMessageContext.getHeader(HttpHeaderUtils.HTTP_HEADER_TRACE_ID);
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
        incomingMessageContext.setProperty(PropertyUtils.PROPERTY_GLOBAL_CONTEXT_PARAM_NAME, requestContext);
        return true;
    }

    @Override
    public boolean processOutgoingMessage(IServerRequestContextWrapper incomingContext, IServerResponseContextWrapper outgoingContext){
        IGlobalContext context = incomingContext.getProperty(PropertyUtils.PROPERTY_GLOBAL_CONTEXT_PARAM_NAME,IGlobalContext.class);
        if(context!=null){
            outgoingContext.setHeader(HttpHeaderUtils.HTTP_CALLEE_TRACE_ID,context.currentTraceId());
        }

        return true;
    }
}
