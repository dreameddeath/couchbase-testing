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

import com.dreameddeath.core.json.JsonProviderFactory;
import com.dreameddeath.core.service.model.CuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.utils.ServiceObjectMapperConfigurator;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.base.Preconditions;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 04/12/2015.
 */
public class ServiceClientImpl implements IServiceClient {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceClientImpl.class);
    private static final Set<String> VARIABLE_TO_IGNORE = Collections.unmodifiableSet(new TreeSet<>(Arrays.asList("scheme","port","address")));

    public static String buildUri(ServiceInstance<CuratorDiscoveryServiceDescription> serviceDescr){
        Map<String,Object> params = new TreeMap<>();
        serviceDescr.getUriSpec().getParts().stream()
                .filter(part -> part.isVariable() && !VARIABLE_TO_IGNORE.contains(part.getValue()))
                .forEach(part -> params.put(part.getValue(), "{" + part.getValue() + "}")
                );
        return serviceDescr.buildUriSpec(params);
    }


    private static class ServiceInstanceConfig{
        private String uri;
        private JacksonJsonProvider provider;

        private ServiceInstanceConfig(ServiceInstance<CuratorDiscoveryServiceDescription> instance){
            uri = buildUri(instance);
            String jsonProvider = instance.getPayload().getJsonProvider();
            if(jsonProvider==null){
                jsonProvider=ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR.getName();
            }
            provider = JsonProviderFactory.getProvider(jsonProvider);
        }
    }

    private final ServiceProvider<CuratorDiscoveryServiceDescription> provider;
    private final ServiceClientFactory parentFactory;
    private final Map<String,ServiceInstanceConfig> configMap = new ConcurrentHashMap<>();
    private final String fullName;
    private final UUID uuid=UUID.randomUUID();

    public ServiceClientImpl(ServiceProvider<CuratorDiscoveryServiceDescription> provider,String serviceFullName,ServiceClientFactory factory){
        Preconditions.checkNotNull(provider,"The provider for service %s is null",serviceFullName);
        this.provider = provider;
        this.fullName = serviceFullName;
        this.parentFactory = factory;
    }

    protected WebTarget buildClient(final ServiceInstance<CuratorDiscoveryServiceDescription> instance){
        ServiceInstanceConfig config = configMap.computeIfAbsent(instance.getId(), s ->
                new ServiceInstanceConfig(instance)
        );
        WebTarget target = ClientBuilder.newBuilder().build()
                .target(config.uri)
                .register(config.provider);
        if(parentFactory.getFeatureFactory()!=null){
            for(Feature feature:parentFactory.getFeatureFactory().getClientFeatures()){
                target=target.register(feature);
            }
        }
        return target;
    }

    @Override
    public WebTarget getInstance(){
        try {
            return buildClient(provider.getInstance());
        }
        catch(Exception e){
            LOG.error("Cannot get instance of service "+fullName,e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String  getUriInstance(){
        try {
            ServiceInstance<CuratorDiscoveryServiceDescription> instance = provider.getInstance();
            if(instance==null){
                throw new RuntimeException("Cannot get instance of service <"+fullName+">");
            }
            return buildUri(instance);
        }
        catch(Exception e){
            LOG.error("Cannot get instance of service "+fullName,e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public UUID getUuid(){
        return uuid;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public WebTarget getInstance(String instanceId){
        try {
            for (ServiceInstance<CuratorDiscoveryServiceDescription> instance : provider.getAllInstances()) {
                if (instance.getId().equals(instanceId)) {
                    return buildClient(instance);
                }
            }
        }
        catch(Exception e){
            LOG.error("Error during get instance <"+instanceId+"> of service "+fullName,e);
            throw new RuntimeException(e);
        }
        LOG.error("Cannot find instance <"+instanceId+"> of service "+fullName);
        throw new RuntimeException("Service instance not found");
    }

}
