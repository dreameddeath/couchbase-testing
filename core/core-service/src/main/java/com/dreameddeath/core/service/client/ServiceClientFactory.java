/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.service.client;

import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.exception.ServiceDiscoveryException;
import com.dreameddeath.core.service.model.ServiceDescription;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import org.apache.curator.x.discovery.ServiceInstance;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.util.*;

//import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;


/**
 * Created by Christophe Jeunesse on 04/03/2015.
 */
public class ServiceClientFactory {
    private static final Set<String> VARIABLE_TO_IGNORE = Collections.unmodifiableSet(new TreeSet<>(Arrays.asList("scheme","port","address")));
    private final ServiceDiscoverer serviceDiscoverer;
    private final ThreadLocal<Map<String,WebTarget>> clientPerUri = new ThreadLocal<Map<String,WebTarget>>() {
        protected Map<String,WebTarget> initialValue() {
            return new HashMap<>();
        }
    };

    public static String buildUri(ServiceInstance<ServiceDescription> serviceDescr){
        Map<String,Object> params = new TreeMap<>();
        serviceDescr.getUriSpec().getParts().stream()
                .filter(part -> part.isVariable() && !VARIABLE_TO_IGNORE.contains(part.getValue()))
                .forEach(part -> params.put(part.getValue(), "{" + part.getValue() + "}")
                );
        return serviceDescr.buildUriSpec(params);
    }

    public ServiceClientFactory(ServiceDiscoverer serviceDiscoverer){
        this.serviceDiscoverer = serviceDiscoverer;
    }

    public WebTarget getClient(String serviceName,String serviceVersion){
        try{
            String uri = buildUri(serviceDiscoverer.getInstance(ServiceNamingUtils.buildServiceFullName(serviceName, serviceVersion)));
            return clientPerUri.get().computeIfAbsent(uri, s -> ClientBuilder.newBuilder().build().target(UriBuilder.fromUri(s)));
        }
        catch(ServiceDiscoveryException e){
            throw new RuntimeException("Error during discovery of "+serviceName+"/"+serviceVersion,e);
            //Todo throw an error
        }
    }


    public WebTarget getClient(String serviceName,String serviceVersion,String uid){
        try{
            String uri = buildUri(serviceDiscoverer.getInstance(ServiceNamingUtils.buildServiceFullName(serviceName, serviceVersion),uid));
            return clientPerUri.get().computeIfAbsent(uri, s -> ClientBuilder.newBuilder().build().target(UriBuilder.fromUri(s)));
        }
        catch(ServiceDiscoveryException e){
            throw new RuntimeException("Error during discovery of "+serviceName+"/"+serviceVersion,e);
        }
    }

}
