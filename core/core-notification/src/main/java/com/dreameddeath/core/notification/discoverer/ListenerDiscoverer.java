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

package com.dreameddeath.core.notification.discoverer;

import com.dreameddeath.core.curator.discovery.impl.PathCuratorDiscoveryImpl;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;

import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class ListenerDiscoverer extends PathCuratorDiscoveryImpl<ListenerDescription> {
    private final ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();

    public ListenerDiscoverer(CuratorFramework curatorFramework, String basePath) {
        super(curatorFramework, basePath);
    }

    @Override
    protected ListenerDescription deserialize(String uid, byte[] element) {
        try {
            return mapper.readValue(element, ListenerDescription.class);
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
