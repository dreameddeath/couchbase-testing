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

package com.dreameddeath.core.service.utils;

import com.dreameddeath.core.service.client.AbstractServiceClientFactory;
import com.dreameddeath.core.service.discovery.AbstractServiceDiscoverer;
import com.dreameddeath.core.service.model.common.CuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.registrar.AbstractServiceRegistrar;
import com.dreameddeath.core.service.registrar.ClientRegistrar;
import org.apache.curator.framework.CuratorFramework;

import javax.ws.rs.core.MediaType;

/**
 * Created by Christophe Jeunesse on 03/09/2016.
 */
public interface IServiceTypeHelper {
    String getType();
    MediaType getSpecificationMediaType();
    Class<? extends CuratorDiscoveryServiceDescription> getCuratorServiceDescription();
    Class<? extends AbstractServiceDiscoverer> getServiceDiscoverer();
    <T extends AbstractServiceDiscoverer> T buildDiscoverer(CuratorFramework client,String domain);
    <T extends AbstractServiceClientFactory> T buildClientFactory(AbstractServiceDiscoverer serviceDiscoverer, ClientRegistrar registrar);
    <T extends AbstractServiceClientFactory> T buildClientFactory(AbstractServiceDiscoverer serviceDiscoverer);

    <T extends AbstractServiceRegistrar> T buildServiceRegistrar(CuratorFramework curatorClient, String domain);
}
