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

package com.dreameddeath.core.service.soap;

import com.dreameddeath.core.service.discovery.AbstractServiceDiscoverer;
import com.dreameddeath.core.service.model.common.ServiceInstanceDescription;
import com.dreameddeath.core.service.soap.model.SoapCuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.soap.model.SoapServiceInstanceDescription;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;

/**
 * Created by Christophe Jeunesse on 05/09/2016.
 */
public class SoapServiceDiscoverer extends AbstractServiceDiscoverer<String,SoapCuratorDiscoveryServiceDescription> {
    public SoapServiceDiscoverer(CuratorFramework client, String domain) {
        super(client, domain, SoapServiceTypeHelper.SERVICE_TYPE);
    }

    @Override
    protected Class<SoapCuratorDiscoveryServiceDescription> getDescriptionClass() {
        return SoapCuratorDiscoveryServiceDescription.class;
    }

    @Override
    protected ServiceInstanceDescription buildInstanceDescription(ServiceInstance<SoapCuratorDiscoveryServiceDescription> instance) {
        SoapServiceInstanceDescription soapServiceInstanceDescription = new SoapServiceInstanceDescription(instance);

        return soapServiceInstanceDescription;
    }
}
