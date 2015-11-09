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

package com.dreameddeath.infrastructure.daemon.manager;

import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.registrar.ServiceRegistrar;
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
    private Map<String,ServiceRegistrar> serviceRegistrarMap = new HashMap<>();
    private Map<String,ServiceDiscoverer> serviceDiscovererMap = new HashMap<>();
    private Map<String,ServiceClientFactory> serviceClientFactoryMap = new HashMap<>();

    public ServiceDiscoveryManager(CuratorFramework curatorClient){
        this.curatorClient = curatorClient;
    }

    synchronized public ServiceRegistrar getServiceRegistrar(String domain) throws Exception{
        if(!serviceRegistrarMap.containsKey(domain)){
            ServiceRegistrar newRegistrar = new ServiceRegistrar(curatorClient,domain);
            serviceRegistrarMap.put(domain,newRegistrar);
            if(status==Status.STARTED){
                newRegistrar.start();
            }
        }
        return serviceRegistrarMap.get(domain);
    }

    synchronized public ServiceDiscoverer getServiceDiscoverer(String domain) throws Exception{
        if(!serviceDiscovererMap.containsKey(domain)){
            ServiceDiscoverer newDiscoverer = new ServiceDiscoverer(curatorClient,domain);
            serviceDiscovererMap.put(domain,newDiscoverer);
            if(status==Status.STARTED || status==Status.STARTING){
                newDiscoverer.start();
            }
        }
        return serviceDiscovererMap.get(domain);
    }


    synchronized public ServiceClientFactory getClientFactory(String domain) throws Exception{
        if(!serviceClientFactoryMap.containsKey(domain)){
            ServiceClientFactory newServiceClientFactory = new ServiceClientFactory(getServiceDiscoverer(domain));
            serviceClientFactoryMap.put(domain,newServiceClientFactory);
        }
        return serviceClientFactoryMap.get(domain);
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
            stopRegistrars();
        }
        this.status=newStatus;
    }


    synchronized private void startRegistrars() throws Exception {
        for (ServiceRegistrar registrar : serviceRegistrarMap.values()) {
            registrar.start();
        }
    }

    synchronized private void stopRegistrars() throws Exception {
        for (ServiceRegistrar registrar : serviceRegistrarMap.values()) {
            registrar.stop();
        }
    }

    synchronized public List<ServiceRegistrar> getRegistrars(){
        return new ArrayList<>(serviceRegistrarMap.values());
    }

    synchronized private void startDiscoverers() throws Exception {
        for(ServiceDiscoverer discoverer:serviceDiscovererMap.values()){
            discoverer.start();
        }
    }

    synchronized private void stopDiscoverers() throws Exception {
        for(ServiceDiscoverer discoverer:serviceDiscovererMap.values()){
            discoverer.start();
        }
    }
}
