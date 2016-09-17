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

package com.dreameddeath.core.service.soap.handler.wrapper;


import org.apache.cxf.message.Message;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Christophe Jeunesse on 16/09/2016.
 */
public abstract class AbstractSoapContextWrapper {
    private final SOAPMessageContext soapMessageContext;
    private final String httpParam;
    private final String propertyParam;

    public AbstractSoapContextWrapper(SOAPMessageContext soapMessageContext, boolean isClient, boolean isRequest) {
        this.soapMessageContext = soapMessageContext;
        final boolean isServer=!isClient;
        final boolean isResponse=!isRequest;
        httpParam=isRequest?MessageContext.HTTP_REQUEST_HEADERS:MessageContext.HTTP_RESPONSE_HEADERS;
        if((isClient && isRequest)||(isServer && isResponse)){
            propertyParam=MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS;
        }
        else{
            propertyParam=MessageContext.INBOUND_MESSAGE_ATTACHMENTS;
        }
    }

    public String getHeader(String name) {
        List<String>headers=((Map<String,List<String>>)soapMessageContext.computeIfAbsent(httpParam,param->new TreeMap<>())).get(name);
        return headers!=null?headers.get(0):null;
    }

    public void setHeader(String name, String value) {
        if(soapMessageContext.get(httpParam)==null){
            TreeMap map=new TreeMap<>();
            map.put(name,Collections.singletonList(value));
            if(httpParam.equals(MessageContext.HTTP_RESPONSE_HEADERS)){
                soapMessageContext.put(Message.PROTOCOL_HEADERS,map);
            }
            else{
                soapMessageContext.put(httpParam,map);
            }
        }
        else {
            ((Map<String, List<String>>) soapMessageContext.get(httpParam)).put(name, Collections.singletonList(value));
        }
    }

    public <T> T getProperty(String name, Class<T> clazz) {
        return (T) soapMessageContext.get(name);
    }

    public <T> void setProperty(String name, T value) {
        soapMessageContext.put(name, value);
        soapMessageContext.setScope(name, MessageContext.Scope.APPLICATION);
    }
}
