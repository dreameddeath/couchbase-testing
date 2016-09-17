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
import com.dreameddeath.core.service.interceptor.context.ServerUserInterceptor;
import com.dreameddeath.core.service.interceptor.rest.ServerRequestFilterMessageContextWrapper;
import com.dreameddeath.core.service.interceptor.rest.ServerResponseFilterMessageContextWrapper;
import com.dreameddeath.core.user.IUserFactory;
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
@Priority(1)
public class UserServerFilter implements ContainerRequestFilter,ContainerResponseFilter {
    private final ServerUserInterceptor serverUserInterceptor = new ServerUserInterceptor();
    @Autowired
    public void setGlobalContextTranscoder(IContextFactory contextFactory){
        serverUserInterceptor.setGlobalContextFactory(contextFactory);
    }

    @Autowired
    public void setUserFactory(IUserFactory userFactory) {
        serverUserInterceptor.setUserFactory(userFactory);
    }

    public void setSetupDefaultUser(boolean setupDefaultUser){
        serverUserInterceptor.setSetupDefaultUser(setupDefaultUser);
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        serverUserInterceptor.processIncomingMessage(new ServerRequestFilterMessageContextWrapper(containerRequestContext));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        serverUserInterceptor.processOutgoingMessage(new ServerRequestFilterMessageContextWrapper(requestContext),new ServerResponseFilterMessageContextWrapper(responseContext));
    }
}
