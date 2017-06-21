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

package com.dreameddeath.core.query.factory;

import com.dreameddeath.core.depinjection.IDependencyInjector;
import com.dreameddeath.core.java.utils.ClassUtils;
import com.dreameddeath.core.query.service.IQueryService;
import com.dreameddeath.core.query.service.remote.RemoteQueryService;
import com.dreameddeath.core.service.client.rest.IRestServiceClient;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 20/12/2016.
 */
public class QueryServiceFactory {
    private final Map<Class<?>,IQueryService<?>> queryServiceClassMap = new ConcurrentHashMap<>();
    private IRemoteQueryClientFactory remoteClientFactory;
    private IDependencyInjector dependencyInjector=null;

    @Autowired(required = false)
    public void setDependencyInjector(IDependencyInjector dependencyInjector){
        this.dependencyInjector=dependencyInjector;

    }

    @Autowired
    public void setRemoteClientFactory(IRemoteQueryClientFactory factory){
        this.remoteClientFactory = factory;
    }


    public <T extends IQueryService> T addQueryService(Class<T> queryServiceClass){
        Class objectClass = ClassUtils.getEffectiveGenericType(queryServiceClass,IQueryService.class,0);
        Preconditions.checkArgument(objectClass!=null,"Cannot get model class from class %s",queryServiceClass.getCanonicalName());
        T queryService = createService(queryServiceClass);
        return registerServiceMap(objectClass,queryService);
    }

    public <T extends IQueryService> T createService(Class<T> queryServiceClass){
        return dependencyInjector.getBeanOfType(queryServiceClass);
    }

    public final <T extends IQueryService> T registerServiceMap(Class<?> clazz,T service){
        T oldService = (T)queryServiceClassMap.putIfAbsent(clazz,service);
        return oldService!=null?oldService:service;
    }

    public <T> IQueryService<T> getQueryService(Class<T> clazz){
        IQueryService<T> queryService = (IQueryService<T>) queryServiceClassMap.get(clazz);
        if(queryService==null && remoteClientFactory!=null){
            IRestServiceClient restServiceClient = remoteClientFactory.getClient(clazz);
            if(restServiceClient!=null) {
                RemoteQueryService<T> remoteQueryService = new RemoteQueryService<>();
                remoteQueryService.setDtoModelClass(clazz);
                remoteQueryService.setRestServiceClient(restServiceClient);
                queryService = remoteQueryService;
            }
        }
        if(queryService==null){
            throw new RuntimeException("Cannot find query service for class "+clazz.getCanonicalName());
        }
        return queryService;
    }

    public void cleanup() {
        queryServiceClassMap.clear();
    }
}
