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

package org.apache.cxf.transport.http_jetty.client;

import org.apache.cxf.Bus;
import org.apache.cxf.buslifecycle.BusLifeCycleListener;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.apache.cxf.common.util.SystemPropertyAction;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HTTPConduitFactory;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 20/09/2016.
 */
public class JettyHttpClientConduitFactory implements HTTPConduitFactory {
    private final Logger LOG = LoggerFactory.getLogger(JettyHttpClientConduit.class);

    //ConnectionPool
    public static final String CONNECTION_TIMEOUT = "org.apache.cxf.transport.http.jetty-client.CONNECTION_TIMEOUT";
    public static final String CONNECTION_MAX_IDLE = "org.apache.cxf.transport.http.jetty-client.CONNECTION_MAX_IDLE";

    //CXF specific
    public static final String USE_POLICY = "org.apache.cxf.transport.http.jetty-client.usePolicy";
    public static final String HTTP_VERSION_POLICY = "org.apache.cxf.transport.http.jetty-client.versionPolicy";


    private final Map<ClientKey,HttpClient> clientKeyHttpClientMap =new ConcurrentHashMap<>();
    private final Bus bus;
    private boolean shutdown;
    private UseAsyncPolicy useAsyncPolicy=null;
    private HttpVersionPolicy httpVersionPolicy=null;
    private int connectionTimeout=0;
    private int connectionMaxIdle=0;

    public JettyHttpClientConduitFactory() {
        this(null);

    }

    public JettyHttpClientConduitFactory(Bus bus){
        this.bus=bus;
        this.shutdown=false;
        if(bus!=null) {
            setProperties(bus.getProperties());

            BusLifeCycleManager manager = bus.getExtension(BusLifeCycleManager.class);

            if (manager != null) {
                manager.registerLifeCycleListener(new BusLifeCycleListener() {
                    @Override
                    public void initComplete() {

                    }

                    @Override
                    public void preShutdown() {

                    }

                    @Override
                    public void postShutdown() {
                        shutdown = true;
                    }
                });
            }
        }
    }

    //Reused from
    protected SSLContext createSSLContext(TLSClientParameters tlsClientParameters)   {
        try {
            return org.apache.cxf.transport.https.SSLUtils.getSSLContext(tlsClientParameters);
        }
        catch (GeneralSecurityException e){
            throw new RuntimeException(e);
        }
    }


    public HttpClient createClient(ClientKey key){
        LOG.info("Create client for version {}",key.version);
        SslContextFactory sslContextFactory = new SslContextFactory();
        if(key.tlsParams!=null) {
            sslContextFactory.setSslContext(createSSLContext(key.tlsParams));
        }
        HttpClientTransport transport=null;
        if(key.version== ClientHttpVersion.HTTP_2){
            HTTP2Client http2Client=new HTTP2Client();
            transport=new HttpClientTransportOverHTTP2(http2Client);
        }
        else{
            transport = new HttpClientTransportOverHTTP();
        }

        HttpClient client= new HttpClient(transport,sslContextFactory);
        if(connectionTimeout>0){
            client.setConnectTimeout(connectionTimeout);
        }

        if(connectionMaxIdle>0){
            client.setIdleTimeout(connectionMaxIdle);
        }

        addBusLifeCycleListenerOrStart(client);
        return client;
    }

    @Override
    public HTTPConduit createConduit(HTTPTransportFactory httpTransportFactory, Bus bus, EndpointInfo endpointInfo, EndpointReferenceType endpointReferenceType) throws IOException {
        return createConduit(bus, endpointInfo, endpointReferenceType);
    }

    public HTTPConduit createConduit(Bus bus, EndpointInfo endpointInfo, EndpointReferenceType endpointReferenceType) throws IOException {
        return new JettyHttpClientConduit(bus,endpointInfo,endpointReferenceType,this);
    }

    public boolean isShutdown() {
        return shutdown;
    }


    public HttpClient getClient(TLSClientParameters tlsClientParameters,ClientHttpVersion version) {
        return clientKeyHttpClientMap.computeIfAbsent(new ClientKey(tlsClientParameters,version), this::createClient);
    }

    private void addBusLifeCycleListenerOrStart(final HttpClient client){
        BusLifeCycleManager manager=null;
        if(bus!=null) {
            manager = bus.getExtension(BusLifeCycleManager.class);
        }
        if (manager != null) {
            manager.registerLifeCycleListener(new BusLifeCycleListener() {
                public void initComplete() {
                    try {
                        client.start();
                    }
                    catch(Throwable e){
                        throw new RuntimeException(e);
                    }
                }
                public void preShutdown() {
                }
                public void postShutdown() {
                    if(client.isStarted()){
                        try {
                            client.stop();
                        }
                        catch(Throwable e){
                            LOG.error("Error during stopping", e);
                        }
                    }
                }
            });
        }
        else{
            try {
                client.start();
            }
            catch(Throwable e){
                throw new RuntimeException(e);
            }
        }
    }

    public UseAsyncPolicy getUseAsyncPolicy() {
        return useAsyncPolicy;
    }


    private void setProperties(Map<String, Object> s) {
        //properties that can be updated "live"
        if (s == null) {
            return;
        }
        Object usePolicy = s.get(USE_POLICY);
        if (usePolicy == null) {
            usePolicy = SystemPropertyAction.getPropertyOrNull(USE_POLICY);
        }
        useAsyncPolicy = UseAsyncPolicy.getPolicy(usePolicy);

        Object httpVerParam = s.get(HTTP_VERSION_POLICY);
        if (httpVerParam == null) {
            httpVerParam = SystemPropertyAction.getPropertyOrNull(HTTP_VERSION_POLICY);
        }
        httpVersionPolicy = HttpVersionPolicy.getPolicy(httpVerParam);


        connectionTimeout = getInt(s.get(CONNECTION_TIMEOUT), connectionTimeout);
        connectionMaxIdle = getInt(s.get(CONNECTION_MAX_IDLE), connectionMaxIdle);
    }

    private int getInt(Object s, int defaultv) {
        int i = defaultv;
        if (s instanceof String) {
            i = Integer.parseInt((String)s);
        } else if (s instanceof Number) {
            i = ((Number)s).intValue();
        }
        if (i == -1) {
            i = defaultv;
        }
        return i;
    }

    public HttpVersionPolicy getHttpVersionPolicy() {
        return httpVersionPolicy;
    }

    private static class ClientKey{
        private final TLSClientParameters tlsParams;
        private final ClientHttpVersion version;

        public ClientKey(TLSClientParameters tlsParams, ClientHttpVersion version) {
            this.tlsParams = tlsParams;
            this.version = version;
        }

        public ClientKey(ClientHttpVersion version) {
            this(null,version);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClientKey clientKey = (ClientKey) o;

            if (tlsParams != null ? !tlsParams.equals(clientKey.tlsParams) : clientKey.tlsParams != null) return false;
            return version == clientKey.version;

        }

        @Override
        public int hashCode() {
            int result = tlsParams != null ? tlsParams.hashCode() : 0;
            result = 31 * result + version.hashCode();
            return result;
        }
    }

    public static enum ClientHttpVersion {
        HTTP_1,
        HTTP_2
    }

    public enum HttpVersionPolicy {
        ALWAYS_1,
        DEFAULT_1,
        ALWAYS_2,
        DEFAULT_2;

        public static HttpVersionPolicy getPolicy(Object httpVerParam) {
            if(httpVerParam instanceof HttpVersionPolicy){
                return (HttpVersionPolicy) httpVerParam;
            }
            else if(httpVerParam instanceof String){
                if(ALWAYS_1.toString().equals(httpVerParam)){
                    return ALWAYS_1;
                }
                else if(ALWAYS_2.toString().equals(httpVerParam)){
                    return ALWAYS_2;
                }
                else if(DEFAULT_1.toString().equals(httpVerParam)){
                    return DEFAULT_1;
                }
                else if(DEFAULT_2.toString().equals(httpVerParam)){
                    return DEFAULT_2;
                }
                if(HttpVersion.HTTP_2.equals(httpVerParam) ||
                        "2.0".equals(httpVerParam) ||
                        "2".equals(httpVerParam)){
                    return DEFAULT_2;

                }
            }
            else if(httpVerParam instanceof Number){
                if(((Number)httpVerParam).intValue()==2){
                    return DEFAULT_2;
                }
            }
            return DEFAULT_1;
        }
    }

    public enum UseAsyncPolicy {
        ALWAYS, ASYNC_ONLY, NEVER;

        public static UseAsyncPolicy getPolicy(Object st) {
            if (st instanceof UseAsyncPolicy) {
                return (UseAsyncPolicy)st;
            } else if (st instanceof String) {
                String s = ((String)st).toUpperCase();
                if ("ALWAYS".equals(s)) {
                    return ALWAYS;
                } else if ("NEVER".equals(s)) {
                    return NEVER;
                } else if ("ASYNC_ONLY".equals(s)) {
                    return ASYNC_ONLY;
                } else {
                    st = Boolean.parseBoolean(s);
                }
            }
            if (st instanceof Boolean) {
                return ((Boolean)st).booleanValue() ? ALWAYS : NEVER;
            }
            return ALWAYS;
        }
    };


}
