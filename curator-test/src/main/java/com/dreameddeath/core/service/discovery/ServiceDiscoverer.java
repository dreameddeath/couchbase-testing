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

package com.dreameddeath.core.service.discovery;

import com.dreameddeath.core.model.ServiceDescription;
import com.dreameddeath.core.model.ServicesInstanceDescription;
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
 * Created by CEAJ8230 on 18/01/2015.
 */
public class ServiceDiscoverer {
    private final String _basePath;
    private final CuratorFramework _client;
    private ServiceDiscovery<ServiceDescription> _serviceDiscovery;
    private final ConcurrentMap<String,ServiceProvider<ServiceDescription>> _serviceProviderMap=new ConcurrentHashMap<>();

    public ServicesInstanceDescription getInstancesDescription() throws Exception{
        ServicesInstanceDescription desc = new ServicesInstanceDescription();
        for(Map.Entry<String,ServiceProvider<ServiceDescription>> entry:_serviceProviderMap.entrySet()){
            for(ServiceInstance<ServiceDescription> instance : entry.getValue().getAllInstances()) {
                desc.addServiceInstance(new ServicesInstanceDescription.ServiceInstanceDescription(instance));
            }
        }
        return desc;
    }

    public ServiceDiscoverer(CuratorFramework client,String basePath){
        _client = client;
        _basePath = basePath;
    }

    public void start() throws Exception {
        _client.blockUntilConnected(10, TimeUnit.SECONDS);
        _serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceDescription.class)
                .client(_client)
                .basePath(_basePath).build();
        _serviceDiscovery.start();

        //Preload names
        for(String name:_serviceDiscovery.queryForNames()){
            loadService(name);
        }
    }

    synchronized public void loadService(String name)throws Exception {
        ServiceProvider<ServiceDescription> provider = _serviceDiscovery.serviceProviderBuilder().serviceName(name).build();
        provider.start();
        _serviceProviderMap.putIfAbsent(name,provider);
    }
    public ServiceInstance<ServiceDescription> getInstance(String name) throws Exception{
        if(!_serviceProviderMap.containsKey(name)){
            loadService(name);
        }
        return _serviceProviderMap.get(name).getInstance();
    }


}
