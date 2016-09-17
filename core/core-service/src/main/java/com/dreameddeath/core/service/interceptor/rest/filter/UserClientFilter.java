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

import com.dreameddeath.core.service.interceptor.context.ClientUserInterceptor;
import com.dreameddeath.core.service.interceptor.rest.ClientRequestFilterContextWrapper;
import com.dreameddeath.core.service.interceptor.rest.ClientResponseFilterContextWrapper;
import com.dreameddeath.core.user.IUserFactory;
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
@Priority(1)
public class UserClientFilter implements ClientRequestFilter,ClientResponseFilter {
    private final ClientUserInterceptor userInterceptor = new ClientUserInterceptor();

    @Autowired
    public void setUserFactory(IUserFactory userFactory) {
        userInterceptor.setUserFactory(userFactory);
    }

    @Override
    public void filter(ClientRequestContext containerRequestContext) throws IOException{
        userInterceptor.processIncomingMessage(new ClientRequestFilterContextWrapper(containerRequestContext));
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        userInterceptor.processOutgoingMessage(new ClientRequestFilterContextWrapper(requestContext),new ClientResponseFilterContextWrapper(responseContext));
    }
}
