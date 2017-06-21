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

package com.dreameddeath.infrastructure.plugin.query;

import com.dreameddeath.core.query.annotation.RemoteQueryInfo;
import com.dreameddeath.core.query.factory.BaseRemoteQueryClientFactory;
import com.dreameddeath.core.query.factory.IRemoteQueryClientFactory;
import com.dreameddeath.core.service.client.rest.IRestServiceClient;
import com.dreameddeath.core.service.client.rest.RestServiceClientFactory;
import com.dreameddeath.core.service.utils.rest.RestServiceTypeHelper;
import com.dreameddeath.infrastructure.daemon.manager.ServiceDiscoveryManager;
import com.dreameddeath.infrastructure.plugin.query.config.InfrastructureQueryPluginConfigProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by Christophe Jeunesse on 13/03/2016.
 */
public class RemoteQueryServiceQueryClientFactoryWithManager implements IRemoteQueryClientFactory {
    private final ServiceDiscoveryManager manager;
    private final Map<String,IRemoteQueryClientFactory> clientFactoryMap = new ConcurrentHashMap<>();

    public RemoteQueryServiceQueryClientFactoryWithManager(ServiceDiscoveryManager manager){
        this.manager = manager;
    }

    private IRemoteQueryClientFactory setupFactory(String domain){
        try {
            BaseRemoteQueryClientFactory result = new BaseRemoteQueryClientFactory();
            result.setClientFactory(manager.getClientFactory(InfrastructureQueryPluginConfigProperties.REMOTE_SERVICE_FOR_DOMAIN.getProperty(domain).getMandatoryValue("Cannot find the service domain"), RestServiceTypeHelper.SERVICE_TECH_TYPE, RestServiceClientFactory.class));
            return result;
        }
        catch (Exception e){
            throw new RuntimeException("Error during lookup of clientFactory for domain "+domain);
        }
    }

    @Override
    public <T> IRestServiceClient getClient(Class<T> dtoModelClass) {
        RemoteQueryInfo annot = dtoModelClass.getAnnotation(RemoteQueryInfo.class);
        if(annot==null){
            throw new RuntimeException("Cannot find annot RemoteServiceInfo for class "+dtoModelClass.getName());
        }
        IRemoteQueryClientFactory factory = clientFactoryMap.computeIfAbsent(annot.domain(),this::setupFactory);
        return factory.getClient(dtoModelClass);
    }
}
