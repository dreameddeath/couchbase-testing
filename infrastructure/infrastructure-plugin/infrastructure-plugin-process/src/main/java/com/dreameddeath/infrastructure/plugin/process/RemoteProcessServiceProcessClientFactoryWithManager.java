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

package com.dreameddeath.infrastructure.plugin.process;

import com.dreameddeath.core.java.utils.ClassUtils;
import com.dreameddeath.core.service.client.rest.IRestServiceClient;
import com.dreameddeath.core.service.client.rest.RestServiceClientFactory;
import com.dreameddeath.core.service.utils.rest.RestServiceTypeHelper;
import com.dreameddeath.couchbase.core.process.remote.RemoteJobTaskProcessing;
import com.dreameddeath.couchbase.core.process.remote.annotation.RemoteServiceInfo;
import com.dreameddeath.couchbase.core.process.remote.factory.BaseRemoteProcessClientFactory;
import com.dreameddeath.couchbase.core.process.remote.factory.IRemoteProcessClientFactory;
import com.dreameddeath.infrastructure.daemon.manager.ServiceDiscoveryManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dreameddeath.infrastructure.plugin.process.config.InfrastructureProcessPluginConfigProperties.REMOTE_SERVICE_FOR_DOMAIN;

/**
 * Created by Christophe Jeunesse on 13/03/2016.
 */
public class RemoteProcessServiceProcessClientFactoryWithManager implements IRemoteProcessClientFactory {
    private final ServiceDiscoveryManager manager;
    private final Map<String,IRemoteProcessClientFactory> clientFactoryMap = new ConcurrentHashMap<>();

    public RemoteProcessServiceProcessClientFactoryWithManager(ServiceDiscoveryManager manager){
        this.manager = manager;
    }

    private IRemoteProcessClientFactory setupFactory(String domain){
        try {
            BaseRemoteProcessClientFactory result = new BaseRemoteProcessClientFactory();
            result.setClientFactory(manager.getClientFactory(REMOTE_SERVICE_FOR_DOMAIN.getProperty(domain).getMandatoryValue("Cannot find the service domain"), RestServiceTypeHelper.SERVICE_TECH_TYPE, RestServiceClientFactory.class));
            return result;
        }
        catch (Exception e){
            throw new RuntimeException("Error during lookup of clientFactory for domain "+domain);
        }
    }

    @Override
    public IRestServiceClient getClient(RemoteJobTaskProcessing forProcessing) {
        RemoteServiceInfo annot = forProcessing.getClass().getAnnotation(RemoteServiceInfo.class);
        if (annot == null) {
            Class<?> requestClass = ClassUtils.getEffectiveGenericType(forProcessing.getClass(),RemoteJobTaskProcessing.class,0);
            annot = requestClass.getAnnotation(RemoteServiceInfo.class);
        }
        if(annot==null){
            throw new RuntimeException("Cannot find annot RemoteServiceInfo for class "+forProcessing.getClass().getName());
        }
        IRemoteProcessClientFactory factory = clientFactoryMap.computeIfAbsent(annot.domain(),this::setupFactory);
        return factory.getClient(forProcessing);
    }
}
