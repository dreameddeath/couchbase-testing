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
import com.dreameddeath.core.service.context.IGlobalContextFactory;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.user.IUserFactory;
import org.apache.cxf.jaxrs.ext.ContextProvider;
import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.HttpHeaders;

/**
 * Created by Christophe Jeunesse on 06/01/2016.
 */
public class UserContextProvider implements ContextProvider<IUser> {
    public static final String USER_TOKEN_HEADER = "X-USER-TOKEN";

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
    public IUser createContext(Message message) {
        HttpHeaders headers = new HttpHeadersImpl(message);
        String userToken = headers.getHeaderString(USER_TOKEN_HEADER);
        if(StringUtils.isNotEmpty(userToken)){
            return userFactory.fromToken(userToken);
        }
        String contextToken = headers.getHeaderString(GlobalContextProvider.CONTEXT_HEADER);
        if(StringUtils.isNotEmpty(contextToken)){
            return transcoder.decode(contextToken).userCtxt().getUser();
        }
        if(setupDefaultUser) {
            return userFactory.defaultUser();
        }
        throw new RuntimeException("Cannot setup User from message");
    }
}
