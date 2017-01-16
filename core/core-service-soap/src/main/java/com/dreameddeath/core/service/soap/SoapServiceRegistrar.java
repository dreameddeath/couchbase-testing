/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.service.soap;

import com.dreameddeath.core.java.utils.ClassUtils;
import com.dreameddeath.core.service.AbstractExposableService;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.registrar.AbstractServiceRegistrar;
import com.dreameddeath.core.service.soap.model.SoapCuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import com.google.common.base.Preconditions;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.UriSpec;

import javax.jws.WebService;

/**
 * Created by Christophe Jeunesse on 05/09/2016.
 */
public class SoapServiceRegistrar extends AbstractServiceRegistrar<SoapCuratorDiscoveryServiceDescription> {
    public SoapServiceRegistrar(CuratorFramework curatorClient, String domain) {
        super(curatorClient, domain, SoapServiceTypeHelper.SERVICE_TECH_TYPE);
    }

    @Override
    protected Class<SoapCuratorDiscoveryServiceDescription> getDescriptionClass() {
        return SoapCuratorDiscoveryServiceDescription.class;
    }

    @Override
    protected ServiceInstance<SoapCuratorDiscoveryServiceDescription> buildServiceInstanceDescription(AbstractExposableService service) {
        SoapCuratorDiscoveryServiceDescription description= initServiceInstanceDescription(service,new SoapCuratorDiscoveryServiceDescription());
        ServiceDef annotDef = service.getClass().getAnnotation(ServiceDef.class);
        String fullPath = (service.getEndPoint().path()+"/"+service.getAddress()).replaceAll("/{2,}","/");
        String uriStr = "{scheme}://{address}:{port}"+("/"+fullPath).replaceAll("/{2,}","/");
        UriSpec uriSpec = new UriSpec(uriStr);

        Class rootClass = ClassUtils.getClassWithAnnotation(service.getClass(),WebService.class);
        Preconditions.checkNotNull(rootClass,"The class %s doesn't have parent class with WebService annotation",service.getClass());
        description.setClassName(rootClass.getName());

        return new ServiceInstance<>(
                ServiceNamingUtils.buildServiceFullName(annotDef.type(),annotDef.name(),annotDef.version()),
                service.getId(),
                service.getEndPoint().host(),
                service.getEndPoint().port(),
                service.getEndPoint().securedPort(),
                description,
                System.currentTimeMillis(),
                ServiceType.PERMANENT,
                uriSpec
        );
    }
}
