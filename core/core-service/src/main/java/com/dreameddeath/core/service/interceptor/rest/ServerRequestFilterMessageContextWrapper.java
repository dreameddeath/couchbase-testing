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

import com.dreameddeath.core.service.interceptor.server.IServerRequestContextWrapper;

import javax.ws.rs.container.ContainerRequestContext;

/**
 * Created by Christophe Jeunesse on 15/09/2016.
 */
public class ServerRequestFilterMessageContextWrapper implements IServerRequestContextWrapper {
    private final ContainerRequestContext context;

    public ServerRequestFilterMessageContextWrapper(ContainerRequestContext context) {
        this.context = context;
    }

    @Override
    public String getHeader(String name) {
        return context.getHeaderString(name);
    }

    @Override
    public <T> T getProperty(String name, Class<T> clazz) {
        return (T)context.getProperty(name);
    }

    @Override
    public <T> void setProperty(String name, T value) {
        context.setProperty(name,value);
    }
}
