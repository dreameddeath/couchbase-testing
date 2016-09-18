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

package com.dreameddeath.core.service.client.rest;

import com.dreameddeath.core.json.JsonProviderFactory;
import com.dreameddeath.core.service.client.AbstractServiceClientImpl;
import com.dreameddeath.core.service.client.rest.rxjava.RxJavaWebTarget;
import com.dreameddeath.core.service.model.rest.RestCuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.utils.ServiceObjectMapperConfigurator;
import com.dreameddeath.core.service.utils.UriUtils;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Feature;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 03/09/2016.
 */
public class RestServiceClientImpl extends AbstractServiceClientImpl<RxJavaWebTarget,RestCuratorDiscoveryServiceDescription> implements IRestServiceClient{
    private final Map<String,ServiceInstanceConfig> configMap = new ConcurrentHashMap<>();

    public RestServiceClientImpl(ServiceProvider<RestCuratorDiscoveryServiceDescription> provider, String serviceFullName, RestServiceClientFactory factory) {
        super(provider, serviceFullName, factory);
    }


    @Override
    protected RxJavaWebTarget buildClient(final ServiceInstance<RestCuratorDiscoveryServiceDescription> instance) {
        ServiceInstanceConfig config = configMap.computeIfAbsent(instance.getId(), s ->
                new ServiceInstanceConfig(instance)
        );
        RxJavaWebTarget target = new RxJavaWebTarget(ClientBuilder.newBuilder().build().target(config.uri))
                .register(config.provider);
        if (((RestServiceClientFactory)getParentFactory()).getFeatureFactory() != null) {
            for (Feature feature : ((RestServiceClientFactory)getParentFactory()).getFeatureFactory().getClientFeatures()) {
                target = target.register(feature);
            }
        }
        return target;
    }


    private static class ServiceInstanceConfig{
        private String uri;
        private JacksonJsonProvider provider;

        private ServiceInstanceConfig(ServiceInstance<RestCuratorDiscoveryServiceDescription> instance){
            uri = UriUtils.buildUri(instance);
            String jsonProvider = instance.getPayload().getJsonProvider();
            if(jsonProvider==null){
                jsonProvider= ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR.getName();
            }
            provider = JsonProviderFactory.getProvider(jsonProvider);
        }
    }

}
