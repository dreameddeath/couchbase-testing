/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.service.context.provider;

import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.context.IGlobalContext;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.user.IUserFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 12/01/2016.
 */
public class UserClientFilter implements ClientRequestFilter {
    private IUserFactory userFactory;

    @Autowired
    public void setUserFactory(IUserFactory userFactory) {
        this.userFactory = userFactory;
    }

    @Override
    public void filter(ClientRequestContext containerRequestContext) throws IOException {
        IUser user = (IUser)containerRequestContext.getProperty(IServiceClient.USER_PROPERTY);
        if(user!=null){
            containerRequestContext.getHeaders().add(UserServerFilter.HTTP_HEADER_USER_TOKEN,userFactory.toToken(user));
        }
        else {
            IGlobalContext context = (IGlobalContext) containerRequestContext.getProperty(IServiceClient.CONTEXT_PROPERTY);
            if(context!=null && context.userCtxt()!=null && context.userCtxt().getUser()!=null){
                containerRequestContext.getHeaders().add(UserServerFilter.HTTP_HEADER_USER_TOKEN,userFactory.toToken(context.userCtxt().getUser()));
            }
        }
    }
}
