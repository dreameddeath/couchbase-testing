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

package com.dreameddeath.core.service.registrar;

import com.dreameddeath.core.service.AbstractExposableService;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.model.rest.RestCuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import com.dreameddeath.core.service.utils.rest.RestServiceTypeHelper;
import io.swagger.jaxrs.Reader;
import io.swagger.models.Swagger;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.UriSpec;

import javax.ws.rs.Path;


/**
 * Created by Christophe Jeunesse on 13/01/2015.
 */
public class RestServiceRegistrar extends AbstractServiceRegistrar<RestCuratorDiscoveryServiceDescription> {
    public RestServiceRegistrar(CuratorFramework curatorClient, String domain) {
        super(curatorClient, domain, RestServiceTypeHelper.SERVICE_TYPE);
    }

    @Override
    protected Class<RestCuratorDiscoveryServiceDescription> getDescriptionClass() {
        return RestCuratorDiscoveryServiceDescription.class;
    }

    @Override
    protected ServiceInstance<RestCuratorDiscoveryServiceDescription> buildServiceInstanceDescription(AbstractExposableService service){
        RestCuratorDiscoveryServiceDescription serviceDescr = new RestCuratorDiscoveryServiceDescription();
        super.initServiceInstanceDescription(service,serviceDescr);
        ServiceDef annotDef = service.getClass().getAnnotation(ServiceDef.class);
        Path pathAnnot = service.getClass().getAnnotation(Path.class);

        Swagger swagger = new Swagger();
        swagger.setBasePath((service.getEndPoint().path()+"/"+service.getAddress()+"/"+pathAnnot.value()).replaceAll("/{2,}","/"));
        swagger.setHost(service.getEndPoint().host());
        swagger.setVendorExtension("x-JavaServiceApiRootPath",pathAnnot.value());
        Reader reader=new Reader(swagger);
        reader.read(service.getClass());
        serviceDescr.setSwagger(reader.getSwagger());

        String uriStr = "{scheme}://{address}:{port}"+("/"+swagger.getBasePath()).replaceAll("/{2,}","/");
        UriSpec uriSpec = new UriSpec(uriStr);

        return new ServiceInstance<>(
                ServiceNamingUtils.buildServiceFullName(annotDef.name(),annotDef.version()),
                service.getId(),
                service.getEndPoint().host(),
                service.getEndPoint().port(),
                service.getEndPoint().securedPort(),
                serviceDescr,
                System.currentTimeMillis(),
                ServiceType.PERMANENT,
                uriSpec
        );
    }

}
