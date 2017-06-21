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

import com.dreameddeath.core.query.annotation.RemoteQueryInfo;
import com.dreameddeath.core.query.service.rest.AbstractRestQueryService;
import com.dreameddeath.core.service.client.rest.IRestServiceClient;
import com.dreameddeath.core.service.client.rest.RestServiceClientFactory;

/**
 * Created by Christophe Jeunesse on 26/02/2016.
 */
public class BaseRemoteQueryClientFactory implements IRemoteQueryClientFactory {
    private RestServiceClientFactory clientFactory;

    public void setClientFactory(RestServiceClientFactory clientFactory){
        this.clientFactory = clientFactory;
    }

    @Override
    public <T> IRestServiceClient getClient(Class<T> dtoModelClass) {
        RemoteQueryInfo annot = dtoModelClass.getAnnotation(RemoteQueryInfo.class);
        if(annot==null){
            throw new RuntimeException("Cannot find annot RemoteServiceInfo for class "+dtoModelClass.getName());
        }
        return clientFactory.getClient(AbstractRestQueryService.SERVICE_TYPE,annot.name(),annot.version());
    }
}
