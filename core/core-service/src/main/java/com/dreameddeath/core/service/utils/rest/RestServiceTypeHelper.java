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

package com.dreameddeath.core.service.utils.rest;

import com.dreameddeath.core.service.client.rest.RestServiceClientFactory;
import com.dreameddeath.core.service.discovery.AbstractServiceDiscoverer;
import com.dreameddeath.core.service.discovery.rest.RestServiceDiscoverer;
import com.dreameddeath.core.service.model.common.CuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.model.rest.RestCuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.registrar.ClientRegistrar;
import com.dreameddeath.core.service.registrar.RestServiceRegistrar;
import com.dreameddeath.core.service.utils.IServiceTypeHelper;
import org.apache.curator.framework.CuratorFramework;

import javax.ws.rs.core.MediaType;

/**
 * Created by Christophe Jeunesse on 03/09/2016.
 */
public class RestServiceTypeHelper implements IServiceTypeHelper {
    public static final String SERVICE_TYPE = "REST";
    @Override
    public String getType() {
        return SERVICE_TYPE;
    }

    @Override
    public MediaType getSpecificationMediaType() {
        return MediaType.APPLICATION_JSON_TYPE;
    }

    @Override
    public Class<? extends CuratorDiscoveryServiceDescription> getCuratorServiceDescription() {
        return RestCuratorDiscoveryServiceDescription.class;
    }

    @Override
    public Class<? extends AbstractServiceDiscoverer> getServiceDiscoverer() {
        return RestServiceDiscoverer.class;
    }

    @Override
    public RestServiceDiscoverer buildDiscoverer(CuratorFramework client, String domain) {
        return new RestServiceDiscoverer(client,domain);
    }

    @Override
    public RestServiceClientFactory buildClientFactory(AbstractServiceDiscoverer serviceDiscoverer, ClientRegistrar registrar) {
        return new RestServiceClientFactory(serviceDiscoverer,registrar);
    }

    @Override
    public RestServiceClientFactory buildClientFactory(AbstractServiceDiscoverer serviceDiscoverer) {
        return new RestServiceClientFactory(serviceDiscoverer);
    }

    @Override
    public RestServiceRegistrar buildServiceRegistrar(CuratorFramework curatorClient, String domain) {
        return new RestServiceRegistrar(curatorClient,domain);
    }
}
