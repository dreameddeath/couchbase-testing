package com.dreameddeath.core.service.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by CEAJ8230 on 18/03/2015.
 */
public class ServiceNamingUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceNamingUtils.class);

    public static String buildServiceFullName(String name,String version){
        return name+"#"+version;
    }

    public static void createBaseServiceName(CuratorFramework client,String basePath){
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(basePath);
        }
        catch (KeeperException.NodeExistsException e){
            LOG.debug("Root node {} already existing",basePath);
        }
        catch(Exception e){
            throw new RuntimeException("Unexpected issue",e);
        }
    }
}
