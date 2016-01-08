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
import com.dreameddeath.core.service.context.IGlobalContext;
import com.dreameddeath.core.service.context.IGlobalContextFactory;
import com.dreameddeath.core.user.IUserFactory;
import org.apache.cxf.jaxrs.ext.ContextProvider;
import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.HttpHeaders;

/**
 * Created by Christophe Jeunesse on 06/01/2016.
 */
public class GlobalContextProvider implements ContextProvider<IGlobalContext> {
    public final static String CONTEXT_HEADER = "X-GLOBAL-CONTEXT";

    private IGlobalContextFactory transcoder;
    private IUserFactory userFactory;
    private boolean autoSetupDefaultContext = false;

    public GlobalContextProvider(){
        super();
    }

    @Autowired
    public void setGlobalContextTranscoder(IGlobalContextFactory transcoder){
        this.transcoder = transcoder;
    }

    @Autowired
    public void setUserFactory(IUserFactory userFactory) {
        this.userFactory = userFactory;
    }

    public void setAutoSetupDefaultContext(boolean setupDefaultContext){
        this.autoSetupDefaultContext = setupDefaultContext;
    }

    @Override
    public IGlobalContext createContext(Message message) {
        HttpHeaders headers = new HttpHeadersImpl(message);
        String contextHeader = headers.getHeaderString(CONTEXT_HEADER);
        String userHeader = headers.getHeaderString(UserContextProvider.USER_TOKEN_HEADER);
        if(StringUtils.isNotEmpty(contextHeader)){
            return transcoder.decode(contextHeader);
        }
        else if (StringUtils.isNotEmpty(userHeader)) {
            return transcoder.buildContext(userFactory.fromToken(userHeader));
        }
        if(autoSetupDefaultContext) {
            return transcoder.buildDefaultContext();
        }
        throw new RuntimeException("Cannot setup Global context from message");
    }
}
