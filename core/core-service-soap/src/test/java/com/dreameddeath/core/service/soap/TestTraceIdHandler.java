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

package com.dreameddeath.core.service.soap;

import com.dreameddeath.core.service.http.HttpHeaderUtils;
import com.dreameddeath.core.service.soap.handler.AbstractSoapClientHandler;
import com.dreameddeath.core.service.soap.handler.wrapper.SoapServerResponseContextWrapper;
import com.google.common.base.Preconditions;

import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * Created by Christophe Jeunesse on 17/09/2016.
 */
public class TestTraceIdHandler extends AbstractSoapClientHandler {
    @Override
    protected boolean handleResponseMessage(SOAPMessageContext context, boolean isFault) {
        Preconditions.checkArgument(isFault==false);
        String traceId = new SoapServerResponseContextWrapper(context).getHeader(HttpHeaderUtils.HTTP_CALLEE_TRACE_ID);
        Preconditions.checkNotNull(traceId);
        try {
            String tid= context.getMessage().getSOAPBody().getFirstChild().getLastChild().getFirstChild().getNodeValue();
            Preconditions.checkArgument(tid!=null);
            Preconditions.checkArgument(traceId.equals(tid));
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
        return true;
    }
}
