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

import com.netflix.config.DynamicWatchedConfiguration;
import com.netflix.config.source.ZooKeeperUpdatableConfigurationSource;
import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Created by Christophe Jeunesse on 25/11/2015.
 */
public class ZookeeperUpdatableConfiguration extends AbstractConfiguration {
    private static Logger LOG = LoggerFactory.getLogger(ZookeeperUpdatableConfiguration.class);
    public DynamicWatchedConfiguration source;
    public ZooKeeperUpdatableConfigurationSource zkCache;

    public ZookeeperUpdatableConfiguration(ZooKeeperUpdatableConfigurationSource zkSource) {
        //super(source);
        this.zkCache = zkSource;
        this.source = new DynamicWatchedConfiguration(zkSource);
    }


    @Override
    protected void addPropertyDirect(String key, Object o) {
        try {
            zkCache.setProperty(key, o.toString());
        }
        catch(Exception e){
            LOG.error("Error during update of key {}",key);
            throw new RuntimeException("Error during update of key "+key,e);
        }
    }

    @Override
    protected void clearPropertyDirect(String key) {
        try{
            zkCache.clearProperty(key);
        }
        catch(Exception e){
            LOG.error("Error during delete of key {}",key);
            throw new RuntimeException("Error during delete of key "+key,e);
        }
    }


    @Override
    public boolean isEmpty() {
        return source.isEmpty();
    }

    @Override
    public boolean containsKey(String s) {
        return source.containsKey(s);
    }

    @Override
    public Object getProperty(String s) {
        return source.getProperty(s);
    }

    @Override
    public Iterator<String> getKeys() {
        return source.getKeys();
    }
}
