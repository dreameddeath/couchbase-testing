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
import com.dreameddeath.core.service.interceptor.context.ServerContextInterceptor;
import com.dreameddeath.core.service.soap.handler.wrapper.SoapServerRequestContextWrapper;
import com.dreameddeath.core.service.soap.handler.wrapper.SoapServerResponseContextWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * Created by Christophe Jeunesse on 16/09/2016.
 */
public class SoapContextServerHandler extends AbstractSoapServerHandler {
    private final ServerContextInterceptor messageContextInterceptor = new ServerContextInterceptor();

    @Autowired
    public void setGlobalContextTranscoder(IContextFactory contextFactory){
        messageContextInterceptor.setGlobalContextTranscoder(contextFactory);
    }

    @Override
    protected boolean handleRequestMessage(SOAPMessageContext context, boolean isFault) {
        return messageContextInterceptor.processIncomingMessage(new SoapServerRequestContextWrapper(context));
    }

    @Override
    protected boolean handleResponseMessage(SOAPMessageContext context, boolean isFault) {
        return messageContextInterceptor.processOutgoingMessage(new SoapServerRequestContextWrapper(context),new SoapServerResponseContextWrapper(context));
    }
}
