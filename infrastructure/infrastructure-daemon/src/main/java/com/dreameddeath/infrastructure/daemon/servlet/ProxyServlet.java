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

package com.dreameddeath.infrastructure.daemon.servlet;

import com.dreameddeath.core.service.client.ServiceClientFactory;
import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.exception.ServiceDiscoveryException;
import com.dreameddeath.core.service.model.ServiceDescription;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.eclipse.jetty.proxy.AsyncProxyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Christophe Jeunesse on 21/08/2015.
 */
public class ProxyServlet extends AsyncProxyServlet {
    public static String SERVICE_DISCOVERER_PATHES_PARAM_NAME = "discoverer-base-pathes";
    public static String PROXY_PREFIX_PARAM_NAME = "proxy-url-prefix";
    private static Logger LOG = LoggerFactory.getLogger(ProxyServlet.class);

    private String prefix;
    private List<ServiceDiscoverer> serviceDiscoverers=new ArrayList<>();
    private ConcurrentMap<ServiceUid,ServiceProvider<ServiceDescription>> serviceMap = new ConcurrentHashMap<>();


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        CuratorFramework curatorClient = (CuratorFramework) config.getServletContext().getAttribute(AbstractDaemon.GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME);
        prefix = ServletUtils.normalizePath((String)config.getServletContext().getAttribute(PROXY_PREFIX_PARAM_NAME),false);

        List<String> basePathsList = (List<String>)config.getServletContext().getAttribute(SERVICE_DISCOVERER_PATHES_PARAM_NAME);
        for(String basePath:basePathsList){
            ServiceDiscoverer newService = new ServiceDiscoverer(curatorClient, basePath);
            try {
                newService.start();
            }
            catch (ServiceDiscoveryException e){
                throw new ServletException(e);
            }
            serviceDiscoverers.add(newService);
        }
    }



    private ServiceProvider<ServiceDescription> findServiceProvider(ServiceUid suid){
        String name = ServiceNamingUtils.buildServiceFullName(suid.getServiceId(),suid.getVersion());
        for(ServiceDiscoverer discoverer:serviceDiscoverers){
            try {
                discoverer.resyncAllServices();
                return discoverer.getServiceProvider(name);
            }
            catch(Exception e){
                //ignore
            }
        }
        throw new RuntimeException("Cannot find service {"+name+"}");
    }

    @Override
    protected String rewriteTarget(HttpServletRequest clientRequest) {
        if (!validateDestination(clientRequest.getServerName(), clientRequest.getServerPort()))
            return null;

        String effectivePath = clientRequest.getRequestURI();

        effectivePath = effectivePath.substring(prefix.length() + 1); //remove prefix
        String serviceId = effectivePath.substring(0, effectivePath.indexOf("/"));
        effectivePath = effectivePath.substring(serviceId.length()+1);
        String version;
        if(effectivePath.contains("/")){
            version= effectivePath.substring(0, effectivePath.indexOf("/"));
            effectivePath = effectivePath.substring(version.length()+1);
        }
        else{
            version=effectivePath;
            effectivePath = "";
        }


        try {
            serviceId = URLDecoder.decode(serviceId, "UTF-8");
            ServiceProvider<ServiceDescription> provider = serviceMap.computeIfAbsent(new ServiceUid(serviceId,version), suid -> findServiceProvider(suid));
            ServiceInstance<ServiceDescription> instance=provider.getInstance();
            if(instance==null){

            }
            String uriStr = ServiceClientFactory.buildUri(instance);
            if(!uriStr.endsWith("/")){
                uriStr += "/";
            }
            uriStr+=effectivePath;
            if(clientRequest.getQueryString()!=null){
                uriStr+="?"+clientRequest.getQueryString();
            }
            LOG.debug("Rewritten path {} to {}",clientRequest.getRequestURI(),uriStr);
            return uriStr;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onProxyRewriteFailed(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }


    private static class ServiceUid{
        private String serviceId;
        private String version;

        public ServiceUid(String serviceId, String version) {
            this.serviceId = serviceId;
            this.version = version;
        }

        public String getServiceId() {
            return serviceId;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ServiceUid that = (ServiceUid) o;

            if (!serviceId.equals(that.serviceId)) return false;
            return version.equals(that.version);

        }

        @Override
        public int hashCode() {
            int result = serviceId.hashCode();
            result = 31 * result + version.hashCode();
            return result;
        }
    }
}
