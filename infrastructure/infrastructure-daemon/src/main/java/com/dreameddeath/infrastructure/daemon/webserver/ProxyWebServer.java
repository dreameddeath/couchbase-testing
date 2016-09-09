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

package com.dreameddeath.infrastructure.daemon.webserver;

import com.dreameddeath.core.service.utils.RestServiceTypeHelper;
import com.dreameddeath.infrastructure.daemon.servlet.ProxyServletContextHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 21/08/2015.
 */
public class ProxyWebServer extends AbstractWebServer {
    public ProxyWebServer(Builder builder){
        super(builder);

        List<ServletContextHandler> handlersList = new ArrayList<>();

        for(String serviceType:builder.perServiceTypediscoverDomains.keySet()) {
            Collection<String> domains = builder.perServiceTypediscoverDomains.get(serviceType);
            Preconditions.checkNotNull(builder.perServiceTypePath.get(serviceType),"The service type %d should have a path defined",serviceType);
            handlersList.add(new ProxyServletContextHandler(this, domains,serviceType,builder.perServiceTypePath.get(serviceType)));
        }

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        ServletContextHandler[] handlersArray =  new ServletContextHandler[handlersList.size()];
        for(int handlerPos=0;handlerPos<handlersArray.length;++handlerPos){
            handlersArray[handlerPos] = handlersList.get(handlerPos);
        }
        contexts.setHandlers(handlersArray);
        setHandler(contexts);
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder extends AbstractWebServer.Builder<Builder>{
        private final SetMultimap<String,String> perServiceTypediscoverDomains = HashMultimap.create();
        private final Map<String,String> perServiceTypePath = new HashMap<>();

        public Builder(){
            perServiceTypePath.put(RestServiceTypeHelper.SERVICE_TYPE,"");
        }

        public Builder withDiscoverDomain(String domain){
            return withDiscoverDomain(RestServiceTypeHelper.SERVICE_TYPE,domain);
        }

        public Builder withDiscoverDomain(String serviceType,String domain){
            perServiceTypediscoverDomains.put(serviceType,domain);
            return this;
        }

        public Builder withDiscoverDomainAndPath(String serviceType,String domain,String path){
            return withDiscoverDomain(serviceType,domain)
                    .withPath(serviceType,path);
        }


        public Builder withPath(String serviceType,String path){
            perServiceTypePath.put(serviceType,path);
            return this;
        }

    }
}
