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

package com.dreameddeath.core.curator.discovery.path;

import com.dreameddeath.core.curator.discovery.generic.IGenericCuratorDiscovery;
import com.dreameddeath.core.curator.model.IRegistrablePathData;

/**
 * Created by Christophe Jeunesse on 06/10/2016.
 */
public interface ICuratorPathDiscovery<T extends IRegistrablePathData> extends IGenericCuratorDiscovery<T,ICuratorPathDiscoveryListener<T>,ICuratorPathDiscoveryLifeCycleListener> {
}
