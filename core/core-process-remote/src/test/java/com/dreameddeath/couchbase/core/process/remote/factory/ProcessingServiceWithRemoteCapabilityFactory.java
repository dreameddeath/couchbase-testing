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

package com.dreameddeath.couchbase.core.process.remote.factory;

import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.service.IJobProcessingService;
import com.dreameddeath.core.process.service.ITaskProcessingService;
import com.dreameddeath.core.process.service.factory.impl.ProcessingServiceFactory;
import com.dreameddeath.couchbase.core.process.remote.RemoteJobTaskProcessing;
import com.google.common.base.Preconditions;

import javax.inject.Inject;

/**
 * Created by Christophe Jeunesse on 27/02/2016.
 */
public class ProcessingServiceWithRemoteCapabilityFactory extends ProcessingServiceFactory {
    private IRemoteProcessClientFactory clientFactory;

    @Inject
    public void setRemoteClientFactory(IRemoteProcessClientFactory clientFactory){
        this.clientFactory = clientFactory;
    }

    @Override
    protected <T extends IJobProcessingService<? extends AbstractJob>> T  createJobProcessingService(Class<T> serviceClass) {
        return super.createJobProcessingService(serviceClass);
    }


    @Override
    protected <TJOB extends AbstractJob,TTASK extends AbstractTask,T extends ITaskProcessingService<TJOB,TTASK>> T createTaskProcessingService(Class<T> serviceClass){
        T service = super.createTaskProcessingService(serviceClass);
        if(service instanceof RemoteJobTaskProcessing){
            Preconditions.checkNotNull(clientFactory,"The client factory must be defined prior to the initialization of the Processing service {}",serviceClass.getName());
            ((RemoteJobTaskProcessing) service).setRemoteJobClientFactory(clientFactory);
        }
        return service;
    }
}
