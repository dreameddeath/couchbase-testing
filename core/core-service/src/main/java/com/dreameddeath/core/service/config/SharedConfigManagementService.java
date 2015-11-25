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

package com.dreameddeath.core.service.config;

import com.dreameddeath.core.curator.config.ConfigCuratorDiscovery;
import com.dreameddeath.core.curator.config.SharedConfigurationUtils;
import com.dreameddeath.core.curator.discovery.ICuratorDiscoveryListener;
import com.dreameddeath.core.curator.model.SharedConfigDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 18/11/2015.
 */
@Path("/shared-config")
public class SharedConfigManagementService {
    private static final Logger LOG= LoggerFactory.getLogger(SharedConfigManagementService.class);
    private ConfigCuratorDiscovery discovery;
    final private Map<String,ConfigManagementService> sharedConfigServices=new ConcurrentHashMap<>();

    @Autowired
    public void setDiscovery(ConfigCuratorDiscovery discovery){
        this.discovery=discovery;
        this.discovery.addListener(new ICuratorDiscoveryListener<SharedConfigDefinition>() {
            @Override
            public void onRegister(String uid, SharedConfigDefinition obj) {
                try {
                    ConfigManagementService service = new ConfigManagementService();
                    service.setConfig(SharedConfigurationUtils.buildSharedConfiguration(discovery.getClient(), obj,true));
                    sharedConfigServices.put(uid,service);
                    LOG.info("Registering shared config <{}>",uid);
                }
                catch(Exception e){
                    LOG.error("Error while registering shared config <"+uid+">",e);
                    ///TODO log error
                }
            }

            @Override
            public void onUnregister(String uid, SharedConfigDefinition oldObj) {
                LOG.info("Removing shared config <{}>",uid);
                sharedConfigServices.remove(uid);
            }

            @Override
            public void onUpdate(String uid, SharedConfigDefinition obj, SharedConfigDefinition newObj) {
                //Nothing to do
            }
        });
    }

    @Path("/")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<String> get(){
        List<String> result = new ArrayList<>(sharedConfigServices.keySet());
        Collections.sort(result);
        return result;
    }

    @Path("/{name}")
    public ConfigManagementService getSharedConfig(@PathParam("name")String sharedName){
        ConfigManagementService service = sharedConfigServices.get(sharedName);
        if(service==null){
            throw new NotFoundException("Cannot find shared config name <"+sharedName+">");
        }
        return service;
    }
}
