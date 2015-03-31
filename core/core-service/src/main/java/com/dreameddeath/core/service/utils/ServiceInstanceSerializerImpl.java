package com.dreameddeath.core.service.utils;

import com.dreameddeath.core.service.model.ServiceDescription;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceSerializer;

/**
 * Created by ceaj8230 on 31/03/2015.
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
