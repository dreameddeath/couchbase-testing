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

package com.dreameddeath.core.curator;

import com.dreameddeath.core.curator.config.CuratorConfigProperties;
import com.dreameddeath.core.curator.exception.BadConnectionStringException;
import com.dreameddeath.core.curator.exception.DuplicateClusterClientException;
import com.dreameddeath.core.curator.exception.InconsitentClientRequest;
import org.apache.curator.RetryPolicy;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorTempFramework;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.CompressionProvider;
import org.apache.curator.framework.api.CuratorEventType;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.utils.ZookeeperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 19/01/2015.
 */
public class CuratorFrameworkFactory{
    private final static Logger LOG = LoggerFactory.getLogger(CuratorFrameworkFactory.class);
    public static String CONNECTION_STRING_SEPARATOR = ",";
    public static Pattern CONNECTION_STRING_PATTERN = Pattern.compile("^\\s*((\\w+)(\\.\\w+)*:\\d+)(\\s*,\\s*(\\w+)(\\.\\w+)*:\\d+)*\\s*$");

    //private static final int DEFAULT_SESSION_TIMEOUT_MS = Integer.getInteger("curator-default-session-timeout", '\uea60').intValue();
    //private static final int DEFAULT_CONNECTION_TIMEOUT_MS = Integer.getInteger("curator-default-connection-timeout", 15000).intValue();

    private static final ConcurrentMap<String,CuratorFramework> curatorFrameworkConcurrentMap = new ConcurrentHashMap<>();


    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String nameSpacePrefix) {
        return new Builder(nameSpacePrefix);
    }


    public static CuratorFramework newClientInstance(String connectString, RetryPolicy retryPolicy) throws DuplicateClusterClientException,BadConnectionStringException{
        return newClientInstance(connectString, CuratorConfigProperties.CURATOR_SESSION_TIMEOUT.get(), CuratorConfigProperties.CURATOR_CONNECTION_TIMEOUT.get(), retryPolicy);
    }

    public static CuratorFramework newClientInstance(String connectString, int sessionTimeoutMs, int connectionTimeoutMs, RetryPolicy retryPolicy) throws DuplicateClusterClientException,BadConnectionStringException{
        return builder().connectString(connectString).sessionTimeoutMs(sessionTimeoutMs).connectionTimeoutMs(connectionTimeoutMs).retryPolicy(retryPolicy).build();
    }


    public static CuratorFramework newClientInstance(String nameSpacePrefix,String connectString, RetryPolicy retryPolicy) throws DuplicateClusterClientException,BadConnectionStringException{
        return newClientInstance(nameSpacePrefix, connectString, CuratorConfigProperties.CURATOR_SESSION_TIMEOUT.get(),CuratorConfigProperties.CURATOR_CONNECTION_TIMEOUT.get(), retryPolicy);
    }

    public static CuratorFramework newClientInstance(String nameSpacePrefix,String connectString, int sessionTimeoutMs, int connectionTimeoutMs, RetryPolicy retryPolicy) throws DuplicateClusterClientException,BadConnectionStringException{
        return builder(nameSpacePrefix).connectString(connectString).sessionTimeoutMs(sessionTimeoutMs).connectionTimeoutMs(connectionTimeoutMs).retryPolicy(retryPolicy).build();
    }


    public static CuratorFramework newClient(String nameSpacePrefix,String connectString, int sessionTimeoutMs, int connectionTimeoutMs, RetryPolicy retryPolicy)throws InconsitentClientRequest,BadConnectionStringException{
        try{
            return newClientInstance(nameSpacePrefix,connectString,sessionTimeoutMs,connectionTimeoutMs,retryPolicy);
        }
        catch (DuplicateClusterClientException e){
            String normalizedNameSpacePrefix = normalizeNameSpacePrefix(nameSpacePrefix);
            if(e.getExistingFramework().getNamespace().equals(normalizedNameSpacePrefix)){
                return e.getExistingFramework();
            }
            else{
                throw new InconsitentClientRequest(e.getExistingFramework().getNamespace(),normalizedNameSpacePrefix);
            }
        }
    }


    public static CuratorFramework newClient(String connectString, int sessionTimeoutMs, int connectionTimeoutMs, RetryPolicy retryPolicy)throws InconsitentClientRequest,BadConnectionStringException{
        try{
            return newClientInstance(connectString,sessionTimeoutMs,connectionTimeoutMs,retryPolicy);
        }
        catch (DuplicateClusterClientException e){
            if((e.getExistingFramework().getNamespace()==null) || e.getExistingFramework().getNamespace().equals("")){
                return e.getExistingFramework();
            }
            else{
                throw new InconsitentClientRequest(e.getExistingFramework().getNamespace(),"");
            }
        }
    }

    public static CuratorFramework newClient(String connectString, RetryPolicy retryPolicy)throws InconsitentClientRequest,BadConnectionStringException{
        return newClient(connectString,CuratorConfigProperties.CURATOR_SESSION_TIMEOUT.get(),CuratorConfigProperties.CURATOR_CONNECTION_TIMEOUT.get(),retryPolicy);
    }


    public static CuratorFramework newClient(String nameSpacePrefix,String connectString, RetryPolicy retryPolicy)throws InconsitentClientRequest,BadConnectionStringException{
        return newClient(nameSpacePrefix,connectString,CuratorConfigProperties.CURATOR_SESSION_TIMEOUT.get(),CuratorConfigProperties.CURATOR_CONNECTION_TIMEOUT.get(),retryPolicy);
    }

    private static String normalizeNameSpacePrefix(String prefix){
        return prefix.replaceAll("[/]+$","");
    }

    public static class Builder{

        private final org.apache.curator.framework.CuratorFrameworkFactory.Builder effectiveBuilder= org.apache.curator.framework.CuratorFrameworkFactory.builder();
        private final String nameSpacePrefix;
        private String connectionString=null;

        private void addClosureListener(CuratorFramework framework){
            framework.getCuratorListenable().addListener((client, event) -> {
                if(event.getType().equals(CuratorEventType.CLOSING)){
                    curatorFrameworkConcurrentMap.entrySet().removeIf(nextEntry -> nextEntry.getValue().equals(client));
                    LOG.info("Closing framework {}",framework);
                }
            });
        }


        private void addEventLogListener(final CuratorFramework framework,final String name){
            framework.getCuratorListenable().addListener((client, event) -> LOG.info("Receiving event {} for curator client {}",event,name));
        }

        public CuratorFramework build() throws DuplicateClusterClientException,BadConnectionStringException{
            if(connectionString==null){
                throw new BadConnectionStringException(null);
            }
            List<String> servers = Arrays.stream(connectionString.split(CONNECTION_STRING_SEPARATOR)).map(server->server.trim().toLowerCase()).collect(Collectors.toList());
            List<CuratorFramework> alreadyAssignedServer=servers.stream().map(curatorFrameworkConcurrentMap::get).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            if(alreadyAssignedServer.size()>=1) {
                throw new DuplicateClusterClientException(servers,"The given connection address already exists",alreadyAssignedServer.get(0));
            }
            final CuratorFramework newCuratorFramework =effectiveBuilder.build();
            addClosureListener(newCuratorFramework);
            servers.forEach(server->curatorFrameworkConcurrentMap.put(server,newCuratorFramework));
            return newCuratorFramework;
        }

        public CuratorTempFramework buildTemp(){return effectiveBuilder.buildTemp();}
        public CuratorTempFramework buildTemp(long inactiveThreshold, TimeUnit unit) { return effectiveBuilder.buildTemp(inactiveThreshold,unit);}

        public Builder authorization(String scheme, byte[] auth) {
            effectiveBuilder.authorization(scheme, auth);
            return this;
        }

        public Builder connectString(String connectString) throws BadConnectionStringException{
            if(connectString==null){
                throw new BadConnectionStringException(null);
            }
            else if(!CONNECTION_STRING_PATTERN.matcher(connectString).matches()){
                throw new BadConnectionStringException(connectString);
            }
            connectionString = connectString;
            effectiveBuilder.connectString(connectString);
            return this;
        }

        public Builder ensembleProvider(EnsembleProvider ensembleProvider) {
            effectiveBuilder.ensembleProvider(ensembleProvider);
            return this;
        }

        public Builder defaultData(byte[] defaultData) {
            effectiveBuilder.defaultData(defaultData);
            return this;
        }

        public Builder namespace(String namespace) {
            if(nameSpacePrefix!=null){
                namespace = nameSpacePrefix+"/"+namespace;
            }
            effectiveBuilder.namespace(namespace);
            return this;
        }

        public Builder sessionTimeoutMs(int sessionTimeoutMs) {
            effectiveBuilder.sessionTimeoutMs(sessionTimeoutMs);
            return this;
        }

        public Builder connectionTimeoutMs(int connectionTimeoutMs) {
            effectiveBuilder.connectionTimeoutMs(connectionTimeoutMs);
            return this;
        }

        public Builder maxCloseWaitMs(int maxCloseWaitMs) {
            effectiveBuilder.maxCloseWaitMs(maxCloseWaitMs);
            return this;
        }

        public Builder retryPolicy(RetryPolicy retryPolicy) {
            effectiveBuilder.retryPolicy(retryPolicy);
            return this;
        }

        public Builder threadFactory(ThreadFactory threadFactory) {
            effectiveBuilder.threadFactory(threadFactory);
            return this;
        }

        public Builder compressionProvider(CompressionProvider compressionProvider) {
            effectiveBuilder.compressionProvider(compressionProvider);
            return this;
        }

        public Builder zookeeperFactory(ZookeeperFactory zookeeperFactory) {
            effectiveBuilder.zookeeperFactory(zookeeperFactory);
            return this;
        }

        public Builder aclProvider(ACLProvider aclProvider) {
            effectiveBuilder.aclProvider(aclProvider);
            return this;
        }

        public Builder canBeReadOnly(boolean canBeReadOnly) {
            effectiveBuilder.canBeReadOnly(canBeReadOnly);
            return this;
        }

        protected Builder(){
            nameSpacePrefix="";
        }

        protected Builder(String nameSpacePrefix){
            nameSpacePrefix = normalizeNameSpacePrefix(nameSpacePrefix);
            //by default set to the user namespace
            namespace(nameSpacePrefix);
            this.nameSpacePrefix = nameSpacePrefix;
        }
    }

    public static void cleanup(){
        List<CuratorFramework> curatorFrameworkList = new ArrayList<>(curatorFrameworkConcurrentMap.values());
        curatorFrameworkList.forEach(client->{
            try{
                if(client.getState().equals(CuratorFrameworkState.STARTED)){
                    client.close();
                }
            }
            catch (Throwable e){
                LOG.error("Error during client stop",e);
            }
        });

        curatorFrameworkConcurrentMap.clear();
    }
}
