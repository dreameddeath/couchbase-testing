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

package com.dreameddeath.core.service.client.rest;

import com.dreameddeath.core.service.client.AbstractServiceClientFactory;
import com.dreameddeath.core.service.discovery.AbstractServiceDiscoverer;
import com.dreameddeath.core.service.interceptor.rest.feature.ClientFeatureFactory;
import com.dreameddeath.core.service.model.rest.RestCuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.registrar.ClientRegistrar;
import io.swagger.models.Swagger;
import org.apache.curator.x.discovery.ServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Christophe Jeunesse on 03/09/2016.
 */
public class RestServiceClientFactory extends AbstractServiceClientFactory<IRestServiceClient,Swagger,RestCuratorDiscoveryServiceDescription> {
    private ClientFeatureFactory featureFactory=null;


    @Autowired(required = false)
    public void setFeatureFactory(ClientFeatureFactory featureFactory){
        this.featureFactory=featureFactory;
    }


    public RestServiceClientFactory(AbstractServiceDiscoverer serviceDiscoverer) {
        super(serviceDiscoverer);
    }

    public RestServiceClientFactory(AbstractServiceDiscoverer serviceDiscoverer, ClientRegistrar registrar) {
        super(serviceDiscoverer, registrar);
    }

    @Override
    protected IRestServiceClient buildClient(ServiceProvider<RestCuratorDiscoveryServiceDescription> provider, String serviceFullName) {
        return new RestServiceClientImpl(provider,serviceFullName,this);
    }

    public ClientFeatureFactory getFeatureFactory() {
        return featureFactory;
    }
}
