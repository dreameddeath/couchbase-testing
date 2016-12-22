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

package com.dreameddeath.infrastructure.daemon.servlet;

import com.dreameddeath.core.service.discovery.AbstractServiceDiscoverer;
import com.dreameddeath.core.service.discovery.IServiceDiscovererListener;
import com.dreameddeath.core.service.model.common.CuratorDiscoveryServiceDescription;
import com.dreameddeath.core.service.model.common.ProxyClientInstanceInfo;
import com.dreameddeath.core.service.model.common.ServiceDescription;
import com.dreameddeath.core.service.registrar.IEndPointDescription;
import com.dreameddeath.core.service.registrar.ProxyClientRegistrar;
import com.dreameddeath.core.service.utils.ServiceNamingUtils;
import com.dreameddeath.core.service.utils.UriUtils;
import com.dreameddeath.core.service.utils.rest.RestServiceTypeHelper;
import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import com.google.common.base.Preconditions;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.proxy.AsyncProxyServlet;
import org.eclipse.jetty.util.component.LifeCycle;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Christophe Jeunesse on 21/08/2015.
 */
public class ProxyServlet extends AsyncProxyServlet{
    public static final String PROXY_ENDPOINT_DESC = "proxy-endpoint";
    public static final String SERVICE_DISCOVERER_DOMAINS_PARAM_NAME = "discoverer-base-pathes";
    public static final String PROXY_SERVICE_TYPE = "proxy-service-type";
    private static final Logger LOG = LoggerFactory.getLogger(ProxyServlet.class);
    public static final String SERVICE_DISCOVERER_MANAGER_PARAM_NAME = "discoverer-manager";

    private final List<AbstractServiceDiscoverer> serviceDiscoverers=new ArrayList<>();
    private final ConcurrentMap<ServiceUid,ServiceProvider<CuratorDiscoveryServiceDescription<?>>> serviceMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<ServiceUid,ProxyClientInstanceInfo> proxyClientMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String,ProxyClientRegistrar> proxyClientRegistrarMap = new ConcurrentHashMap<>();
    private final List<ListenerRegistered> listenerRegisteredList=new ArrayList<>();

    private final AtomicBoolean forHttp2=new AtomicBoolean(false);
    private String prefix;
    private String serviceType;
    private IEndPointDescription endPointDescription;
    private CuratorFramework curatorClient;
    private AbstractWebServer parentWebServer;
    private Collection<String> domainsList;
    private HttpClient http2Client;
    private ProxyServletLifeCycleListener lifeCycleListener;

    @Override
    public void init(ServletConfig config) throws ServletException {
        forHttp2.set(false);
        super.init(config);
        forHttp2.set(true);
        http2Client=createHttpClient();
        forHttp2.set(false);
        getServletContext().setAttribute(config.getServletName() + ".HttpClientForHTTP2", http2Client);

        curatorClient = (CuratorFramework) config.getServletContext().getAttribute(AbstractDaemon.GLOBAL_CURATOR_CLIENT_SERVLET_PARAM_NAME);
        endPointDescription = (IEndPointDescription)config.getServletContext().getAttribute(PROXY_ENDPOINT_DESC);
        prefix = ServletUtils.normalizePath(endPointDescription.path(),false);
        serviceType = (String)config.getServletContext().getAttribute(PROXY_SERVICE_TYPE);
        if(serviceType==null){
            serviceType = RestServiceTypeHelper.SERVICE_TYPE;
        }
        parentWebServer = (AbstractWebServer)config.getServletContext().getAttribute(AbstractServletContextHandler.GLOBAL_WEBSERVER_PARAM_NAME);
        lifeCycleListener = new ProxyServletLifeCycleListener();
        parentWebServer.getLifeCycle().addLifeCycleListener(lifeCycleListener);

        domainsList = (Collection<String>)config.getServletContext().getAttribute(SERVICE_DISCOVERER_DOMAINS_PARAM_NAME);
        Preconditions.checkNotNull(parentWebServer.getServiceDiscoveryManager(),"A service discovery manager should be defined for proxy servlet");
    }

    @Override
    public void destroy() {
        super.destroy();
        if(http2Client!=null){
            try {
                http2Client.stop();
            }
            catch (Exception e){
                    _log.warn("Error during http2 client stop",e);
            }
        }
        if(parentWebServer!=null){
            if(lifeCycleListener!=null) {
                try {
                    parentWebServer.getLifeCycle().removeLifeCycleListener(lifeCycleListener);
                }
                catch(Exception e){
                    _log.warn("Error during lifeCycle unregister",e);
                }
                lifeCycleListener=null;
            }
            parentWebServer=null;
        }
    }

    @Override
    protected void addViaHeader(Request proxyRequest) {
        proxyRequest.header(HttpHeader.VIA, proxyRequest.getVersion() + getViaHost());
    }

    @Override
    protected HttpClient newHttpClient() {
        HttpClientTransport transport;
        if(forHttp2.get()){
            HTTP2Client http2Client=new HTTP2Client();
            transport=new HttpClientTransportOverHTTP2(http2Client);
        }
        else{
            transport = new HttpClientTransportOverHTTP();
        }

        return new HttpClient(transport,null);
    }

    protected HttpClient getHttpClient(HttpVersion protocolVersion){
        if(HttpVersion.HTTP_2==protocolVersion){
            return http2Client;
        }
        else{
            return getHttpClient();
        }
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        final int requestId = getRequestId(request);

        String rewrittenTarget = rewriteTarget(request);

        if (_log.isDebugEnabled())
        {
            StringBuffer uri = request.getRequestURL();
            if (request.getQueryString() != null)
                uri.append("?").append(request.getQueryString());
            if (_log.isDebugEnabled())
                _log.debug("{} rewriting: {} -> {}", requestId, uri, rewrittenTarget);
        }

        if (rewrittenTarget == null)
        {
            onProxyRewriteFailed(request, response);
            return;
        }

        HttpVersion version=HttpVersion.fromString(request.getProtocol());
        final Request proxyRequest = getHttpClient(version).newRequest(rewrittenTarget)
                .method(request.getMethod())
                .version(version);

        copyRequestHeaders(request, proxyRequest);

        addProxyHeaders(request, proxyRequest);

        final AsyncContext asyncContext = request.startAsync();
        // We do not timeout the continuation, but the proxy request
        asyncContext.setTimeout(0);
        proxyRequest.timeout(getTimeout(), TimeUnit.MILLISECONDS);

        if (hasContent(request))
            proxyRequest.content(proxyRequestContent(request, response, proxyRequest));

        sendProxyRequest(request, response, proxyRequest);
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
            ServiceProvider<CuratorDiscoveryServiceDescription<?>> provider = serviceMap.get(new ServiceUid(serviceId,version));
            if(provider==null){
                LOG.error("Cannot find the service of {}",serviceId,version);
                throw new NotFoundException("Cannot Retrieve service instance of "+serviceId+"/"+version);
            }
            ServiceInstance<CuratorDiscoveryServiceDescription<?>> instance=provider.getInstance();
            if(instance==null){
                LOG.error("Cannot find the service instance for {}/{}",serviceId,version);
                throw new NotFoundException("Cannot Retrieve service instance of "+serviceId+"/"+version);
            }
            String uriStr = UriUtils.buildUri(instance,false);
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
        private final String serviceId;
        private final String version;

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


    private class ListenerRegistered{
        private final AbstractServiceDiscoverer<?,CuratorDiscoveryServiceDescription<?>> discoverer;
        private final IServiceDiscovererListener<CuratorDiscoveryServiceDescription<?>> listener;

        ListenerRegistered(AbstractServiceDiscoverer<?, CuratorDiscoveryServiceDescription<?>> discoverer, IServiceDiscovererListener<CuratorDiscoveryServiceDescription<?>> listener) {
            this.discoverer = discoverer;
            this.listener = listener;
        }
    }

    private class ProxyServletLifeCycleListener implements LifeCycle.Listener {
        @Override public void lifeCycleStarting(LifeCycle lifeCycle) {}
        @Override public void lifeCycleStarted(LifeCycle lifeCycle) {
            for(String domain:domainsList){
                LOG.info("Registering domain {} and type {} for proxy of webserver {}",domain,serviceType,parentWebServer.getUuid().toString());
                AbstractServiceDiscoverer<?,CuratorDiscoveryServiceDescription<?>> serviceDiscovered;
                try{
                    serviceDiscovered = parentWebServer.getServiceDiscoveryManager().getServiceDiscoverer(domain,serviceType);
                }
                catch (Exception e){
                    throw new RuntimeException("Cannot get "+domain+" for service type "+serviceType,e);
                }
                IServiceDiscovererListener<CuratorDiscoveryServiceDescription<?>> listener=serviceDiscovered.addListener(new IServiceDiscovererListener<CuratorDiscoveryServiceDescription<?>>() {
                    @Override
                    public void onProviderRegister(AbstractServiceDiscoverer discoverer, ServiceProvider<CuratorDiscoveryServiceDescription<?>> provider, ServiceDescription descr) {
                        final ServiceUid suid= new ServiceUid(descr.getName(),descr.getVersion());
                        serviceMap.computeIfAbsent(suid,newSuid->{
                                    LOG.info("Registering service {} for proxy of webserver {}",descr.getFullName(),parentWebServer.getUuid().toString());
                                    try{
                                        ProxyClientRegistrar registrar = proxyClientRegistrarMap.computeIfAbsent(discoverer.getDomain(), domain -> new ProxyClientRegistrar(curatorClient, domain,serviceType, parentWebServer.getParentDaemon().getUuid().toString(), parentWebServer.getUuid().toString()));
                                        ProxyClientInstanceInfo proxyClientInfo = new ProxyClientInstanceInfo();
                                        proxyClientInfo.setUid(UUID.randomUUID().toString());
                                        proxyClientInfo.setServiceName(ServiceNamingUtils.buildServiceFullName(suid.getServiceId(), suid.getVersion()));
                                        proxyClientInfo.setCreationDate(DateTime.now());
                                        proxyClientInfo.setServiceType(serviceType);
                                        proxyClientInfo.setUri("http://"+endPointDescription.host()+":"+endPointDescription.port()+ServletUtils.normalizePath("/"+endPointDescription.path()+"/"+suid.getServiceId()+"/"+suid.getVersion(),false));
                                        registrar.enrich(proxyClientInfo);
                                        registrar.register(proxyClientInfo);
                                        proxyClientMap.put(suid,proxyClientInfo);
                                    }
                                    catch(Exception e){
                                        LOG.error("Error while registrar Proxy client "+descr.getFullName(),e);
                                    }
                                    return provider;
                                }
                        );
                    }

                    @Override
                    public void onProviderUpdate(AbstractServiceDiscoverer discoverer, ServiceProvider<CuratorDiscoveryServiceDescription<?>> provider, ServiceDescription description) {

                    }

                    @Override
                    public void onProviderUnRegister(AbstractServiceDiscoverer discoverer, ServiceProvider<CuratorDiscoveryServiceDescription<?>> provider, ServiceDescription descr) {
                        ServiceUid suid= new ServiceUid(descr.getName(),descr.getVersion());
                        serviceMap.remove(suid);
                        ProxyClientRegistrar registrar = proxyClientRegistrarMap.get(discoverer.getDomain());
                        ProxyClientInstanceInfo clientInfo = proxyClientMap.remove(suid);
                        if(registrar!=null && clientInfo!=null){
                            try {
                                registrar.deregister(clientInfo);
                            }
                            catch(Throwable e){
                                LOG.error("Error during unregistrer on domain <"+discoverer.getDomain()+"> for client <"+clientInfo.getUid()+":"+clientInfo.getUri()+">",e);
                            }
                        }
                    }
                });
                listenerRegisteredList.add(new ListenerRegistered(serviceDiscovered,listener));
                serviceDiscoverers.add(serviceDiscovered);
            }
        }
        @Override public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {
            lifeCycleStopping(lifeCycle);
        }
        @Override public void lifeCycleStopping(LifeCycle lifeCycle) {
            listenerRegisteredList.forEach(listenerRegistered -> listenerRegistered.discoverer.removeListener(listenerRegistered.listener));
            listenerRegisteredList.clear();
            //Should not occur
            proxyClientRegistrarMap.values().forEach(ProxyClientRegistrar::close);
            proxyClientRegistrarMap.clear();
            serviceDiscoverers.clear();
            serviceMap.clear();
        }
        @Override public void lifeCycleStopped(LifeCycle lifeCycle) {}
    }
}
