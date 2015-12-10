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

import com.dreameddeath.core.curator.discovery.ICuratorDiscoveryListener;
import com.dreameddeath.core.curator.discovery.impl.CuratorDiscoveryImpl;
import com.dreameddeath.core.json.BaseObjectMapperConfigurator;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.model.ClientInstanceInfo;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Christophe Jeunesse on 09/12/2015.
 */
public class ClientDiscoverer extends CuratorDiscoveryImpl<ClientInstanceInfo> {
    private final static Logger LOG = LoggerFactory.getLogger(ClientDiscoverer.class);

    private final ObjectMapper mapper= ObjectMapperFactory.BASE_INSTANCE.getMapper(BaseObjectMapperConfigurator.BASE_TYPE);
    private final String domain;
    private final ConcurrentMap<String,Set<ClientInstanceInfo>> clientInstances = new ConcurrentHashMap<>();

    private static Set<ClientInstanceInfo> buildSet(String serviceName){
        return Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public ClientDiscoverer(final CuratorFramework curatorFramework, final String domain) {
        super(curatorFramework, ServiceNamingUtils.buildServiceDomainPathName(domain, ServiceNamingUtils.DomainPathType.CLIENT));
        this.domain = domain;
        addListener(new ICuratorDiscoveryListener<ClientInstanceInfo>() {
            @Override
            public void onRegister(String uid, ClientInstanceInfo obj) {
                LOG.info("Registering client {} of name {}",uid,obj.getServiceName());
                Set<ClientInstanceInfo> clientInstanceInfoSet = clientInstances.computeIfAbsent(obj.getServiceName(), ClientDiscoverer::buildSet);
                clientInstanceInfoSet.add(obj);
            }

            @Override
            public void onUnregister(String uid, ClientInstanceInfo oldObj) {
                LOG.info("UnRegistering client {} of name {}",uid,oldObj.getServiceName());
                Set<ClientInstanceInfo> clientInstanceInfoSet = clientInstances.get(oldObj.getServiceName());
                if(clientInstanceInfoSet!=null) {
                    clientInstanceInfoSet.remove(oldObj);
                }
            }

            @Override
            public void onUpdate(String uid, ClientInstanceInfo oldObj, ClientInstanceInfo newObj) {
                LOG.info("UnRegistering client {} of name {}",uid,oldObj.getServiceName());
                Set<ClientInstanceInfo> oldClientInstanceInfoSet = clientInstances.get(oldObj.getServiceName());
                if(oldClientInstanceInfoSet!=null) {
                    oldClientInstanceInfoSet.remove(oldObj);
                }

                Set<ClientInstanceInfo> clientInstanceInfoSet = clientInstances.computeIfAbsent(newObj.getServiceName(),ClientDiscoverer::buildSet);
                clientInstanceInfoSet.add(newObj);
            }
        });
    }

    @Override
    protected void preparePath() {
        ServiceNamingUtils.buildServiceDiscovererDomain(getClient(),domain);
        super.preparePath();
    }

    public long getNbInstances(String serviceName, String version) {
        return getNbInstances(ServiceNamingUtils.buildServiceFullName(serviceName,version));
    }

    public long getNbInstances(String serviceFullName){
        Set<ClientInstanceInfo> list = clientInstances.get(serviceFullName);
        if(list!=null){
            return (long)list.size();
        }
        else{
            return 0L;
        }
    }

    public List<ClientInstanceInfo> getInstances(String serviceName,String version){
        return getInstances(ServiceNamingUtils.buildServiceFullName(serviceName,version));
    }

    public List<ClientInstanceInfo> getInstances(String serviceFullName){
        Set<ClientInstanceInfo> list = clientInstances.get(serviceFullName);
        if(list!=null){
            return new ArrayList<>(list);
        }
        else{
            return Collections.emptyList();
        }
    }


    public List<ClientInstanceInfo> getInstances(){
        List<ClientInstanceInfo> result = new ArrayList<>();
        clientInstances.values().forEach(result::addAll);
        return result;
    }



    @Override
    protected ClientInstanceInfo deserialize(String uid, byte[] element) {
        try {
            return mapper.readValue(element, ClientInstanceInfo.class);
        }
        catch(IOException e){
            LOG.error("Cannot deserialize client node "+uid,e);
            throw new RuntimeException("Cannot deserialize client node "+uid,e);
        }
    }
}
