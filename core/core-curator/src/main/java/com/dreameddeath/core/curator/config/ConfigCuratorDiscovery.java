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

package com.dreameddeath.core.curator.config;

import com.dreameddeath.core.curator.discovery.impl.CuratorDiscoveryImpl;
import com.dreameddeath.core.curator.model.SharedConfigDefinition;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 16/11/2015.
 */
public class ConfigCuratorDiscovery extends CuratorDiscoveryImpl<SharedConfigDefinition> {
    private static final Logger LOG= LoggerFactory.getLogger(ConfigCuratorDiscovery.class);
    private ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();

    public ConfigCuratorDiscovery(CuratorFramework curatorFramework, String basePath) {
        super(curatorFramework, basePath);
    }

    @Override
    protected SharedConfigDefinition deserialize(String uid, byte[] element) {
        try {
            return mapper.readValue(element, SharedConfigDefinition.class);
        }
        catch(IOException e){
            throw new RuntimeException("Cannot deserialize ",e);
        }
    }
}
