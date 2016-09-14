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

import com.dreameddeath.core.context.IContextFactory;
import com.dreameddeath.core.context.IGlobalContext;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.log.MDCUtils;
import com.dreameddeath.core.service.http.HttpHeaderUtils;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.user.IUserFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 12/01/2016.
 */
@Priority(1)
public class UserServerFilter implements ContainerRequestFilter {
    private IContextFactory contextFactory;
    private IUserFactory userFactory;
    private boolean setupDefaultUser=false;

    @Autowired
    public void setGlobalContextTranscoder(IContextFactory transcoder){
        this.contextFactory = transcoder;
    }

    @Autowired
    public void setUserFactory(IUserFactory userFactory) {
        this.userFactory = userFactory;
    }

    public void setSetupDefaultUser(boolean setupDefaultUser){
        this.setupDefaultUser = setupDefaultUser;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        IUser foundUser=null;
        String userToken = containerRequestContext.getHeaderString(HttpHeaderUtils.HTTP_HEADER_USER_TOKEN);
        if(StringUtils.isNotEmpty(userToken)){
            foundUser=userFactory.fromToken(userToken);
        }
        if(foundUser==null) {
            String contextToken = containerRequestContext.getHeaderString(HttpHeaderUtils.HTTP_CONTEXT_HEADER);
            if (StringUtils.isNotEmpty(contextToken)) {
                IGlobalContext context = contextFactory.decode(contextToken);
                if (context != null && context.userCtxt()!=null) {
                    foundUser = context.userCtxt().getUser();
                    userToken=context.userCtxt().getToken();
                }
            }
        }

        if(foundUser == null && setupDefaultUser) {
            foundUser = userFactory.defaultUser();
        }
        if(foundUser!=null) {
            MDCUtils.setUserId(foundUser.getUserId());
            containerRequestContext.setProperty(FilterUtils.PROPERTY_USER_PARAM_NAME, foundUser);
        }
        if(StringUtils.isNotEmpty(userToken)){
            containerRequestContext.setProperty(FilterUtils.PROPERTY_USER_TOKEN_PARAM_NAME,userToken);
        }
    }
}
