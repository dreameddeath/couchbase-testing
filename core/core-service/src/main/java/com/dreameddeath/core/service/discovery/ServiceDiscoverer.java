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

package com.dreameddeath.core.service.discovery;

import com.dreameddeath.core.service.exception.ServiceDiscoveryException;
import com.dreameddeath.core.service.model.ServiceDescription;
import com.dreameddeath.core.service.model.ServiceInstanceDescription;
import com.dreameddeath.core.service.model.ServicesInstanceDescription;
import com.dreameddeath.core.service.utils.ServiceInstanceSerializerImpl;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 18/01/2015.
 */
public class ServiceDiscoverer {
    private final String _basePath;
    private final CuratorFramework _client;
    private ServiceDiscovery<ServiceDescription> _serviceDiscovery;
    private final ConcurrentMap<String,ServiceProvider<ServiceDescription>> _serviceProviderMap=new ConcurrentHashMap<>();


    public ServiceDiscoverer(CuratorFramework client,String basePath){
        _client = client;
        _basePath = basePath;
    }

    public void start() throws ServiceDiscoveryException {
        try {
            _client.blockUntilConnected(10, TimeUnit.SECONDS);
        }
        catch(InterruptedException e){
            throw new ServiceDiscoveryException("Cannot connect to Zookeeper",e);
        }

        ServiceNamingUtils.createBaseServiceName(_client,_basePath);
        _serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceDescription.class)
                .serializer(new ServiceInstanceSerializerImpl())
                .client(_client)
                .basePath(_basePath).build();
        try {
            _serviceDiscovery.start();
        }
        catch(Exception e){
            throw new ServiceDiscoveryException("Cannot start service discovery",e);
        }

        resyncAllServices();
    }

    synchronized public void loadService(String name) throws ServiceDiscoveryException {
        try {
            ServiceProvider<ServiceDescription> provider = _serviceDiscovery.serviceProviderBuilder().serviceName(name).build();
            provider.start();
            _serviceProviderMap.putIfAbsent(name, provider);
        }
        catch(Exception e){
            throw new ServiceDiscoveryException("Cannot start service provider for service "+name,e);
        }
    }

    public void resyncAllServices() throws ServiceDiscoveryException{
        try {
            for (String name : _serviceDiscovery.queryForNames()) {
                loadService(name);
            }
        }
        catch(ServiceDiscoveryException e){
            throw e;
        }
        catch(Exception e){
            throw new ServiceDiscoveryException("Cannot request existing service names",e);
        }
    }

    public ServiceInstance<ServiceDescription> getInstance(String name) throws ServiceDiscoveryException{
        if(!_serviceProviderMap.containsKey(name)){
            loadService(name);
        }
        try {
            return _serviceProviderMap.get(name).getInstance();
        }
        catch(Exception e){
            throw new ServiceDiscoveryException("Cannot get instance for service "+name,e);
        }
    }

    public ServicesInstanceDescription getInstancesDescription() throws ServiceDiscoveryException{
        resyncAllServices();
        ServicesInstanceDescription desc = new ServicesInstanceDescription();
        for(Map.Entry<String,ServiceProvider<ServiceDescription>> entry:_serviceProviderMap.entrySet()){
            try {
                for (ServiceInstance<ServiceDescription> instance : entry.getValue().getAllInstances()) {
                    desc.addServiceInstance(new ServiceInstanceDescription(instance));
                }
            }
            catch(Exception e){
                throw new ServiceDiscoveryException("Cannot find all service instances for service "+entry.getKey(),e);
            }
        }

        return desc;
    }

}
