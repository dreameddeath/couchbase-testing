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

import com.dreameddeath.core.curator.discovery.impl.CuratorDiscoveryImpl;
import com.dreameddeath.infrastructure.daemon.model.DaemonInfo;
import com.dreameddeath.infrastructure.daemon.registrar.DaemonRegistrar;
import com.dreameddeath.infrastructure.daemon.utils.DaemonJacksonMapper;
import org.apache.curator.framework.CuratorFramework;

import java.io.IOException;

/**
 * Created by Christophe Jeunesse on 17/09/2015.
 */
public class DaemonDiscovery extends CuratorDiscoveryImpl<DaemonInfo> {
    //private final CuratorFramework curatorFramework;

    public DaemonDiscovery(CuratorFramework curatorFramework) {
        super(curatorFramework, DaemonRegistrar.getRootPath());
    }

    @Override
    protected DaemonInfo deserialize(String uid, byte[] element) {
        try {
            return DaemonJacksonMapper.getInstance().readValue(element, DaemonInfo.class);
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}