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

import com.dreameddeath.core.service.client.ServiceClientImpl;
import com.dreameddeath.core.service.discovery.IServiceDiscovererListener;
import com.dreameddeath.core.service.discovery.ServiceDiscoverer;
import com.dreameddeath.core.service.model.CuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.model.ProxyClientInstanceInfo;
import com.dreameddeath.core.service.model.ServiceDescription;
import com.dreameddeath.core.service.registrar.ProxyClientRegistrar;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.eclipse.jetty.proxy.AsyncProxyServlet;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Christophe Jeunesse on 21/08/2015.
 */
public class ProxyServlet extends AsyncProxyServlet {
    public static String SERVICE_DISCOVERER_DOMAINS_PARAM_NAME = "discoverer-base-pathes";
    public static String PROXY_PREFIX_PARAM_NAME = "proxy-url-prefix";
    private static Logger LOG = LoggerFactory.getLogger(ProxyServlet.class);

    private final List<ServiceDiscoverer> serviceDiscoverers=new ArrayList<>();
    private final ConcurrentMap<ServiceUid,ServiceProvider<CuratorDiscoveryServiceDescription>> serviceMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<ServiceUid,ProxyClientInstanceInfo> proxyClientMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String,ProxyClientRegistrar> proxyClientRegistrarMap = new ConcurrentHashMap<>();
    private String prefix;
    private CuratorFramework curatorClient;
    private AbstractWebServer parentWebServer;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        curatorClient = (CuratorFramework) config.getServletContext().getAttribute(AbstractDaemon.GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME);
        prefix = ServletUtils.normalizePath((String)config.getServletContext().getAttribute(PROXY_PREFIX_PARAM_NAME),false);
        parentWebServer = (AbstractWebServer)config.getServletContext().getAttribute(AbstractServletContextHandler.GLOBAL_WEBSERVER_PARAM_NAME);
        List<String> domainsList = (List<String>)config.getServletContext().getAttribute(SERVICE_DISCOVERER_DOMAINS_PARAM_NAME);
        for(String domain:domainsList){
            LOG.info("Registering domain {} for proxy of webserver {}",domain,parentWebServer.getUuid().toString());
            ServiceDiscoverer newService = new ServiceDiscoverer(curatorClient, domain);
            newService.addListener(new IServiceDiscovererListener() {
                @Override
                public void onProviderRegister(ServiceDiscoverer discoverer, ServiceProvider<CuratorDiscoveryServiceDescription> provider, ServiceDescription descr) {
                    LOG.info("Registering service {} for proxy of webserver {}",descr.getFullName(),parentWebServer.getUuid().toString());
                    ServiceUid suid= new ServiceUid(descr.getName(),descr.getVersion());
                    serviceMap.put(suid,provider);
                    try{
                        ProxyClientRegistrar registrar = proxyClientRegistrarMap.computeIfAbsent(discoverer.getDomain(), domain -> new ProxyClientRegistrar(curatorClient, domain, parentWebServer.getParentDaemon().getUuid().toString(), parentWebServer.getUuid().toString()));
                        ProxyClientInstanceInfo proxyClientInfo = new ProxyClientInstanceInfo();
                        proxyClientInfo.setUid(UUID.randomUUID().toString());
                        proxyClientInfo.setServiceName(ServiceNamingUtils.buildServiceFullName(suid.getServiceId(), suid.getVersion()));
                        proxyClientInfo.setCreationDate(DateTime.now());
                        registrar.enrich(proxyClientInfo);
                        registrar.register(proxyClientInfo);
                        proxyClientMap.put(suid,proxyClientInfo);
                    }
                    catch(Exception e){
                        LOG.error("Error while registrar Proxy client "+descr.getFullName(),e);
                    }
                }

                @Override
                public void onProviderUnRegister(ServiceDiscoverer discoverer, ServiceProvider<CuratorDiscoveryServiceDescription> provider, ServiceDescription descr) {
                    ServiceUid suid= new ServiceUid(descr.getName(),descr.getVersion());
                    serviceMap.remove(suid);
                    proxyClientMap.remove(suid);
                }
            });
            try {
                newService.start();
            }
            catch (Exception e){
                throw new ServletException(e);
            }
            serviceDiscoverers.add(newService);
        }
    }

    @Override
    protected String rewriteTarget(HttpServletRequest clientRequest) {
        if (!validateDestination(clientRequest.getServerName(), clientRequest.getServerPort())) {
            return null;
        }

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
            ServiceProvider<CuratorDiscoveryServiceDescription> provider = serviceMap.get(new ServiceUid(serviceId,version));
            if(provider==null){
                LOG.error("Cannot find the service of {}",serviceId,version);
                throw new NotFoundException("Cannot Retrieve service instance of "+serviceId+"/"+version);
            }
            ServiceInstance<CuratorDiscoveryServiceDescription> instance=provider.getInstance();
            if(instance==null){
                LOG.error("Cannot find the service instance for {}/{}",serviceId,version);
                throw new NotFoundException("Cannot Retrieve service instance of "+serviceId+"/"+version);
            }
            String uriStr = ServiceClientImpl.buildUri(instance);
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
        catch (NotFoundException e){
            throw e;
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

        @Override
        public String toString() {
            return "ServiceUid{" +
                    "serviceId='" + serviceId + '\'' +
                    ", version='" + version + '\'' +
                    '}';
        }
    }

    @Override
    public void destroy() {
        proxyClientRegistrarMap.values().forEach(ProxyClientRegistrar::close);
        proxyClientRegistrarMap.clear();
        serviceDiscoverers.clear();
        serviceMap.clear();
        super.destroy();
    }
}
