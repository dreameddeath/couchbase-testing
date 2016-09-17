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

import com.dreameddeath.core.context.IGlobalContext;
import com.dreameddeath.core.log.MDCUtils;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.http.HttpHeaderUtils;
import com.dreameddeath.core.service.interceptor.PropertyUtils;
import com.dreameddeath.core.service.interceptor.client.IClientInterceptor;
import com.dreameddeath.core.service.interceptor.client.IClientRequestContextWrapper;
import com.dreameddeath.core.service.interceptor.client.IClientResponseContextWrapper;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.user.IUserFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Christophe Jeunesse on 15/09/2016.
 */
public class ClientUserInterceptor implements IClientInterceptor {
    private IUserFactory userFactory;

    @Autowired
    public void setUserFactory(IUserFactory userFactory) {
        this.userFactory = userFactory;
    }

    @Override
    public boolean processIncomingMessage(IClientRequestContextWrapper incomingContext) {
        IUser user = incomingContext.getProperty(IServiceClient.USER_PROPERTY,IUser.class);
        String userToken=null;
        IGlobalContext context = incomingContext.getProperty(IServiceClient.CONTEXT_PROPERTY,IGlobalContext.class);
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
            incomingContext.setProperty(PropertyUtils.PROPERTY_USER_PARAM_NAME, user);
        }
        if(userToken!=null){
            incomingContext.setProperty(PropertyUtils.PROPERTY_USER_TOKEN_PARAM_NAME, userToken);
            incomingContext.setHeader(HttpHeaderUtils.HTTP_HEADER_USER_TOKEN,userToken);
        }
        return true;
    }

    @Override
    public boolean processOutgoingMessage(IClientRequestContextWrapper incomingContext, IClientResponseContextWrapper outgoingContext) {
        return true;
    }
}
