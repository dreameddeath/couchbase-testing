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

package com.dreameddeath.core.service.utils;

import com.dreameddeath.core.service.model.ServiceDescription;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceSerializer;

/**
 * Created by Christophe Jeunesse on 31/03/2015.
 */
public class ServiceInstanceSerializerImpl implements  InstanceSerializer<ServiceDescription> {
    private ServiceInstanceJacksonMapper _MAPPER = ServiceInstanceJacksonMapper.getInstance();


    @Override
    public byte[] serialize(ServiceInstance<ServiceDescription> serviceInstance) throws Exception {
        return _MAPPER.writeValueAsBytes(serviceInstance);
    }

    @Override
    public ServiceInstance<ServiceDescription> deserialize(byte[] bytes) throws Exception {
        return _MAPPER.readValue(bytes, new TypeReference<ServiceInstance<ServiceDescription>>(){});
    }
}
