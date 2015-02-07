package com.dreameddeath.core.curator;

import com.dreameddeath.core.exception.curator.BadConnectionStringException;
import com.dreameddeath.core.exception.curator.DuplicateClusterClientException;
import com.dreameddeath.core.exception.curator.InconsitentClientRequest;
import org.apache.curator.RetryPolicy;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorTempFramework;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.api.CompressionProvider;
import org.apache.curator.utils.ZookeeperFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by CEAJ8230 on 19/01/2015.
 */
public class CuratorFrameworkFactory{
    public static String CONNECTION_STRING_SEPARATOR = ",";
    public static Pattern CONNECTION_STRING_PATTERN = Pattern.compile("^\\s*((\\w+)(\\.\\w+)*:\\d+)(\\s*,\\s*(\\w+)(\\.\\w+)*:\\d+)*\\s*$");

    private static final int DEFAULT_SESSION_TIMEOUT_MS = Integer.getInteger("curator-default-session-timeout", '\uea60').intValue();
    private static final int DEFAULT_CONNECTION_TIMEOUT_MS = Integer.getInteger("curator-default-connection-timeout", 15000).intValue();

    private static final ConcurrentMap<String,CuratorFramework> _curatorFrameworkConcurrentMap = new ConcurrentHashMap<>();


    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String nameSpacePrefix) {
        return new Builder(nameSpacePrefix);
    }


    public static CuratorFramework newClientInstance(String connectString, RetryPolicy retryPolicy) throws DuplicateClusterClientException,BadConnectionStringException{
        return newClientInstance(connectString, DEFAULT_SESSION_TIMEOUT_MS, DEFAULT_CONNECTION_TIMEOUT_MS, retryPolicy);
    }

    public static CuratorFramework newClientInstance(String connectString, int sessionTimeoutMs, int connectionTimeoutMs, RetryPolicy retryPolicy) throws DuplicateClusterClientException,BadConnectionStringException{
        return builder().connectString(connectString).sessionTimeoutMs(sessionTimeoutMs).connectionTimeoutMs(connectionTimeoutMs).retryPolicy(retryPolicy).build();
    }


    public static CuratorFramework newClientInstance(String nameSpacePrefix,String connectString, RetryPolicy retryPolicy) throws DuplicateClusterClientException,BadConnectionStringException{
        return newClientInstance(nameSpacePrefix, connectString, DEFAULT_SESSION_TIMEOUT_MS, DEFAULT_CONNECTION_TIMEOUT_MS, retryPolicy);
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
        return newClient(connectString,DEFAULT_SESSION_TIMEOUT_MS,DEFAULT_CONNECTION_TIMEOUT_MS,retryPolicy);
    }


    public static CuratorFramework newClient(String nameSpacePrefix,String connectString, RetryPolicy retryPolicy)throws InconsitentClientRequest,BadConnectionStringException{
        return newClient(nameSpacePrefix,connectString,DEFAULT_SESSION_TIMEOUT_MS,DEFAULT_CONNECTION_TIMEOUT_MS,retryPolicy);
    }

    private static String normalizeNameSpacePrefix(String prefix){
        return prefix.replaceAll("[/]+$","");
    }

    public static class Builder{

        private final org.apache.curator.framework.CuratorFrameworkFactory.Builder _effectiveBuilder= org.apache.curator.framework.CuratorFrameworkFactory.builder();
        private final String _nameSpacePrefix;
        private String _connectionString=null;

        public CuratorFramework build() throws DuplicateClusterClientException,BadConnectionStringException{
            if(_connectionString==null){
                throw new BadConnectionStringException(_connectionString);
            }
            List<String> servers = new ArrayList();
            CuratorFramework oldFramework=null;
            String newConnectionString="";
            synchronized (_curatorFrameworkConcurrentMap) {
                for (String server : _connectionString.split(CONNECTION_STRING_SEPARATOR)) {
                    server = server.trim().toLowerCase();
                    servers.add(server);
                    if (_curatorFrameworkConcurrentMap.containsKey(server)) {
                        CuratorFramework foundFramework = _curatorFrameworkConcurrentMap.get(server);
                        if((oldFramework!=null) && (oldFramework!=foundFramework)){
                            ///TODO throw an error
                        }
                        oldFramework = foundFramework;
                    }
                }
                if(oldFramework==null){
                    CuratorFramework newCuratorFramework =_effectiveBuilder.build();
                    for(String server:servers){
                        _curatorFrameworkConcurrentMap.putIfAbsent(server,newCuratorFramework);
                    }
                    return newCuratorFramework;
                }
                else{
                    throw new DuplicateClusterClientException(servers,"The given connection address already exists",oldFramework);
                    //return oldFramework;
                }
            }
        }

        public CuratorTempFramework buildTemp(){return _effectiveBuilder.buildTemp();}
        public CuratorTempFramework buildTemp(long inactiveThreshold, TimeUnit unit) { return _effectiveBuilder.buildTemp(inactiveThreshold,unit);}

        public Builder authorization(String scheme, byte[] auth) {
            _effectiveBuilder.authorization(scheme, auth);
            return this;
        }

        public Builder connectString(String connectString) throws BadConnectionStringException{
            if(connectString==null){
                throw new BadConnectionStringException(connectString);
            }
            else if(!CONNECTION_STRING_PATTERN.matcher(connectString).matches()){
                throw new BadConnectionStringException(connectString);
            }
            _connectionString = connectString;
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
            if(_nameSpacePrefix!=null){
                namespace = _nameSpacePrefix+"/"+namespace;
            }
            _effectiveBuilder.namespace(namespace);
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
            nameSpacePrefix = normalizeNameSpacePrefix(nameSpacePrefix);
            //by default set to the user namespace
            namespace(nameSpacePrefix);
            _nameSpacePrefix = nameSpacePrefix;
        }
    }
}
