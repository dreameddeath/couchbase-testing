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

package com.dreameddeath.core.notification.registrar;

import com.dreameddeath.core.curator.registrar.impl.CuratorPathRegistrarImpl;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;

import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 16/08/2016.
 */
public class ListenerRegistrar extends CuratorPathRegistrarImpl<ListenerDescription> {
    private final ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();

    public ListenerRegistrar(CuratorFramework curatorFramework, String basePath) {
        super(curatorFramework, basePath);
    }

    @Override
    protected ListenerDescription deserialize(String uid, byte[] currentData) {
        try {
            return mapper.readValue(currentData, ListenerDescription.class);
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int compare(ListenerDescription currObj, ListenerDescription newObj) {
        return currObj.version().compareTo(newObj.version());
    }

    @Override
    protected byte[] serialize(ListenerDescription obj) throws Exception {
        return mapper.writeValueAsBytes(obj);
    }
}
