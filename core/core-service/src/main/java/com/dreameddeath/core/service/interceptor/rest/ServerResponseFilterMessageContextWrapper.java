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

package com.dreameddeath.core.service.interceptor.rest;

import com.dreameddeath.core.service.interceptor.server.IServerResponseContextWrapper;

import javax.ws.rs.container.ContainerResponseContext;

/**
 * Created by Christophe Jeunesse on 15/09/2016.
 */
public class ServerResponseFilterMessageContextWrapper implements IServerResponseContextWrapper {
    private final ContainerResponseContext context;

    public ServerResponseFilterMessageContextWrapper(ContainerResponseContext context) {
        this.context = context;
    }

    @Override
    public void setHeader(String name, String value) {
        context.getHeaders().add(name,value);

    }

}