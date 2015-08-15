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

import java.util.HashMap;
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

    private final CuratorFramework _curatorClient;
    private Status _status = Status.STOPPED;
    private Map<String,ServiceRegistrar> _serviceRegistrarMap = new HashMap<>();
    private Map<String,ServiceDiscoverer> _serviceDiscovererMap = new HashMap<>();
    private Map<String,ServiceClientFactory> _serviceClientFactoryMap = new HashMap<>();

    public ServiceDiscoveryManager(CuratorFramework curatorClient){
        _curatorClient = curatorClient;
    }

    synchronized public ServiceRegistrar getServiceRegistrar(String domain) throws Exception{
        if(!_serviceRegistrarMap.containsKey(domain)){
            ServiceRegistrar newRegistrar = new ServiceRegistrar(_curatorClient,domain);
            _serviceRegistrarMap.put(domain,newRegistrar);
            if(_status==Status.STARTED){
                newRegistrar.start();
            }
        }
        return _serviceRegistrarMap.get(domain);
    }

    synchronized public ServiceDiscoverer getServiceDiscoverer(String domain) throws Exception{
        if(!_serviceDiscovererMap.containsKey(domain)){
            ServiceDiscoverer newDiscoverer = new ServiceDiscoverer(_curatorClient,domain);
            _serviceDiscovererMap.put(domain,newDiscoverer);
            if(_status==Status.STARTED || _status==Status.STARTING){
                newDiscoverer.start();
            }
        }
        return _serviceDiscovererMap.get(domain);
    }


    synchronized public ServiceClientFactory getClientFactory(String domain) throws Exception{
        if(!_serviceClientFactoryMap.containsKey(domain)){
            ServiceClientFactory newServiceClientFactory = new ServiceClientFactory(getServiceDiscoverer(domain));
            _serviceClientFactoryMap.put(domain,newServiceClientFactory);
        }
        return _serviceClientFactoryMap.get(domain);
    }

    synchronized public void setStatus(Status status) throws Exception{
        if(_status==Status.STOPPED && (status==Status.STARTING||status==Status.STARTED)){
            startDiscoverers();
        }

        if((_status!=Status.STARTED)&& (status==Status.STARTED)){
            startRegistrars();
        }

        if((_status==Status.STARTED)&& (status!=Status.STARTED)){
            stopRegistrars();
        }

        if((_status!=Status.STOPPED)&& (status!=Status.STARTED)){
            stopRegistrars();
        }
        _status=status;
    }


    synchronized private void startRegistrars() throws Exception {
        for (ServiceRegistrar registrar : _serviceRegistrarMap.values()) {
            registrar.start();
        }
    }

    synchronized private void stopRegistrars() throws Exception {
        for (ServiceRegistrar registrar : _serviceRegistrarMap.values()) {
            registrar.stop();
        }
    }
    synchronized private void startDiscoverers() throws Exception {
        for(ServiceDiscoverer discoverer:_serviceDiscovererMap.values()){
            discoverer.start();
        }
    }

    synchronized private void stopDiscoverers() throws Exception {
        for(ServiceDiscoverer discoverer:_serviceDiscovererMap.values()){
            discoverer.start();
        }
    }
}
