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

import com.dreameddeath.core.curator.discovery.ICuratorDiscovery;
import com.dreameddeath.core.curator.discovery.ICuratorDiscoveryLifeCycleListener;
import com.dreameddeath.core.curator.discovery.ICuratorDiscoveryListener;
import com.dreameddeath.core.curator.discovery.impl.CuratorDiscoveryImpl;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.json.BaseObjectMapperConfigurator;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.client.ServiceClientImpl;
import com.dreameddeath.core.service.exception.ServiceDiscoveryException;
import com.dreameddeath.core.service.model.*;
import com.dreameddeath.core.service.registrar.IRestEndPointDescription;
import com.dreameddeath.core.service.utils.ServiceInstanceSerializerImpl;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;

/**
 * Created by Christophe Jeunesse on 18/01/2015.
 */
public class ServiceDiscoverer extends CuratorDiscoveryImpl<ServiceDescription>{
    private final static Logger LOG = LoggerFactory.getLogger(ServiceDiscoverer.class);
    private final ObjectMapper mapper= ObjectMapperFactory.BASE_INSTANCE.getMapper(BaseObjectMapperConfigurator.BASE_TYPE);
    private ServiceDiscovery<CuratorDiscoveryServiceDescription> serviceDiscovery;
    private final ConcurrentMap<String,ServiceProvider<CuratorDiscoveryServiceDescription>> serviceProviderMap=new ConcurrentHashMap<>();
    private final List<IServiceDiscovererListener> listeners=new CopyOnWriteArrayList<>();
    private final String domain;
    private final CountDownLatch started = new CountDownLatch(1);

    @Override
    protected void preparePath() {
        ServiceNamingUtils.buildServiceDiscovererDomain(getClient(),domain);
        super.preparePath();
    }

    public ServiceDiscoverer(final CuratorFramework client, final String domain){
        super(client,ServiceNamingUtils.buildServiceDomainPathName(domain, ServiceNamingUtils.DomainPathType.SERVER));
        this.domain = domain;
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(CuratorDiscoveryServiceDescription.class)
                .serializer(new ServiceInstanceSerializerImpl())
                .client(client)
                .basePath(getBasePath()).build();

        this.addLifeCycleListener(new ICuratorDiscoveryLifeCycleListener() {
            @Override
            public void onStart(ICuratorDiscovery discoverer, boolean isBefore) {
                if(isBefore) {
                    try {
                        serviceDiscovery.start();
                    } catch (Exception e) {
                        LOG.error("Cannot start discovery for domain "+domain, e);
                        throw new RuntimeException("Cannot start service discovery", e);
                    }
                }
                else{
                    started.countDown();
                }
            }

            @Override
            public void onStop(ICuratorDiscovery discoverer, boolean isBefore) {
                if(!isBefore) {
                    try {
                        serviceDiscovery.close();
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot stop service discovery for domain "+domain, e);
                    }
                }
            }
        });

        addListener(new ICuratorDiscoveryListener<ServiceDescription>() {
            @Override
            public void onRegister(String uid, ServiceDescription obj) {
                try {
                    ServiceProvider<CuratorDiscoveryServiceDescription> provider = serviceDiscovery.serviceProviderBuilder().serviceName(obj.getFullName()).build();
                    if(serviceProviderMap.putIfAbsent(obj.getFullName(), provider)==null){
                        provider.start();
                        for(IServiceDiscovererListener listener:listeners){
                            try {
                                listener.onProviderRegister(ServiceDiscoverer.this, provider, obj);
                            }
                            catch (Exception e){
                                LOG.error("Error on register "+obj.getFullName()+" for listener "+listener,e);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Cannot register service " + obj.getFullName(), e);
                }
            }

            @Override
            public void onUnregister(String uid, ServiceDescription oldObj) {
                try {
                    ServiceProvider<CuratorDiscoveryServiceDescription> provider=serviceProviderMap.remove(oldObj.getFullName());
                    if(provider!=null){
                        provider.close();
                        for(IServiceDiscovererListener listener:listeners){
                            try {
                                listener.onProviderUnRegister(ServiceDiscoverer.this, provider, oldObj);
                            }
                            catch (Exception e){
                                LOG.error("Error on unregister "+oldObj.getFullName()+" for listener "+listener,e);
                            }
                        }
                    }
                }
                catch(Exception e){
                    LOG.error("Cannot stop service "+oldObj.getFullName(),e);
                }
            }

            @Override
            public void onUpdate(String uid, ServiceDescription obj, ServiceDescription newObj) {
                onRegister(uid,obj);
            }
        });
    }

    public  ServiceProvider<CuratorDiscoveryServiceDescription> getServiceProvider(String name) throws ServiceDiscoveryException{
        return getServiceProvider(name,1,TimeUnit.SECONDS);
    }

    public  ServiceProvider<CuratorDiscoveryServiceDescription> getServiceProvider(String name,long timeout,TimeUnit unit) throws ServiceDiscoveryException{
        try {
            waitStarted();
        }
        catch(InterruptedException e){
            throw new ServiceDiscoveryException("Not yet started",e);
        }
        final CountDownLatch serviceProviderFound=new CountDownLatch(1);
        final IServiceDiscovererListener listener = new IServiceDiscovererListener() {
            @Override
            public void onProviderRegister(ServiceDiscoverer discoverer, ServiceProvider<CuratorDiscoveryServiceDescription> provider, ServiceDescription description) {
                if(description.getName().equals(name)){serviceProviderFound.countDown();}
            }
            @Override public void onProviderUnRegister(ServiceDiscoverer discoverer, ServiceProvider<CuratorDiscoveryServiceDescription> provider, ServiceDescription description) {}
        };
        ServiceProvider<CuratorDiscoveryServiceDescription> provider =serviceProviderMap.computeIfAbsent(name,missingName->{
            listeners.add(listener);
            return null;
        });

        if(provider==null){
            try {
                serviceProviderFound.await(timeout,unit);
            }
            catch (InterruptedException e){}
            provider=serviceProviderMap.get(name);
        }
        if(provider==null){
            LOG.error("Cannot find provider for service name {}",name);
            throw new ServiceDiscoveryException("Cannot find provider for service name "+name+" in domain "+ domain);
        }
        return provider;
    }


    public ServiceInstance<CuratorDiscoveryServiceDescription> getInstance(String fullName) throws ServiceDiscoveryException{
        try {
            return getServiceProvider(fullName).getInstance();
        }
        catch(ServiceDiscoveryException e){
            throw e;
        }
        catch(Exception e){
            throw new ServiceDiscoveryException("Cannot get instance for service "+fullName,e);
        }
    }

    public ServiceInstance<CuratorDiscoveryServiceDescription> getInstance(String fullName, String uid) throws ServiceDiscoveryException{
        try {
            ServiceProvider<CuratorDiscoveryServiceDescription> provider = getServiceProvider(fullName);
            for(ServiceInstance<CuratorDiscoveryServiceDescription> instance : provider.getAllInstances()){
                if(instance.getId().equals(uid)){
                    return instance;
                }
            }
        }
        catch(ServiceDiscoveryException e){
            throw e;
        }
        catch(Exception e){
            throw new ServiceDiscoveryException("Cannot get instance for service "+fullName+" and uid <"+uid+">",e);
        }
        throw new ServiceDiscoveryException("The instance <"+uid+"> isn't found for service name <"+fullName+">");
    }


    public ServicesByNameInstanceDescription getInstancesDescription() throws ServiceDiscoveryException{
        ServicesByNameInstanceDescription desc = new ServicesByNameInstanceDescription();
        for(Map.Entry<String,ServiceProvider<CuratorDiscoveryServiceDescription>> entry:serviceProviderMap.entrySet()){
            try {
                for (ServiceInstance<CuratorDiscoveryServiceDescription> instance : entry.getValue().getAllInstances()) {
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
        ServicesListInstanceDescription result = new ServicesListInstanceDescription();
        try {
            for (ServiceInstance<CuratorDiscoveryServiceDescription> instance : getServiceProvider(fullName).getAllInstances()) {
                result.addServiceInstance(new ServiceInstanceDescription(instance));
            }
        }
        catch(ServiceDiscoveryException e){
            throw e;
        }
        catch (Exception e){
            throw new ServiceDiscoveryException("Cannot find all service instances for service "+fullName,e);
        }
        return result;
    }


    public Collection<ServiceInfoDescription> getInstancesInfo(String filterName) throws ServiceDiscoveryException {
        Map<String,ServiceInfoDescription> mapServiceByName = new TreeMap<>();
        for(Map.Entry<String,ServiceProvider<CuratorDiscoveryServiceDescription>> entry:serviceProviderMap.entrySet()){
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
                for (ServiceInstance<CuratorDiscoveryServiceDescription> instance : entry.getValue().getAllInstances()) {
                    if(foundServiceVersionInfoDescription.getFullName()==null){
                        foundServiceVersionInfoDescription.setFullName(fullName);
                        foundServiceVersionInfoDescription.setState(instance.getPayload().getState());
                        foundServiceVersionInfoDescription.setSwagger(instance.getPayload().getSwagger());
                    }
                    ServiceInfoVersionInstanceDescription instanceDescr = new ServiceInfoVersionInstanceDescription();
                    instanceDescr.setAddress(instance.getAddress());
                    instanceDescr.setPort(instance.getPort());
                    instanceDescr.setDaemonUid(IRestEndPointDescription.Utils.getDaemonUid(instance.getId()));
                    instanceDescr.setWebServerUid(IRestEndPointDescription.Utils.getServerUid(instance.getId()));
                    instanceDescr.setUriSpec(ServiceClientImpl.buildUri(instance));
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

    @Override
    protected ServiceDescription deserialize(String uid, byte[] element) {
        try {
            return mapper.readValue(element, ServiceDescription.class);
        }
        catch(IOException e){
            LOG.error("Cannot deserialize service node "+uid,e);
            throw new RuntimeException("Cannot deserialize service node "+uid,e);
        }
    }

    public void addListener(IServiceDiscovererListener listener){
        listeners.add(listener);
    }


    public String getDomain() {
        return domain;
    }
}
