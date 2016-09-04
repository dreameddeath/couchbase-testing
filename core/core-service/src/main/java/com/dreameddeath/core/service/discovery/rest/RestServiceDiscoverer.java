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

package com.dreameddeath.core.service.discovery.rest;

import com.dreameddeath.core.service.discovery.AbstractServiceDiscoverer;
import com.dreameddeath.core.service.model.common.ServiceInstanceDescription;
import com.dreameddeath.core.service.model.rest.RestCuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.model.rest.RestServiceInstanceDescription;
import com.dreameddeath.core.service.utils.RestServiceTypeHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;

/**
 * Created by Christophe Jeunesse on 03/09/2016.
 */
public class RestServiceDiscoverer extends AbstractServiceDiscoverer<RestCuratorDiscoveryServiceDescription> {

    public RestServiceDiscoverer(CuratorFramework client, String domain) {
        super(client, domain, RestServiceTypeHelper.SERVICE_TYPE);
    }

    @Override
    protected Class<RestCuratorDiscoveryServiceDescription> getDescriptionClass() {
        return RestCuratorDiscoveryServiceDescription.class;
    }

    @Override
    protected ServiceInstanceDescription buildInstanceDescription(ServiceInstance<RestCuratorDiscoveryServiceDescription> instance) {
        return new RestServiceInstanceDescription(instance);
    }
}
