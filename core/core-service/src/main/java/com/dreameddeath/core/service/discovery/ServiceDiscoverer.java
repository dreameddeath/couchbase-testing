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

import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.service.exception.ServiceDiscoveryException;
import com.dreameddeath.core.service.model.*;
import com.dreameddeath.core.service.utils.ServiceInstanceSerializerImpl;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
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
        if(!basePath.startsWith("/")){
            basePath="/"+basePath;
        }
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


    public  ServiceProvider<ServiceDescription> getServiceProvider(String name) throws ServiceDiscoveryException{
        if(!_serviceProviderMap.containsKey(name)){
            loadService(name);
        }
        return _serviceProviderMap.get(name);
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

    public ServiceInstance<ServiceDescription> getInstance(String fullName) throws ServiceDiscoveryException{
        if(!_serviceProviderMap.containsKey(fullName)){
            loadService(fullName);
        }
        try {
            return _serviceProviderMap.get(fullName).getInstance();
        }
        catch(Exception e){
            throw new ServiceDiscoveryException("Cannot get instance for service "+fullName,e);
        }
    }

    public ServiceInstance<ServiceDescription> getInstance(String fullName,String uid) throws ServiceDiscoveryException{
        if(!_serviceProviderMap.containsKey(fullName)){
            loadService(fullName);
        }
        try {
            ServiceProvider<ServiceDescription> provider = _serviceProviderMap.get(fullName);
            for(ServiceInstance<ServiceDescription> instance : provider.getAllInstances()){
                if(instance.getId().equals(uid)){
                    return instance;
                }
            }
        }
        catch(Exception e){
            throw new ServiceDiscoveryException("Cannot get instance for service "+fullName+" and uid <"+uid+">",e);
        }
        throw new ServiceDiscoveryException("The instance <"+uid+"> isn't found for service name <"+fullName+">");
    }


    public ServicesByNameInstanceDescription getInstancesDescription() throws ServiceDiscoveryException{
        resyncAllServices();
        ServicesByNameInstanceDescription desc = new ServicesByNameInstanceDescription();
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

    public ServicesListInstanceDescription getInstancesDescriptionByFullName(String fullName) throws ServiceDiscoveryException{
        resyncAllServices();
        ServicesListInstanceDescription result = new ServicesListInstanceDescription();
        try {
            for (ServiceInstance<ServiceDescription> instance : _serviceProviderMap.get(fullName).getAllInstances()) {
                result.addServiceInstance(new ServiceInstanceDescription(instance));
            }
        }
        catch (Exception e){
            throw new ServiceDiscoveryException("Cannot find all service instances for service "+fullName,e);
        }
        return result;
    }


    public Collection<ServiceInfoDescription> getInstancesInfo(String filterName) throws ServiceDiscoveryException {
        resyncAllServices();
        Map<String,ServiceInfoDescription> mapServiceByName = new TreeMap<>();
        for(Map.Entry<String,ServiceProvider<ServiceDescription>> entry:_serviceProviderMap.entrySet()){
            String fullName = entry.getKey();
            String name = ServiceNamingUtils.getNameFromServiceFullName(fullName);
            if(StringUtils.isNotEmpty(filterName) && !filterName.equals(name)){
                continue;
            }
            ServiceInfoDescription foundServiceInfoDescription  = mapServiceByName.get(name);
            if(foundServiceInfoDescription ==null){
                foundServiceInfoDescription  = new ServiceInfoDescription();
                foundServiceInfoDescription.setName(name);
                mapServiceByName.put(name,foundServiceInfoDescription);
            }

            String version = ServiceNamingUtils.getVersionFromServiceFullName(fullName);
            ServiceInfoVersionDescription foundServiceVersionInfoDescription = foundServiceInfoDescription.addIfNeededServiceVersionInfoDescriptionMap(version, new ServiceInfoVersionDescription());
            try {
                for (ServiceInstance<ServiceDescription> instance : entry.getValue().getAllInstances()) {
                    if(foundServiceVersionInfoDescription.getFullName()==null){
                        foundServiceVersionInfoDescription.setFullName(fullName);
                        foundServiceVersionInfoDescription.setState(instance.getPayload().getState());
                        foundServiceVersionInfoDescription.setSwagger(instance.getPayload().getSwagger());
                    }
                    ServiceInfoVersionInstanceDescription instanceDescr = new ServiceInfoVersionInstanceDescription();
                    instanceDescr.setAddress(instance.getAddress());
                    instanceDescr.setPort(instance.getPort());
                    instanceDescr.setUriSpec(instance.getUriSpec().toString());
                    instanceDescr.setUid(instance.getId());
                    foundServiceVersionInfoDescription.addInstance(instanceDescr);
                }
            }
            catch(Exception e){
                throw new ServiceDiscoveryException("Cannot find all service instances for service "+entry.getKey(),e);
            }
        }
        return mapServiceByName.values();
    }
}
