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

package com.dreameddeath.core.service.discovery;

import com.dreameddeath.core.curator.discovery.impl.CuratorDiscoveryImpl;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.config.ServiceConfigProperties;
import com.dreameddeath.core.service.model.common.ServiceDomainDefinition;
import com.dreameddeath.core.service.utils.ServiceObjectMapperConfigurator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 26/11/2015.
 */
public class ServiceDomainDiscovery extends CuratorDiscoveryImpl<ServiceDomainDefinition> {
    private static final Logger LOG= LoggerFactory.getLogger(ServiceDomainDiscovery.class);
    private ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR);

    public ServiceDomainDiscovery(CuratorFramework curatorFramework) {
        super(curatorFramework, ServiceConfigProperties.SERVICES_DISCOVERY_ROOT_PATH.get());
    }

    @Override
    protected ServiceDomainDefinition deserialize(String uid, byte[] element) {
        try {
            return mapper.readValue(element, ServiceDomainDefinition.class);
        }
        catch(IOException e){
            throw new RuntimeException("Cannot deserialize domain <"+uid+">",e);
        }
    }


}
