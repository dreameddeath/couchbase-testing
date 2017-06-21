/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.service.discovery;

import com.dreameddeath.core.curator.discovery.impl.StandardCuratorDiscoveryImpl;
import com.dreameddeath.core.curator.discovery.standard.ICuratorDiscoveryListener;
import com.dreameddeath.core.service.model.common.AbstractClientInstanceInfo;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Christophe Jeunesse on 17/12/2015.
 */
public abstract class AbstractClientDiscoverer<T extends AbstractClientInstanceInfo> extends StandardCuratorDiscoveryImpl<T> {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractClientDiscoverer.class);

    private final String domain;
    private final String serviceType;
    private final ConcurrentMap<String,Set<T>> clientInstances = new ConcurrentHashMap<>();

    private static Set buildSet(String serviceName){
        return Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public AbstractClientDiscoverer(final CuratorFramework curatorFramework, final String domain,String serviceType,ServiceNamingUtils.DomainPathType subType) {
        super(curatorFramework, ServiceNamingUtils.buildServiceDomainPathName(domain,serviceType, subType));
        this.serviceType = serviceType;
        this.domain = domain;
        addListener(new ICuratorDiscoveryListener<T>() {
            @Override
            public void onRegister(String uid, T obj) {
                LOG.info("Registering discovered client {} of name {}",uid,obj.getServiceName());
                Set<T> clientInstanceInfoSet = clientInstances.computeIfAbsent(obj.getServiceName(), AbstractClientDiscoverer::buildSet);
                clientInstanceInfoSet.add(obj);
            }

            @Override
            public void onUnregister(String uid, T oldObj) {
                LOG.info("UnRegistering discovered client {} of name {}",uid,oldObj.getServiceName());
                Set<T> clientInstanceInfoSet = clientInstances.get(oldObj.getServiceName());
                if(clientInstanceInfoSet!=null) {
                    clientInstanceInfoSet.remove(oldObj);
                }
            }

            @Override
            public void onUpdate(String uid, T oldObj, T newObj) {
                LOG.info("UnRegistering discovered client {} of name {}",uid,oldObj.getServiceName());
                Set<T> oldClientInstanceInfoSet = clientInstances.get(oldObj.getServiceName());
                if(oldClientInstanceInfoSet!=null) {
                    oldClientInstanceInfoSet.remove(oldObj);
                }

                Set<T> clientInstanceInfoSet = clientInstances.computeIfAbsent(newObj.getServiceName(), AbstractClientDiscoverer::buildSet);
                clientInstanceInfoSet.add(newObj);
            }
        });
    }

    @Override
    protected void preparePath() {
        ServiceNamingUtils.buildServiceDiscovererDomain(getClient(),domain,serviceType);
        super.preparePath();
    }

    public long getNbInstances(String functionnalType,String serviceName, String version) {
        return getNbInstances(ServiceNamingUtils.buildServiceFullName(functionnalType,serviceName,version));
    }

    public long getNbInstances(String serviceFullName){
        Set<T> list = clientInstances.get(serviceFullName);
        if(list!=null){
            return (long)list.size();
        }
        else{
            return 0L;
        }
    }

    public List<T> getInstances(String functionnalType,String serviceName,String version){
        return getInstances(ServiceNamingUtils.buildServiceFullName(functionnalType,serviceName,version));
    }

    public List<T> getInstances(String serviceFullName){
        Set<T> list = clientInstances.get(serviceFullName);
        if(list!=null){
            return new ArrayList<>(list);
        }
        else{
            return Collections.emptyList();
        }
    }

    public List<T> getInstances(){
        List<T> result = new ArrayList<>();
        clientInstances.values().forEach(result::addAll);
        return result;
    }

}

