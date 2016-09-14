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

import com.dreameddeath.core.user.IUser;
import org.apache.cxf.jaxrs.ext.ContextProvider;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import javax.annotation.Priority;
import javax.servlet.ServletRequest;

/**
 * Created by Christophe Jeunesse on 06/01/2016.
 */
@Priority(4)
public class UserContextProvider implements ContextProvider<IUser> {

    @Override
    public IUser createContext(Message message) {
        ServletRequest request = (ServletRequest)message.get(AbstractHTTPDestination.HTTP_REQUEST);
        IUser context = (IUser) request.getAttribute(FilterUtils.PROPERTY_USER_PARAM_NAME);
        if(context!=null){
            return context;
        }
        throw new RuntimeException("Cannot setup Global context from message");
    }
}
