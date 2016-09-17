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

import com.dreameddeath.core.service.interceptor.context.ClientUserInterceptor;
import com.dreameddeath.core.service.soap.handler.wrapper.SoapClientRequestContextWrapper;
import com.dreameddeath.core.service.soap.handler.wrapper.SoapClientResponseContextWrapper;
import com.dreameddeath.core.user.IUserFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Priority;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * Created by Christophe Jeunesse on 16/09/2016.
 */
@Priority(1)
public class SoapUserClientHandler extends AbstractSoapClientHandler  {
    private final ClientUserInterceptor userClientInterceptor = new ClientUserInterceptor();

    public SoapUserClientHandler() {
    }

    public SoapUserClientHandler(IUserFactory userFactory) {
        setUserFactory(userFactory);
    }

    @Autowired
    public void setUserFactory(IUserFactory userFactory) {
        userClientInterceptor.setUserFactory(userFactory);
    }

    @Override
    protected boolean handleRequestMessage(SOAPMessageContext context, boolean isFault) {
        return userClientInterceptor.processIncomingMessage(new SoapClientRequestContextWrapper(context));
    }

    @Override
    protected boolean handleResponseMessage(SOAPMessageContext context, boolean isFault) {
        return userClientInterceptor.processOutgoingMessage(new SoapClientRequestContextWrapper(context),new SoapClientResponseContextWrapper(context));
    }
}
