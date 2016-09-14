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

import com.dreameddeath.core.context.IGlobalContext;
import com.dreameddeath.core.log.MDCUtils;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.http.HttpHeaderUtils;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.user.IUserFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 12/01/2016.
 */
@Priority(1)
public class UserClientFilter implements ClientRequestFilter {
    private IUserFactory userFactory;

    @Autowired
    public void setUserFactory(IUserFactory userFactory) {
        this.userFactory = userFactory;
    }

    @Override
    public void filter(ClientRequestContext containerRequestContext) throws IOException {
        IUser user = (IUser)containerRequestContext.getProperty(IServiceClient.USER_PROPERTY);
        String userToken=null;
        IGlobalContext context = (IGlobalContext) containerRequestContext.getProperty(IServiceClient.CONTEXT_PROPERTY);
        if(context!=null && context.userCtxt()!=null){

            if(context.userCtxt().getToken()!=null){
                userToken=context.userCtxt().getToken();
            }
            else if(user==null){
                user=context.userCtxt().getUser();
            }
        }

        if(user==null){
            if(MDCUtils.getUserId()!=null){
                user=userFactory.fromId(MDCUtils.getUserId());
            }
        }

        if(user!=null && userToken==null){
            userToken=userFactory.toToken(user);
        }
        if(user!=null) {
            containerRequestContext.setProperty(FilterUtils.PROPERTY_USER_PARAM_NAME, user);
        }
        if(userToken!=null){
            containerRequestContext.setProperty(FilterUtils.PROPERTY_USER_TOKEN_PARAM_NAME, userToken);
            containerRequestContext.getHeaders().add(HttpHeaderUtils.HTTP_HEADER_USER_TOKEN,userToken);
        }
    }
}
