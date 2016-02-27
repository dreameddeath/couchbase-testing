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

import com.dreameddeath.core.process.service.IJobProcessingService;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.process.service.factory.impl.ProcessingServiceFactory;
import com.dreameddeath.couchbase.core.process.remote.RemoteJobTaskProcessing;

import javax.inject.Inject;

/**
 * Created by Christophe Jeunesse on 27/02/2016.
 */
public class ProcessingServiceWithRemoteCapabiltyFactory extends ProcessingServiceFactory {
    private IRemoteClientFactory clientFactory;

    @Inject
    public void setRemoteClientFactory(IRemoteClientFactory clientFactory){
        this.clientFactory = clientFactory;
    }

    @Override
    protected IJobProcessingService createJobProcessingService(Class<? extends IJobProcessingService> serviceClass) {
        return super.createJobProcessingService(serviceClass);
    }


    @Override
    protected ITaskProcessingService createTaskProcessingService(Class<? extends ITaskProcessingService> serviceClass) {
        ITaskProcessingService service = super.createTaskProcessingService(serviceClass);
        if(service instanceof RemoteJobTaskProcessing){
            ((RemoteJobTaskProcessing) service).setRemoteJobClientFactory(clientFactory);
        }
        return service;
    }
}
