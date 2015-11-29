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

import com.dreameddeath.core.curator.utils.CuratorUtils;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.config.ServiceConfigProperties;
import com.dreameddeath.core.service.model.ServiceDomainDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christophe Jeunesse on 18/03/2015.
 */
public class ServiceNamingUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceNamingUtils.class);
    private final static String FULLNAME_SEPARATOR_CHAR ="#";

    public static String buildServiceFullName(String name,String version){
        return name+ FULLNAME_SEPARATOR_CHAR +version;
    }

    public static String getNameFromServiceFullName(String fullName){
        return fullName.substring(0,fullName.lastIndexOf(FULLNAME_SEPARATOR_CHAR));
    }

    public static String getVersionFromServiceFullName(String fullName){
        return fullName.substring(fullName.lastIndexOf(FULLNAME_SEPARATOR_CHAR));
    }


    public static void createBaseServiceName(CuratorFramework client,String basePath){
        try {
            if(!basePath.startsWith("/")){
                basePath = "/"+basePath;
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(basePath);
        }
        catch (KeeperException.NodeExistsException e){
            LOG.debug("Root node {} already existing",basePath);
        }
        catch(Exception e){
            throw new RuntimeException("Unexpected issue",e);
        }
    }

    public static String buildServiceDomain(CuratorFramework client,String domain){
        String basePath = ServiceConfigProperties.SERVICES_DISCOVERY_ROOT_PATH.get();
        if(!domain.matches("(?:\\w|\\.)+")){
            throw new IllegalArgumentException("The domain <"+domain+"> is not valid");
        }
        String fullPath = ("/"+basePath+"/"+domain).replaceAll("/{2,}","/");
        ServiceDomainDefinition definition = new ServiceDomainDefinition();
        definition.setCreationDate(DateTime.now());
        definition.setName(domain);
        definition.setFullPath(fullPath);
        definition.setDescription(ServiceConfigProperties.SERVICE_DOMAIN_DESCRIPTION.getProperty(domain).get());
        try {
            CuratorUtils.createPathIfNeeded(client,fullPath , () -> {
                try {
                    LOG.info("Path <{}> created for domain <{}>",fullPath,domain);
                    return ObjectMapperFactory.BASE_INSTANCE.getMapper().writeValueAsBytes(definition);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        catch(Exception e){
            throw new RuntimeException("Unexpected issue",e);
        }
        return fullPath;
    }
}
