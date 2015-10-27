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

package com.dreameddeath.core.curator.registrar.impl;

import com.dreameddeath.core.curator.model.IRegisterable;
import com.dreameddeath.core.curator.registrar.ICuratorRegistrar;
import com.dreameddeath.core.curator.utils.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 26/10/2015.
 */
public abstract class CuratorRegistrarImpl<T extends IRegisterable> implements ICuratorRegistrar<T> {
    private final static Logger LOG = LoggerFactory.getLogger(CuratorRegistrarImpl.class);
    private final CuratorFramework curatorFramework;
    private final String basePath;
    private final Map<String,PersistentEphemeralNode> registered = new HashMap<>();

    public CuratorRegistrarImpl(CuratorFramework curatorFramework,String basePath) {
        this.curatorFramework = curatorFramework;
        this.basePath = basePath;
    }

    protected abstract byte[] serialize(T obj) throws Exception;

    private PersistentEphemeralNode setupNode(T obj) throws Exception {
        CuratorUtils.createPathIfNeeded(curatorFramework, basePath);
        PersistentEphemeralNode node = new PersistentEphemeralNode(curatorFramework, PersistentEphemeralNode.Mode.EPHEMERAL,
                CuratorUtils.buildPath(basePath,obj), serialize(obj));
        node.start();
        node.waitForInitialCreate(1, TimeUnit.SECONDS);
        return node;
    }

    @Override
    public synchronized final void close() {
        registered.values().forEach(node -> {
            try {
                node.close();
            } catch (IOException e) {
                LOG.error("Cannot close node "+node.getActualPath());
            }
        });
    }

    @Override
    public synchronized final void register(T obj) throws Exception{
        registered.put(obj.getUid(), setupNode(obj));
    }

    @Override
    public synchronized final void update(T obj) throws Exception {
        if (registered.containsKey(obj.getUid())) {
            registered.get(obj.getUid()).setData(serialize(obj));
        }
        else{
            registered.put(obj.getUid(),setupNode(obj));
        }
    }

    @Override
    public synchronized final void deregister(T obj) throws Exception{
        if(registered.containsKey(obj.getUid())){
            try {
                registered.get(obj.getUid()).close();
            }
            finally {
                registered.remove(obj.getUid());
            }
        }
    }

}
