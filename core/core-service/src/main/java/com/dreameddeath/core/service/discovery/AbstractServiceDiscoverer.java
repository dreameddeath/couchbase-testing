/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.service.discovery;

import com.dreameddeath.core.curator.discovery.impl.StandardCuratorDiscoveryImpl;
import com.dreameddeath.core.curator.discovery.standard.ICuratorDiscovery;
import com.dreameddeath.core.curator.discovery.standard.ICuratorDiscoveryLifeCycleListener;
import com.dreameddeath.core.curator.discovery.standard.ICuratorDiscoveryListener;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.json.BaseObjectMapperConfigurator;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.exception.ServiceDiscoveryException;
import com.dreameddeath.core.service.model.common.*;
import com.dreameddeath.core.service.registrar.IEndPointDescription;
import com.dreameddeath.core.service.utils.ServiceInstanceSerializerImpl;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import com.dreameddeath.core.service.utils.UriUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 18/01/2015.
 */
public abstract class AbstractServiceDiscoverer<TSPEC,T extends CuratorDiscoveryServiceDescription<TSPEC>> extends StandardCuratorDiscoveryImpl<ServiceDescription> {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractServiceDiscoverer.class);
    private final ObjectMapper mapper= ObjectMapperFactory.BASE_INSTANCE.getMapper(BaseObjectMapperConfigurator.BASE_TYPE);
    private ServiceDiscovery<T> serviceDiscovery;
    private final ConcurrentMap<String,ProviderInfo> serviceProviderMap=new ConcurrentHashMap<>();
    private final List<IServiceDiscovererListener> listeners=new CopyOnWriteArrayList<>();
    private final String domain;
    private final String serviceTechnicalType;
    private final CountDownLatch started = new CountDownLatch(1);

    private class ProviderInfo{
        private final IServiceProviderSupplier<T> providerSupplier;
        private final ServiceDescription infoDescription;

        public ProviderInfo(ServiceProvider<T> provider, ServiceDescription infoDescription) {
            this.providerSupplier = new StandardServiceProviderSupplier<>(provider);
            this.infoDescription = infoDescription;
        }
    }

    @Override
    protected void preparePath() {
        ServiceNamingUtils.buildServiceDiscovererDomain(getClient(),domain, serviceTechnicalType);
        super.preparePath();
    }

    protected abstract Class<T> getDescriptionClass();

    public AbstractServiceDiscoverer(final CuratorFramework client, final String domain,String serviceTechnicalType){
        super(client,ServiceNamingUtils.buildServiceDomainPathName(domain, serviceTechnicalType, ServiceNamingUtils.DomainPathType.SERVER));
        this.serviceTechnicalType = serviceTechnicalType;
        this.domain = domain;
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(getDescriptionClass())
                .serializer((InstanceSerializer<T>)new ServiceInstanceSerializerImpl())
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
                    ServiceProvider<T> provider = serviceDiscovery.serviceProviderBuilder().serviceName(obj.getFullName()).build();
                    ProviderInfo info=new ProviderInfo(provider,obj);
                    if(serviceProviderMap.putIfAbsent(obj.getFullName(), info)==null){
                        provider.start();
                        for(IServiceDiscovererListener listener:listeners){
                            try {
                                listener.onProviderRegister(AbstractServiceDiscoverer.this, provider, obj);
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
                    ProviderInfo info=serviceProviderMap.remove(oldObj.getFullName());
                    if(info!=null){
                        info.providerSupplier.getServiceProvider().close();
                        for(IServiceDiscovererListener listener:listeners){
                            try {
                                listener.onProviderUnRegister(AbstractServiceDiscoverer.this, info.providerSupplier.getServiceProvider(), oldObj);
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
                try {
                    ServiceProvider<T> provider = serviceDiscovery.serviceProviderBuilder().serviceName(obj.getFullName()).build();
                    ProviderInfo info=new ProviderInfo(provider,obj);
                    if(serviceProviderMap.putIfAbsent(obj.getFullName(), info)==null){
                        provider.start();
                        for(IServiceDiscovererListener listener:listeners){
                            try {
                                listener.onProviderRegister(AbstractServiceDiscoverer.this, provider, obj);
                            }
                            catch (Exception e){
                                LOG.error("Error on registrar "+obj.getFullName()+" for listener "+listener,e);
                            }
                        }
                    }
                    else{
                        ProviderInfo oldInfo=serviceProviderMap.get(obj.getFullName());
                        for(IServiceDiscovererListener listener:listeners){
                        try {
                            listener.onProviderUpdate(AbstractServiceDiscoverer.this, oldInfo.providerSupplier.getServiceProvider(), obj);
                        }
                        catch (Exception e){
                            LOG.error("Error on update "+obj.getFullName()+" for listener "+listener,e);
                        }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Cannot register service " + obj.getFullName(), e);
                }
            }
        });
    }

    public  IServiceProviderSupplier<T> getServiceProviderSupplier(String fullName) throws ServiceDiscoveryException{
        return getServiceProviderSupplier(fullName,30,TimeUnit.SECONDS);
    }

    public  IServiceProviderSupplier<T> getServiceProviderSupplier(String fullName, long timeout, TimeUnit unit) throws ServiceDiscoveryException{
        try {
            waitStarted(timeout,unit);
        }
        catch(InterruptedException e){
            throw new ServiceDiscoveryException("Not yet started",e);
        }

        ProviderInfo info=serviceProviderMap.get(fullName);
        if(info==null){
            LOG.warn("Cannot find provider for service name {}, use Deferred Supplier",fullName);
            return new DeferredServiceProviderSupplier<>(()->{
                ProviderInfo deferredInfo=serviceProviderMap.get(fullName);
                if(deferredInfo==null){
                    LOG.error("Cannot find even in deferred mode the service {}",fullName);
                    throw new IllegalStateException("Cannot find service <"+fullName+">");
                }
                return deferredInfo.providerSupplier.getServiceProvider();
            });
        }
        else {
            return info.providerSupplier;
        }
    }


    public ServiceInstance<T> getInstance(String fullName) throws ServiceDiscoveryException{
        try {
            return getServiceProviderSupplier(fullName).getServiceProvider().getInstance();
        }
        catch(ServiceDiscoveryException e){
            throw e;
        }
        catch(Exception e){
            throw new ServiceDiscoveryException("Cannot get instance for service "+fullName,e);
        }
    }

    public ServiceInstance<T> getInstance(String fullName, String uid) throws ServiceDiscoveryException{
        try {
            ServiceProvider<T> provider = getServiceProviderSupplier(fullName).getServiceProvider();
            for(ServiceInstance<T> instance : provider.getAllInstances()){
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


    protected abstract ServiceInstanceDescription buildInstanceDescription(ServiceInstance<T> instance);

    public ServicesByNameInstanceDescription getInstancesDescription() throws ServiceDiscoveryException{
        ServicesByNameInstanceDescription desc = new ServicesByNameInstanceDescription();
        for(Map.Entry<String,ProviderInfo> entry:serviceProviderMap.entrySet()){
            try {
                for (ServiceInstance<T> instance : entry.getValue().providerSupplier.getServiceProvider().getAllInstances()) {
                    desc.addServiceInstance(buildInstanceDescription(instance));
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
            for (ServiceInstance<T> instance : getServiceProviderSupplier(fullName).getServiceProvider().getAllInstances()) {
                result.addServiceInstance(buildInstanceDescription(instance));
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
        for(Map.Entry<String,ProviderInfo> entry:serviceProviderMap.entrySet()){
            String fullName = entry.getKey();
            String type = ServiceNamingUtils.getTypeFromServiceFullName(fullName);
            String name = ServiceNamingUtils.getNameFromServiceFullName(fullName);
            String version = ServiceNamingUtils.getVersionFromServiceFullName(fullName);

            if(StringUtils.isNotEmpty(filterName) && !filterName.equals(name)){
                continue;
            }
            ServiceInfoDescription foundServiceInfoDescription  = mapServiceByName.get(name);
            if(foundServiceInfoDescription ==null){
                foundServiceInfoDescription  = new ServiceInfoDescription();
                foundServiceInfoDescription.setFuncType(type);
                foundServiceInfoDescription.setName(name);
                foundServiceInfoDescription.setTechType(serviceTechnicalType);
                mapServiceByName.put(name,foundServiceInfoDescription);
            }

            ServiceInfoVersionDescription<TSPEC> foundServiceVersionInfoDescription = foundServiceInfoDescription.addIfNeededServiceVersionInfoDescriptionMap(version, new ServiceInfoVersionDescription());
            try {
                for (ServiceInstance<T> instance : entry.getValue().providerSupplier.getServiceProvider().getAllInstances()) {

                    if(foundServiceInfoDescription.getAccess()==null){
                        foundServiceInfoDescription.setAccess(instance.getPayload().getAccessType().toString());
                    }
                    else{
                        String newAccessType=instance.getPayload().getAccessType().toString();
                        String currentAccess=foundServiceInfoDescription.getAccess();
                        Preconditions.checkArgument(currentAccess.equalsIgnoreCase(newAccessType),"Cannot have multiple access mode for service %s (%s vs %s)",fullName,currentAccess,newAccessType);
                    }
                    if(foundServiceInfoDescription.getFuncType()==null){
                        foundServiceInfoDescription.setFuncType(instance.getPayload().getType());
                    }
                    else{
                        String currentType=foundServiceInfoDescription.getFuncType();
                        String instanceType = instance.getPayload().getType();
                        Preconditions.checkArgument(currentType.equalsIgnoreCase(instanceType),"Cannot have multiple functional Type for service %s (%s vs %s)",fullName,currentType,instanceType);
                    }
                    if(foundServiceVersionInfoDescription.getFullName()==null){
                        foundServiceVersionInfoDescription.setFullName(fullName);
                        foundServiceVersionInfoDescription.setState(instance.getPayload().getState());
                        if(instance.getPayload()!=null) {
                            foundServiceVersionInfoDescription.setSpec(instance.getPayload().getSpec());
                        }
                    }
                    ServiceInfoVersionInstanceDescription instanceDescr = new ServiceInfoVersionInstanceDescription();
                    instanceDescr.setAddress(instance.getAddress());
                    instanceDescr.setPort(instance.getPort());
                    instanceDescr.setDaemonUid(IEndPointDescription.Utils.getDaemonUid(instance.getId()));
                    instanceDescr.setWebServerUid(IEndPointDescription.Utils.getServerUid(instance.getId()));
                    instanceDescr.setUriSpec(UriUtils.buildUri(instance,false));
                    instanceDescr.setUid(instance.getId());
                    if(instance.getPayload()!=null){
                        instanceDescr.setProtocols(instance.getPayload().getProtocols().stream().map(Enum::toString).collect(Collectors.toSet()));
                    }
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

    public IServiceDiscovererListener<T> addListener(IServiceDiscovererListener<T> listener){
        Map<String,ProviderInfo> copy = new HashMap<>(serviceProviderMap);
        listeners.add(listener);
        for(Map.Entry<String,ProviderInfo> entry :copy.entrySet()){
            try {
                listener.onProviderRegister(AbstractServiceDiscoverer.this, entry.getValue().providerSupplier.getServiceProvider(), entry.getValue().infoDescription);
            }
            catch (Exception e){
                LOG.error("Error on register "+entry.getValue().infoDescription.getFullName()+" for listener "+listener,e);
            }
        }
        return listener;
    }

    public IServiceDiscovererListener<T> removeListener(IServiceDiscovererListener<T> listener){
        Map<String,ProviderInfo> copy = new HashMap<>(serviceProviderMap);
        if(listeners.remove(listener)) {
            for (Map.Entry<String, ProviderInfo> entry : copy.entrySet()) {
                try {
                    listener.onProviderUnRegister(AbstractServiceDiscoverer.this, entry.getValue().providerSupplier.getServiceProvider(), entry.getValue().infoDescription);
                } catch (Exception e) {
                    LOG.error("Error on unregister " + entry.getValue().infoDescription.getFullName() + " for listener " + listener, e);
                }
            }
        }
        return listener;
    }


    public String getDomain() {
        return domain;
    }

    public ServiceInstanceDescription getInstanceDescription(String fullName, String id) throws ServiceDiscoveryException {
        return buildInstanceDescription(this.getInstance(fullName,id));
    }

    public String getServiceTechnicalType() {
        return serviceTechnicalType;
    }

}
