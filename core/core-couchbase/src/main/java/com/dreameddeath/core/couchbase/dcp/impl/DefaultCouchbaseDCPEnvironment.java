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

package com.dreameddeath.core.couchbase.dcp.impl;

import com.couchbase.client.core.env.DefaultCoreEnvironment;
import com.couchbase.client.java.CouchbaseCluster;
import com.dreameddeath.core.couchbase.dcp.ICouchbaseDCPEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by Christophe Jeunesse on 27/05/2015.
 */
public class DefaultCouchbaseDCPEnvironment extends DefaultCoreEnvironment implements ICouchbaseDCPEnvironment {
    private static Logger LOG = LoggerFactory.getLogger(DefaultCouchbaseDCPEnvironment.class);

    public static String SDK_PACKAGE_NAME_AND_VERSION = "couchbase-dcp-connector";
    private static final String VERSION_PROPERTIES = "com.dreameddeath.core.couchbase.properties";
    private static final int DEFAULT_EVENT_BUFFER_SIZE = 16384;
    private static final int DEFAULT_THREAD_POOL_SIZE = 2;
    private static final String DEFAULT_THREAD_POOL_NAME = "dcp-connector-pool";

    static {
        try {
            Class<CouchbaseCluster> facadeClass = CouchbaseCluster.class;
            if (facadeClass == null) {
                throw new IllegalStateException("Could not locate ClusterFacade");
            }

            String version = null;
            String gitVersion = null;
            try {
                Properties versionProp = new Properties();
                versionProp.load(DefaultCoreEnvironment.class.getClassLoader().getResourceAsStream(VERSION_PROPERTIES));
                version = versionProp.getProperty("specificationVersion");
                gitVersion = versionProp.getProperty("implementationVersion");
            } catch (Exception e) {
                LOG.info("Could not retrieve version properties, defaulting.", e);
            }
            SDK_PACKAGE_NAME_AND_VERSION = String.format("couchbase-dcp-connector/%s (git: %s)",
                    version == null ? "unknown" : version, gitVersion == null ? "unknown" : gitVersion);

            //this will overwrite the USER_AGENT in Core
            // making core send user_agent with java client version information
            USER_AGENT = String.format("%s (%s/%s %s; %s %s)",
                    SDK_PACKAGE_NAME_AND_VERSION,
                    System.getProperty("os.name"),
                    System.getProperty("os.version"),
                    System.getProperty("os.arch"),
                    System.getProperty("java.vm.name"),
                    System.getProperty("java.runtime.version")
            );
        } catch (Exception ex) {
            LOG.info("Could not set up user agent and packages, defaulting.", ex);
        }
    }


    private final Integer threadPoolSize;
    private final String threadPoolName;
    private final Integer eventBufferSize;
    private final String streamName;

    protected DefaultCouchbaseDCPEnvironment(Builder builder){
        super(builder);
        threadPoolName = builder.threadPoolName();
        threadPoolSize = builder.threadPoolSize();
        eventBufferSize= builder.eventBufferSize();
        streamName = builder.streamName();
    }

    @Override
    public Integer threadPoolSize() {
        return threadPoolSize;
    }

    @Override
    public String threadPoolName() {
        return threadPoolName;
    }

    @Override
    public Integer eventBufferSize() {
        return eventBufferSize;
    }

    @Override
    public String streamName() {
        return streamName;
    }

    public static DefaultCouchbaseDCPEnvironment.Builder builder(){
        return new DefaultCouchbaseDCPEnvironment.Builder();
    }

    public static class Builder extends DefaultCoreEnvironment.Builder{
        private Integer threadPoolSize=DEFAULT_THREAD_POOL_SIZE;
        private String threadPoolName=DEFAULT_THREAD_POOL_NAME;
        private Integer eventBufferSize=DEFAULT_EVENT_BUFFER_SIZE;
        private String streamName;

        public Builder(){
            dcpEnabled(true);
        }

        public Builder threadPoolSize(Integer size){
            threadPoolSize = size;
            return this;
        }

        public Builder threadPoolName(String name){
            threadPoolName = name;
            return this;
        }

        public Builder eventBufferSize(Integer size){
            eventBufferSize = size;
            return this;
        }

        public Builder streamName(String name){
            streamName = name;
            return this;
        }

        public Integer threadPoolSize() {
            return threadPoolSize;
        }

        public String threadPoolName() {
            return threadPoolName;
        }

        public Integer eventBufferSize() {
            return eventBufferSize;
        }


        public String streamName() {
            return streamName;
        }

        @Override
        public DefaultCouchbaseDCPEnvironment build() {
            if(this.streamName()==null){
                throw new IllegalArgumentException("The stream name should be set");
            }
            return new DefaultCouchbaseDCPEnvironment(this);
        }
    }
}
