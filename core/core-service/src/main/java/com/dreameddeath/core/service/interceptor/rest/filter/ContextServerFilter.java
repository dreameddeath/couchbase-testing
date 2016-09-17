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

import com.dreameddeath.core.context.IContextFactory;
import com.dreameddeath.core.service.interceptor.context.ServerContextInterceptor;
import com.dreameddeath.core.service.interceptor.rest.ServerRequestFilterMessageContextWrapper;
import com.dreameddeath.core.service.interceptor.rest.ServerResponseFilterMessageContextWrapper;
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
    private final ServerContextInterceptor messageContextInterceptor = new ServerContextInterceptor();

    @Autowired
    public void setGlobalContextTranscoder(IContextFactory contextFactory){
        messageContextInterceptor.setGlobalContextTranscoder(contextFactory);
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        messageContextInterceptor.processIncomingMessage(new ServerRequestFilterMessageContextWrapper(containerRequestContext));
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        messageContextInterceptor.processOutgoingMessage(new ServerRequestFilterMessageContextWrapper(containerRequestContext),new ServerResponseFilterMessageContextWrapper(containerResponseContext));
    }
}
