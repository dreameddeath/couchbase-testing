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

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

/**
 * Created by Christophe Jeunesse on 15/09/2016.
 */
public abstract class AbstractSoapHandler implements SOAPHandler<SOAPMessageContext> {
    private final boolean isClient;

    public AbstractSoapHandler(boolean isClient) {
        this.isClient = isClient;
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    protected final boolean isRequest(SOAPMessageContext context){
        Boolean outboundProperty = (Boolean)context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        return isClient?outboundProperty.booleanValue():!outboundProperty.booleanValue();
    }

    protected boolean handleRequestMessage(SOAPMessageContext context, boolean isFault){return true;}
    protected boolean handleResponseMessage(SOAPMessageContext context, boolean isFault){return true;}

    @Override
    public final boolean handleMessage(SOAPMessageContext context) {
        if(isRequest(context)){
            return handleRequestMessage(context,false);
        }
        else{
            return handleResponseMessage(context,false);
        }
    }

    @Override
    public final boolean handleFault(SOAPMessageContext context) {
        if(isRequest(context)){
            return handleRequestMessage(context,true);
        }
        else{
            return handleResponseMessage(context,true);
        }
    }

    @Override
    public void close(MessageContext context) {
    }
}
