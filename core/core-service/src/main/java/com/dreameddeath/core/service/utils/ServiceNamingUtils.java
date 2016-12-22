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

package com.dreameddeath.core.service.utils;

import com.dreameddeath.core.curator.utils.CuratorUtils;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.service.config.ServiceConfigProperties;
import com.dreameddeath.core.service.model.common.CuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.model.common.ServiceDescription;
import com.dreameddeath.core.service.model.common.ServiceDomainDefinition;
import com.dreameddeath.core.service.model.common.ServiceTypeDefinition;
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

    public enum DomainPathType{
        ROOT,
        SERVICE_TYPE,
        SERVER,
        PROXY,
        CLIENT
    }



    public static String buildServiceDomainPathName(String domain,String serviceType,DomainPathType pathType) {
        String basePath = ServiceConfigProperties.SERVICES_DISCOVERY_ROOT_PATH.get();
        if(!domain.matches("(?:\\w|\\.)+")){
            throw new IllegalArgumentException("The domain <"+domain+"> is not valid");
        }

        String subPath="";
        if(pathType.equals(DomainPathType.SERVER)){
            subPath="/servers";
        }
        else if(pathType.equals(DomainPathType.CLIENT)){
            subPath="/clients";
        }
        else if(pathType.equals(DomainPathType.PROXY)){
            subPath="/proxys";
        }
        if(DomainPathType.ROOT.equals(pathType)){
            serviceType = "";
        }
        else{
            serviceType="/"+serviceType;
        }
        return ("/"+basePath+"/"+domain+serviceType+subPath).replaceAll("/{2,}","/");

    }

    public static String buildServiceDomainType(CuratorFramework client,String domain,String type) {
        buildServiceDomain(client,domain);
        String fullPath = buildServiceDomainPathName(domain, type, DomainPathType.SERVICE_TYPE);
        ServiceTypeDefinition serviceTypeDefinition= new ServiceTypeDefinition();
        serviceTypeDefinition.setType(type);
        serviceTypeDefinition.setDescription(ServiceConfigProperties.SERVICE_TYPE_DESCRIPTION.getProperty(domain).get());
        try {
            CuratorUtils.createPathIfNeeded(client,fullPath , () -> {
                try {
                    LOG.info("Path <{}> created for domain <{}>",fullPath,domain);
                    return ObjectMapperFactory.BASE_INSTANCE.getMapper().writeValueAsBytes(serviceTypeDefinition);
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

    public static String buildServiceDomain(CuratorFramework client,String domain){
        String fullPath = buildServiceDomainPathName(domain,"",DomainPathType.ROOT);
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


    public static String buildServiceDiscovererDomain(CuratorFramework client,String domain,String type){
        buildServiceDomainType(client,domain,type);
        return buildServiceDomainPathName(domain,type,DomainPathType.SERVER);
    }

    public static String buildClientDiscovererDomain(CuratorFramework client,String domain,String type){
        buildServiceDomainType(client,domain,type);
        return buildServiceDomainPathName(domain,type,DomainPathType.CLIENT);
    }

    public static String buildServerServicePath(CuratorFramework client, String registrarDomain, CuratorDiscoveryServiceDescription service){
        String domainPathName = buildServiceDomainPathName(registrarDomain,service.getServiceType(),DomainPathType.SERVER);
        String serviceFullName = ServiceNamingUtils.buildServiceFullName(service.getName(),service.getVersion());
        String fullPath = (domainPathName+"/"+serviceFullName).replaceAll("/{2,}","/");
        ServiceDescription definition = new ServiceDescription();
        definition.setCreationDate(DateTime.now());
        definition.setName(service.getName());
        definition.setVersion(service.getVersion());
        definition.setFullName(serviceFullName);
        definition.setDescription(ServiceConfigProperties.SERVICE_INSTANCE_DESCRIPTION.getProperty(service.getName(),service.getVersion()).get());
        try {
            CuratorUtils.createPathIfNeeded(client,fullPath , () -> {
                try {
                    LOG.info("Path <{}> created for service <{}/{}> in domain {}",fullPath,service.getName(),service.getVersion(),service.getDomain());
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
