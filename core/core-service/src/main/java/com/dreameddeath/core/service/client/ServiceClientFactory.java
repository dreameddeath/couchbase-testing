/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.service.client;

import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.exception.ServiceDiscoveryException;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

//import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;


/**
 * Created by CEAJ8230 on 04/03/2015.
 */
public class ServiceClientFactory {
    private final ServiceDiscoverer _serviceDiscoverer;
    private ConcurrentMap<String,WebTarget> _clientPerUri = new ConcurrentHashMap<>();

    public ServiceClientFactory(ServiceDiscoverer serviceDiscoverer){
        _serviceDiscoverer = serviceDiscoverer;
    }

    public WebTarget getClient(String serviceName,String serviceVersion){
        try{
            String uri = _serviceDiscoverer.getInstance(ServiceNamingUtils.buildServiceFullName(serviceName,serviceVersion)).buildUriSpec();
            return _clientPerUri.computeIfAbsent(uri, new Function<String, WebTarget>() {
                @Override
                public WebTarget apply(String s) {
                    return ClientBuilder.newBuilder().build().target(UriBuilder.fromUri(uri));
                }
            })
            ;
        }
        catch(ServiceDiscoveryException e){
            //Todo throw an error
        }
        return null;
    }

}
