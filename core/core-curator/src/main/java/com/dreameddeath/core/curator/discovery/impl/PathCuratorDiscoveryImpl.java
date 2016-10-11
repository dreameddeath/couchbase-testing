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

package com.dreameddeath.core.curator.discovery.impl;

import com.dreameddeath.core.curator.discovery.path.ICuratorPathDiscovery;
import com.dreameddeath.core.curator.discovery.path.ICuratorPathDiscoveryLifeCycleListener;
import com.dreameddeath.core.curator.discovery.path.ICuratorPathDiscoveryListener;
import com.dreameddeath.core.curator.model.IRegistrablePathData;
import org.apache.curator.framework.CuratorFramework;

/**
 * Created by Christophe Jeunesse on 07/10/2016.
 */
public abstract class PathCuratorDiscoveryImpl<T extends IRegistrablePathData> extends AbstractGenericCuratorDiscoveryImpl<T,ICuratorPathDiscoveryListener<T>,ICuratorPathDiscoveryLifeCycleListener> implements ICuratorPathDiscovery<T> {
    public PathCuratorDiscoveryImpl(CuratorFramework curatorFramework, String basePath) {
        super(curatorFramework, basePath);
    }

    @Override
    protected String getUid(T data) {
        return data.uid();
    }
}
