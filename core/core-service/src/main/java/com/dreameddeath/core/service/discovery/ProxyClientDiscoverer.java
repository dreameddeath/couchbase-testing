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
import com.dreameddeath.core.service.model.ProxyClientInstanceInfo;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 17/12/2015.
 */
public class ProxyClientDiscoverer extends AbstractClientDiscoverer<ProxyClientInstanceInfo> {
    private final static Logger LOG = LoggerFactory.getLogger(ProxyClientDiscoverer.class);

    private final ObjectMapper mapper= ObjectMapperFactory.BASE_INSTANCE.getMapper(BaseObjectMapperConfigurator.BASE_TYPE);

    public ProxyClientDiscoverer(final CuratorFramework curatorFramework, final String domain) {
        super(curatorFramework,domain, ServiceNamingUtils.DomainPathType.PROXY);
    }


    @Override
    protected ProxyClientInstanceInfo deserialize(String uid, byte[] element) {
        try {
            return mapper.readValue(element, ProxyClientInstanceInfo.class);
        }
        catch(IOException e){
            LOG.error("Cannot deserialize client node "+uid,e);
            throw new RuntimeException("Cannot deserialize client node "+uid,e);
        }
    }
}
