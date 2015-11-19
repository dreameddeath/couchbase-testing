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

package com.dreameddeath.core.curator.discovery;

import com.dreameddeath.core.curator.model.IRegisterable;
import org.apache.curator.framework.CuratorFramework;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 26/10/2015.
 */
public interface ICuratorDiscovery<T extends IRegisterable> {
    List<T> getList();
    T get(String uid);
    void start() throws Exception;
    void stop() throws Exception;
    void refresh() throws Exception;
    void addListener(ICuratorDiscoveryListener<T> listener);
    CuratorFramework getClient();
}
