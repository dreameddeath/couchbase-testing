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

package com.dreameddeath.couchbase.core.process.remote.factory;

import com.dreameddeath.core.java.utils.ClassUtils;
import com.dreameddeath.core.service.client.IServiceClient;
import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.couchbase.core.process.remote.RemoteJobTaskProcessing;
import com.dreameddeath.couchbase.core.process.remote.annotation.RemoteServiceInfo;

/**
 * Created by Christophe Jeunesse on 26/02/2016.
 */
public class BaseRemoteClientFactory implements IRemoteClientFactory {
    private ServiceClientFactory clientFactory;

    public void setClientFactory(ServiceClientFactory clientFactory){
        this.clientFactory = clientFactory;
    }

    @Override
    public IServiceClient getClient(RemoteJobTaskProcessing forProcessing) {
        RemoteServiceInfo annot = forProcessing.getClass().getAnnotation(RemoteServiceInfo.class);
        if (annot == null) {
            Class<?> requestClass = ClassUtils.getEffectiveGenericType(forProcessing.getClass(),RemoteJobTaskProcessing.class,0);
            annot = requestClass.getAnnotation(RemoteServiceInfo.class);
        }
        if(annot==null){
            throw new RuntimeException("Cannot find annot RemoteServiceInfo for class "+forProcessing.getClass().getName());
        }
        return clientFactory.getClient(annot.name(),annot.version());
    }
}
