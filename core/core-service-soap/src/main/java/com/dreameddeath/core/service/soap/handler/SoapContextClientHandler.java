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

package com.dreameddeath.core.service.soap.handler;

import com.dreameddeath.core.context.IContextFactory;
import com.dreameddeath.core.service.interceptor.context.ClientContextInterceptor;
import com.dreameddeath.core.service.soap.handler.wrapper.SoapClientRequestContextWrapper;
import com.dreameddeath.core.service.soap.handler.wrapper.SoapClientResponseContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Priority;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * Created by Christophe Jeunesse on 16/09/2016.
 */
@Priority(2)
public class SoapContextClientHandler extends AbstractSoapClientHandler {
    private final ClientContextInterceptor messageContextInterceptor = new ClientContextInterceptor();

    public SoapContextClientHandler() {
    }

    public SoapContextClientHandler(IContextFactory contextFactory){
        setGlobalContextTranscoder(contextFactory);
    }

    @Autowired
    public void setGlobalContextTranscoder(IContextFactory contextFactory){
        messageContextInterceptor.setGlobalContextFactory(contextFactory);
    }

    @Override
    protected boolean handleRequestMessage(SOAPMessageContext context, boolean isFault) {
        return messageContextInterceptor.processIncomingMessage(new SoapClientRequestContextWrapper(context));
    }

    @Override
    protected boolean handleResponseMessage(SOAPMessageContext context, boolean isFault) {
        return messageContextInterceptor.processOutgoingMessage(new SoapClientRequestContextWrapper(context),new SoapClientResponseContextWrapper(context));
    }
}
