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
import org.apache.curator.x.discovery.ServiceProvider;
import org.eclipse.jetty.proxy.AsyncProxyServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
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

    private String _prefix;
    private List<ServiceDiscoverer> _serviceDiscoverers=new ArrayList<>();
    private ConcurrentMap<ServiceUid,ServiceProvider<ServiceDescription>> _serviceMap = new ConcurrentHashMap<>();


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        CuratorFramework curatorClient = (CuratorFramework) config.getServletContext().getAttribute(AbstractDaemon.GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME);
        _prefix = (String)config.getServletContext().getAttribute(PROXY_PREFIX_PARAM_NAME);
        List<String> basePathsList = (List<String>)config.getServletContext().getAttribute(SERVICE_DISCOVERER_PATHES_PARAM_NAME);
        for(String basePath:basePathsList){
            ServiceDiscoverer newService = new ServiceDiscoverer(curatorClient, basePath);
            try {
                newService.start();
            }
            catch (ServiceDiscoveryException e){
                throw new ServletException(e);
            }
            _serviceDiscoverers.add(newService);
        }
    }



    private ServiceProvider<ServiceDescription> findServiceProvider(ServiceUid suid){
        String name = ServiceNamingUtils.buildServiceFullName(suid.getServiceId(),suid.getVersion());
        for(ServiceDiscoverer discoverer:_serviceDiscoverers){
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

    protected URI rewriteURI(HttpServletRequest request) {
        String effectivePath = request.getRequestURI();
        if(effectivePath.startsWith("/")){
            effectivePath=effectivePath.substring(1);
        }
        effectivePath = effectivePath.substring(_prefix.length() + 1); //remove prefix
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
            ServiceProvider<ServiceDescription> provider = _serviceMap.computeIfAbsent(new ServiceUid(serviceId,version), suid -> findServiceProvider(suid));
            String uriStr = ServiceClientFactory.buildUri(provider.getInstance());
            if(!uriStr.endsWith("/")){
                uriStr += "/";
            }
            uriStr+=effectivePath;
            if(request.getQueryString()!=null){
                uriStr+="?"+request.getQueryString();
            }
            //uriStr = uriStr.replaceAll("/{2,}","/");
            URI result =new URI(uriStr);
            return result;
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private static class ServiceUid{
        private String _serviceId;
        private String _version;

        public ServiceUid(String serviceId, String version) {
            _serviceId = serviceId;
            _version = version;
        }

        public String getServiceId() {
            return _serviceId;
        }

        public String getVersion() {
            return _version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ServiceUid that = (ServiceUid) o;

            if (!_serviceId.equals(that._serviceId)) return false;
            return _version.equals(that._version);

        }

        @Override
        public int hashCode() {
            int result = _serviceId.hashCode();
            result = 31 * result + _version.hashCode();
            return result;
        }
    }
}
