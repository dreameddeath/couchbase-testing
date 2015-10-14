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

package com.dreameddeath.infrastructure.daemon.discovery;

import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.config.DaemonConfigProperties;
import com.dreameddeath.infrastructure.daemon.model.DaemonInfo;
import com.dreameddeath.infrastructure.daemon.utils.DaemonJacksonMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 17/09/2015.
 */
public class DaemonDiscovery implements IDaemonDiscovery {
    private final CuratorFramework curatorFramework;
    private PersistentEphemeralNode currDaemonNode = null;

    public DaemonDiscovery(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    private String getAndCreateRootPath() throws Exception {
        String path = DaemonConfigProperties.DAEMON_REGISTER_PATH.getMandatoryValue("The daemon registering path isn't set");
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (curatorFramework.checkExists().forPath(path) == null) {
            curatorFramework.create().creatingParentsIfNeeded().forPath(path);
        }
        return path;
    }


    private String getAndDaemonPath(AbstractDaemon daemon) throws Exception {
        return getAndCreateRootPath() + "/" + daemon.getUuid().toString();
    }

    private PersistentEphemeralNode setupNode(AbstractDaemon daemon) throws Exception {
        PersistentEphemeralNode node = new PersistentEphemeralNode(curatorFramework, PersistentEphemeralNode.Mode.EPHEMERAL, getAndDaemonPath(daemon), serializeDaemonInfo(daemon));
        node.start();
        node.waitForInitialCreate(1, TimeUnit.SECONDS);
        return node;
    }


    private byte[] serializeDaemonInfo(AbstractDaemon daemon) throws JsonProcessingException {
        DaemonInfo info = new DaemonInfo(daemon);
        return DaemonJacksonMapper.getInstance().writeValueAsBytes(info);
    }

    @Override
    synchronized public void register(AbstractDaemon daemon) throws Exception {
        currDaemonNode = setupNode(daemon);
    }

    @Override
    synchronized public void unregister(AbstractDaemon daemon) throws Exception {
        if (currDaemonNode != null) {
            currDaemonNode.close();
            currDaemonNode = null;
        }
    }

    @Override
    synchronized public void update(AbstractDaemon daemon) throws Exception {
        if (currDaemonNode == null) {
            currDaemonNode = setupNode(daemon);
        }
        else {
            currDaemonNode.setData(serializeDaemonInfo(daemon));
        }
    }

    @Override
    public List<DaemonInfo> registeredDaemonInfoList() throws Exception {
        String rootPath = getAndCreateRootPath();
        List<String> childrenPathList = curatorFramework.getChildren().forPath(rootPath);
        List<DaemonInfo> result = new ArrayList<>(childrenPathList.size());
        for (String childrenPath : childrenPathList) {
            byte[] childrenData = curatorFramework.getData().forPath(rootPath + "/" + childrenPath);
            DaemonInfo info = DaemonJacksonMapper.getInstance().readValue(childrenData, DaemonInfo.class);
            result.add(info);
        }
        return result;
    }

    @Override
    public DaemonInfo registeredDaemonInfo(String id) throws Exception {
        String rootPath = getAndCreateRootPath();
        byte[] childrenData = curatorFramework.getData().forPath(rootPath + "/" + id);
        return DaemonJacksonMapper.getInstance().readValue(childrenData, DaemonInfo.class);
    }
}