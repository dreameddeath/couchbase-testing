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

package com.dreameddeath.core.curator.config;

import com.dreameddeath.core.config.ConfigManagerFactory;
import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFoundException;
import com.dreameddeath.core.curator.model.SharedConfigDefinition;
import com.dreameddeath.core.curator.utils.CuratorUtils;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.config.DynamicWatchedConfiguration;
import com.netflix.config.source.ZooKeeperConfigurationSource;
import com.netflix.config.source.ZooKeeperUpdatableConfigurationSource;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.curator.framework.CuratorFramework;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christophe Jeunesse on 18/11/2015.
 */
public class SharedConfigurationUtils {
    private static Logger LOG= LoggerFactory.getLogger(SharedConfigurationUtils.class);

    public static String getRootPath() throws ConfigPropertyValueNotFoundException{
        return CuratorConfigProperties.CURATOR_SHARED_CONFIG_ROOT_PATH.getMandatoryValue("Cannot find the root path for shared config");
    }

    public static String buildPath(String name) throws ConfigPropertyValueNotFoundException{
        String relativePath = CuratorConfigProperties.CURATOR_SHARED_CONFIG_PATH_FOR_NAME.getProperty(name).getMandatoryValue("Cannot find the relative path for shared config name {}",name);
        return getRootPath()+"/"+relativePath;
    }

    public static void setupZookeeperConfigSource(CuratorFramework client, String name) throws Exception {
        CuratorUtils.createPathIfNeeded(client, getRootPath());
        final SharedConfigDefinition sharedDefinition = new SharedConfigDefinition();
        final String path =buildPath(name);

        sharedDefinition.setName(name);
        sharedDefinition.setCreationDate(DateTime.now());
        sharedDefinition.setFullPath(path);
        sharedDefinition.setDescription(CuratorConfigProperties.CURATOR_SHARED_CONFIG_DESCR_FOR_NAME.getProperty(name).get());
        CuratorUtils.createPathIfNeeded(client,path , () -> {
            try {
                LOG.info("Path <{}> created for shared config <{}>",path,name);
                return ObjectMapperFactory.BASE_INSTANCE.getMapper().writeValueAsBytes(sharedDefinition);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void registerZookeeperConfigSource(CuratorFramework client, String name) throws Exception {
        ConfigManagerFactory.addConfiguration(buildSharedConfiguration(client,name,false), "zk-" + name, ConfigManagerFactory.PriorityDomain.CENTRALIZED);
        LOG.info("Shared configuration {} registered",name);
    }


    private static AbstractConfiguration buildSharedConfigurationFromPath(CuratorFramework client, String path,boolean isUpdatable) throws Exception{
        path = path.replaceAll("/{2,}","/");
        ZooKeeperConfigurationSource zkConfigSource;
        AbstractConfiguration result;
        if(isUpdatable){
            zkConfigSource = new ZooKeeperUpdatableConfigurationSource(client, path);
            zkConfigSource.start();
            result = new ZookeeperUpdatableConfiguration((ZooKeeperUpdatableConfigurationSource)zkConfigSource);
        }
        else{
            zkConfigSource = new ZooKeeperConfigurationSource(client, path);
            zkConfigSource.start();
            result = new DynamicWatchedConfiguration(zkConfigSource);
        }

        return result;
    }

    public static AbstractConfiguration buildSharedConfiguration(CuratorFramework client, String name,boolean isUpdatable) throws Exception{
        return buildSharedConfigurationFromPath(client, buildPath(name),isUpdatable);
    }

    public static AbstractConfiguration buildSharedConfiguration(CuratorFramework client, SharedConfigDefinition config,boolean isUpdatable) throws Exception{
        return buildSharedConfigurationFromPath(client,config.getFullPath(),isUpdatable);
    }
}
