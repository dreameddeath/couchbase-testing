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

package com.dreameddeath.core.service.utils;

import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.model.common.CuratorDiscoveryServiceDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceSerializer;

/**
 * Created by Christophe Jeunesse on 31/03/2015.
 */
public class ServiceInstanceSerializerImpl implements  InstanceSerializer<CuratorDiscoveryServiceDescription<?>> {
    private final ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR);
    //private final JavaType javaType = mapper.getTypeFactory().constructParametricType(ServiceInstance.class,CuratorDiscoveryServiceDescription.class);

    @Override
    public byte[] serialize(ServiceInstance<CuratorDiscoveryServiceDescription<?>> serviceInstance) throws Exception {
        return mapper/*.writerFor(javaType)*/.writeValueAsBytes(serviceInstance);
    }

    @Override
    public ServiceInstance<CuratorDiscoveryServiceDescription<?>> deserialize(byte[] bytes) throws Exception {
        return mapper.readValue(bytes, ServiceInstance.class);
    }
}
