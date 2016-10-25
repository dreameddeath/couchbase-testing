/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.service.client;

import com.dreameddeath.core.service.discovery.IServiceProviderSupplier;
import com.dreameddeath.core.service.model.common.CuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.utils.UriUtils;
import com.google.common.base.Preconditions;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 04/12/2015.
 */
public abstract class AbstractServiceClientImpl<T,TSPEC,TDESCR extends CuratorDiscoveryServiceDescription<TSPEC>> implements IServiceClient<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceClientImpl.class);


    private final IServiceProviderSupplier<TDESCR> providerSupplier;
    private final AbstractServiceClientFactory<? extends IServiceClient<T>,TSPEC,TDESCR> parentFactory;
    private final String fullName;
    private final UUID uuid=UUID.randomUUID();

    public AbstractServiceClientImpl(IServiceProviderSupplier<TDESCR> providerSupplier, String serviceFullName, AbstractServiceClientFactory<? extends IServiceClient<T>,TSPEC,TDESCR> factory){
        Preconditions.checkNotNull(providerSupplier,"The provider for service %s is null",serviceFullName);
        this.providerSupplier = providerSupplier;
        this.fullName = serviceFullName;
        this.parentFactory = factory;
    }

    protected abstract T buildClient(final ServiceInstance<TDESCR> instance);

    @Override
    public T getInstance(){
        try {
            ServiceInstance<TDESCR> instance = providerSupplier.getServiceProvider().getInstance();
            if(instance==null){
                throw new NoSuchElementException("No instance found");
            }
            return buildClient(instance);
        }
        catch(Exception e){
            LOG.error("Cannot get instance of service "+fullName,e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String  getUriInstance(){
        try {
            ServiceInstance<TDESCR> instance = providerSupplier.getServiceProvider().getInstance();
            if(instance==null){
                throw new RuntimeException("Cannot get instance of service <"+fullName+">");
            }
            return UriUtils.buildUri(instance,false);
        }
        catch(Exception e){
            LOG.error("Cannot get instance of service "+fullName,e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public UUID getUuid(){
        return uuid;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public T getInstance(String instanceId){
        try {
            for (ServiceInstance<TDESCR> instance : providerSupplier.getServiceProvider().getAllInstances()) {
                if (instance.getId().equals(instanceId)) {
                    return buildClient(instance);
                }
            }
        }
        catch(Exception e){
            LOG.error("Error during get instance <"+instanceId+"> of service "+fullName,e);
            throw new RuntimeException(e);
        }
        LOG.error("Cannot find instance <"+instanceId+"> of service "+fullName);
        throw new RuntimeException("Service instance not found");
    }

    public AbstractServiceClientFactory getParentFactory() {
        return parentFactory;
    }

}
