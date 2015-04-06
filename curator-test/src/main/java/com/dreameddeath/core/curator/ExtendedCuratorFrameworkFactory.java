/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorTempFramework;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.CompressionProvider;
import org.apache.curator.utils.ZookeeperFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by CEAJ8230 on 19/01/2015.
 */
public class ExtendedCuratorFrameworkFactory {
    public static ExtendedCuratorFrameworkFactory.Builder builder() {
        return new ExtendedCuratorFrameworkFactory.Builder();
    }

    public static ExtendedCuratorFrameworkFactory.Builder builder(String nameSpacePrefix) {
        return new ExtendedCuratorFrameworkFactory.Builder(nameSpacePrefix);
    }

    public static class Builder{
        private final CuratorFrameworkFactory.Builder _effectiveBuilder=CuratorFrameworkFactory.builder();
        private final String _nameSpacePrefix;

        public CuratorFramework build(){return _effectiveBuilder.build();}
        public CuratorTempFramework buildTemp(){return _effectiveBuilder.buildTemp();}
        public CuratorTempFramework buildTemp(long inactiveThreshold, TimeUnit unit) { return _effectiveBuilder.buildTemp(inactiveThreshold,unit);}

        public Builder authorization(String scheme, byte[] auth) {
            _effectiveBuilder.authorization(scheme, auth);
            return this;
        }

        public Builder connectString(String connectString) {
            _effectiveBuilder.connectString(connectString);
            return this;
        }

        public Builder ensembleProvider(EnsembleProvider ensembleProvider) {
            _effectiveBuilder.ensembleProvider(ensembleProvider);
            return this;
        }

        public Builder defaultData(byte[] defaultData) {
            _effectiveBuilder.defaultData(defaultData);
            return this;
        }

        public Builder namespace(String namespace) {
            _effectiveBuilder.namespace(_nameSpacePrefix+namespace);
            return this;
        }

        public Builder sessionTimeoutMs(int sessionTimeoutMs) {
            _effectiveBuilder.sessionTimeoutMs(sessionTimeoutMs);
            return this;
        }

        public Builder connectionTimeoutMs(int connectionTimeoutMs) {
            _effectiveBuilder.connectionTimeoutMs(connectionTimeoutMs);
            return this;
        }

        public Builder maxCloseWaitMs(int maxCloseWaitMs) {
            _effectiveBuilder.maxCloseWaitMs(maxCloseWaitMs);
            return this;
        }

        public Builder retryPolicy(RetryPolicy retryPolicy) {
            _effectiveBuilder.retryPolicy(retryPolicy);
            return this;
        }

        public Builder threadFactory(ThreadFactory threadFactory) {
            _effectiveBuilder.threadFactory(threadFactory);
            return this;
        }

        public Builder compressionProvider(CompressionProvider compressionProvider) {
            _effectiveBuilder.compressionProvider(compressionProvider);
            return this;
        }

        public Builder zookeeperFactory(ZookeeperFactory zookeeperFactory) {
            _effectiveBuilder.zookeeperFactory(zookeeperFactory);
            return this;
        }

        public Builder aclProvider(ACLProvider aclProvider) {
            _effectiveBuilder.aclProvider(aclProvider);
            return this;
        }

        public Builder canBeReadOnly(boolean canBeReadOnly) {
            _effectiveBuilder.canBeReadOnly(canBeReadOnly);
            return this;
        }

        protected Builder(){_nameSpacePrefix="";}
        protected Builder(String nameSpacePrefix){
            _nameSpacePrefix=nameSpacePrefix.replaceAll("[/]+$","");
            //by default set to the user namespace
            namespace(_nameSpacePrefix);
        }
    }
}
