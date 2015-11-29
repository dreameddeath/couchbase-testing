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

package com.dreameddeath.infrastructure.daemon.services;

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.service.config.service.ConfigManagementService;

import javax.ws.rs.Path;

/**
 * Created by Christophe Jeunesse on 11/11/2015.
 */
public class RestLocalConfigAdminService {
    private final ConfigManagementService allConfigService;
    private final ConfigManagementService tempConfigService;
    private final ConfigManagementService persistentConfigService;

    public RestLocalConfigAdminService(){
        allConfigService = new ConfigManagementService();
        allConfigService.setConfig(ConfigManagerFactory.getConfig(ConfigManagerFactory.PriorityDomain.ALL));
        allConfigService.setReadOnly(true);
        tempConfigService = new ConfigManagementService();
        tempConfigService.setConfig(ConfigManagerFactory.getConfig(ConfigManagerFactory.PriorityDomain.LOCAL_TEMP_OVERRIDE));
        persistentConfigService = new ConfigManagementService();
        persistentConfigService.setConfig(ConfigManagerFactory.getConfig(ConfigManagerFactory.PriorityDomain.LOCAL_OVERRIDE));
    }

    @Path("/local-temporary")
    public ConfigManagementService getTempConfig(){
        return tempConfigService;
    }

    @Path("/local-persistent")
    public ConfigManagementService getPersistentConfig(){
        return persistentConfigService;
    }

    @Path("/all")
    public ConfigManagementService getAll(){
        return allConfigService;
    }

}
