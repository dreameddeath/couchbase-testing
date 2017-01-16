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

package com.dreameddeath.infrastructure.daemon.manager;

import com.dreameddeath.core.service.client.AbstractServiceClientFactory;
import com.dreameddeath.core.service.client.rest.RestServiceClientFactory;
import com.dreameddeath.core.service.discovery.AbstractServiceDiscoverer;
import com.dreameddeath.core.service.discovery.rest.RestServiceDiscoverer;
import com.dreameddeath.core.service.model.common.CuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.registrar.AbstractServiceRegistrar;
import com.dreameddeath.core.service.registrar.ClientRegistrar;
import com.dreameddeath.core.service.registrar.RestServiceRegistrar;
import com.dreameddeath.core.service.utils.ServiceTypeUtils;
import com.dreameddeath.core.service.utils.rest.RestServiceTypeHelper;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import org.apache.curator.framework.CuratorFramework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 13/08/2015.
 */
public class ServiceDiscoveryManager {
    public enum Status{
        STARTING,
        STARTED,
        STOPPING,
        STOPPED
    }

    private final CuratorFramework curatorClient;
    private Status status = Status.STOPPED;
    private final AbstractWebServer parentWebServer;
    private final Map<ServiceKey,AbstractServiceRegistrar<? extends CuratorDiscoveryServiceDescription>> serviceRegistrarMap = new HashMap<>();
    private final Map<ServiceKey,AbstractServiceDiscoverer> serviceDiscovererMap = new HashMap<>();
    private final Map<ServiceKey,AbstractServiceClientFactory> serviceClientFactoryMap = new HashMap<>();

    private static class ServiceKey{
        private final String domain;
        private final String serviceType;

        public ServiceKey(String domain, String serviceType) {
            this.domain = domain;
            this.serviceType = serviceType;
        }

         private static ServiceKey buildKey(String domain,String serviceType){
            return new ServiceKey(domain,serviceType);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ServiceKey that = (ServiceKey) o;

            if (!domain.equals(that.domain)) return false;
            return serviceType.equals(that.serviceType);

        }

        @Override
        public int hashCode() {
            int result = domain.hashCode();
            result = 31 * result + serviceType.hashCode();
            return result;
        }
    }

    public ServiceDiscoveryManager(AbstractWebServer server){
        this.parentWebServer = server;
        this.curatorClient = server.getParentDaemon().getCuratorClient();
    }

    synchronized public RestServiceRegistrar getServiceRegistrar(String domain) throws Exception{
        return (RestServiceRegistrar) getServiceRegistrar(domain,RestServiceTypeHelper.SERVICE_TECH_TYPE);
    }

    synchronized public AbstractServiceRegistrar getServiceRegistrar(String domain,String serviceType) throws Exception{
        ServiceKey key=ServiceKey.buildKey(domain,serviceType);
        if(!serviceRegistrarMap.containsKey(key)){
            AbstractServiceRegistrar<? extends CuratorDiscoveryServiceDescription> newRegistrar = ServiceTypeUtils.getDefinition(serviceType).buildServiceRegistrar(curatorClient,domain);
            serviceRegistrarMap.put(key,newRegistrar);
            if(status==Status.STARTED){
                newRegistrar.start();
            }
        }
        return serviceRegistrarMap.get(key);
    }

    synchronized public RestServiceDiscoverer getServiceDiscoverer(String domain) throws Exception{
        return (RestServiceDiscoverer) getServiceDiscoverer(domain,RestServiceTypeHelper.SERVICE_TECH_TYPE);
    }

    synchronized public AbstractServiceDiscoverer getServiceDiscoverer(String domain,String serviceType) throws Exception{
        ServiceKey key=ServiceKey.buildKey(domain,serviceType);
        if(!serviceDiscovererMap.containsKey(key)){
            AbstractServiceDiscoverer newDiscoverer = ServiceTypeUtils.getDefinition(serviceType).buildDiscoverer(curatorClient,domain);
            serviceDiscovererMap.put(key,newDiscoverer);
            if(status==Status.STARTED || status==Status.STARTING){
                newDiscoverer.start();
            }
        }
        return serviceDiscovererMap.get(key);
    }


    synchronized public RestServiceClientFactory getClientFactory(String domain) throws Exception{
        return (RestServiceClientFactory) getClientFactory(domain,RestServiceTypeHelper.SERVICE_TECH_TYPE);
    }

    synchronized public AbstractServiceClientFactory getClientFactory(String domain,String serviceType) throws Exception{
        ServiceKey key=ServiceKey.buildKey(domain,serviceType);
        if(!serviceClientFactoryMap.containsKey(key)){
            ClientRegistrar clientRegistrar = new ClientRegistrar(curatorClient,domain,serviceType,parentWebServer.getParentDaemon().getUuid().toString(),parentWebServer.getUuid().toString());
            AbstractServiceClientFactory newServiceClientFactory = ServiceTypeUtils.getDefinition(serviceType).buildClientFactory(getServiceDiscoverer(domain,serviceType),clientRegistrar);
            serviceClientFactoryMap.put(key,newServiceClientFactory);
        }
        return serviceClientFactoryMap.get(key);
    }

    synchronized public <T extends AbstractServiceClientFactory> T getClientFactory(String domain,String serviceType,Class<T> clazz) throws Exception{
        return (T)getClientFactory(domain, serviceType);
    }

    synchronized public void setStatus(Status newStatus) throws Exception{
        if(status==Status.STOPPED && (newStatus==Status.STARTING||newStatus==Status.STARTED)){
            startDiscoverers();
        }

        if((status!=Status.STARTED)&& (newStatus==Status.STARTED)){
            startRegistrars();
        }

        if((status==Status.STARTED)&& (newStatus!=Status.STARTED)){
            stopRegistrars();
        }

        if((status!=Status.STOPPED)&& (newStatus!=Status.STARTED)){
            stopDiscoverers();
            stopClientRegistrar();
        }
        this.status=newStatus;
    }


    synchronized private void startRegistrars() throws Exception {
        for (AbstractServiceRegistrar registrar : serviceRegistrarMap.values()) {
            registrar.start();
        }
    }

    synchronized private void stopRegistrars() throws Exception {
        for (AbstractServiceRegistrar registrar : serviceRegistrarMap.values()) {
            registrar.stop();
        }
    }

    synchronized private void stopClientRegistrar() throws Exception{
        for(AbstractServiceClientFactory factory:serviceClientFactoryMap.values()){
            factory.stop();
        }
    }

    synchronized public List<AbstractServiceRegistrar<? extends CuratorDiscoveryServiceDescription>> getRegistrars(){
        return new ArrayList<>(serviceRegistrarMap.values());
    }

    synchronized private void startDiscoverers() throws Exception {
        for(AbstractServiceDiscoverer discoverer:serviceDiscovererMap.values()){
            discoverer.start();
        }
    }

    synchronized private void stopDiscoverers() throws Exception {
        for(AbstractServiceDiscoverer discoverer:serviceDiscovererMap.values()){
            discoverer.start();
        }
    }
}
