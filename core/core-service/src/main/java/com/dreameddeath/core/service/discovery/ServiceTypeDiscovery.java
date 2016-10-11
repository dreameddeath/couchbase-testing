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

package com.dreameddeath.core.service.discovery;

import com.dreameddeath.core.curator.discovery.impl.StandardCuratorDiscoveryImpl;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.model.common.ServiceTypeDefinition;
import com.dreameddeath.core.service.utils.ServiceObjectMapperConfigurator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 04/09/2016.
 */
public class ServiceTypeDiscovery extends StandardCuratorDiscoveryImpl<ServiceTypeDefinition> {
    private static final Logger LOG= LoggerFactory.getLogger(ServiceTypeDiscovery.class);
    private ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR);

    public ServiceTypeDiscovery(CuratorFramework curatorFramework, String basePath) {
        super(curatorFramework, basePath);
    }

    @Override
    protected ServiceTypeDefinition deserialize(String uid, byte[] element) {
        try {
            return mapper.readValue(element, ServiceTypeDefinition.class);
        }
        catch(IOException e){
            throw new RuntimeException("Cannot deserialize service type <"+uid+">",e);
        }
    }
}
