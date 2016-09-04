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

package com.dreameddeath.core.service.registrar;

import com.dreameddeath.core.curator.registrar.impl.CuratorRegistrarImpl;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.model.common.ClientInstanceInfo;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;

/**
 * Created by Christophe Jeunesse on 09/12/2015.
 */
public class ClientRegistrar extends CuratorRegistrarImpl<ClientInstanceInfo> {
    private final ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();

    private final String daemonUid;
    private final String webServerUid;
    private final String domain;
    private final String serviceType;

    public ClientRegistrar(CuratorFramework curatorFramework, String domain,String serviceType,String daemonUid,String webServerUid) {
        super(curatorFramework, ServiceNamingUtils.buildServiceDomainPathName(domain,serviceType, ServiceNamingUtils.DomainPathType.CLIENT));
        this.serviceType = serviceType;
        this.daemonUid = daemonUid;
        this.webServerUid = webServerUid;
        this.domain = domain;
    }

    @Override
    protected void preparePath() {
        ServiceNamingUtils.buildServiceDomainType(getCuratorFramework(),domain,serviceType);
        super.preparePath();
    }

    public void enrich(ClientInstanceInfo obj){
        obj.setDaemonUid(daemonUid);
        obj.setWebServerUid(webServerUid);
    }

    @Override
    protected byte[] serialize(ClientInstanceInfo obj) throws Exception {
        return mapper.writeValueAsBytes(obj);
    }
}
