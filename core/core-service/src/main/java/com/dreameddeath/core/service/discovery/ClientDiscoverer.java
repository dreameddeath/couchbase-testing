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

import com.dreameddeath.core.json.BaseObjectMapperConfigurator;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.model.common.ClientInstanceInfo;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 09/12/2015.
 */
public class ClientDiscoverer extends AbstractClientDiscoverer<ClientInstanceInfo> {
    private final static Logger LOG = LoggerFactory.getLogger(ClientDiscoverer.class);

    private final ObjectMapper mapper= ObjectMapperFactory.BASE_INSTANCE.getMapper(BaseObjectMapperConfigurator.BASE_TYPE);

    public ClientDiscoverer(final CuratorFramework curatorFramework, final String domain,final String type) {
        super(curatorFramework,domain,type, ServiceNamingUtils.DomainPathType.CLIENT);
    }


    @Override
    protected ClientInstanceInfo deserialize(String uid, byte[] element) {
        try {
            return mapper.readValue(element, ClientInstanceInfo.class);
        }
        catch(IOException e){
            LOG.error("Cannot deserialize client node "+uid,e);
            throw new RuntimeException("Cannot deserialize client node "+uid,e);
        }
    }
}
