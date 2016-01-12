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

import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.context.IGlobalContext;
import com.dreameddeath.core.service.context.IGlobalContextFactory;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.user.IUserFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 12/01/2016.
 */
public class UserServerFilter implements ContainerRequestFilter {
    public static final String HTTP_HEADER_USER_TOKEN = "X-USER-TOKEN";
    public static final String PROPERTY_PARAM_NAME = IUser.class.getName();

    private IGlobalContextFactory transcoder;
    private IUserFactory userFactory;
    private boolean setupDefaultUser=false;

    @Autowired
    public void setGlobalContextTranscoder(IGlobalContextFactory transcoder){
        this.transcoder = transcoder;
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
        String userToken = containerRequestContext.getHeaderString(HTTP_HEADER_USER_TOKEN);
        if(StringUtils.isNotEmpty(userToken)){
            foundUser=userFactory.fromToken(userToken);
        }
        if(foundUser==null) {
            String contextToken = containerRequestContext.getHeaderString(ContextServerFilter.HTTP_CONTEXT_HEADER);
            if (StringUtils.isNotEmpty(contextToken)) {
                IGlobalContext context = transcoder.decode(contextToken);
                if (context != null && context.userCtxt()!=null) {
                    foundUser = context.userCtxt().getUser();
                }
            }
        }

        if(foundUser == null && setupDefaultUser) {
            foundUser = userFactory.defaultUser();
        }
        if(foundUser!=null) {
            containerRequestContext.setProperty(IServiceClient.USER_PROPERTY, foundUser);
        }
    }
}
