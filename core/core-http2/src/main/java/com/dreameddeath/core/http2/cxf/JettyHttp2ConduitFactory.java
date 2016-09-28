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

package com.dreameddeath.core.http2.cxf;

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
public class JettyHttp2ConduitFactory implements HTTPConduitFactory {
    private final Logger LOG = LoggerFactory.getLogger(JettyHttp2Conduit.class);

    //ConnectionPool
    public static final String CONNECTION_TIMEOUT = "org.apache.cxf.transport.http.jettyh2.CONNECTION_TIMEOUT";
    public static final String CONNECTION_MAX_IDLE = "org.apache.cxf.transport.http.jettyh2.CONNECTION_MAX_IDLE";

    //CXF specific
    public static final String USE_POLICY = "org.apache.cxf.transport.http.jettyh2.usePolicy";


    private final Map<TLSClientParameters,HttpClient> clientPerTlsParams=new ConcurrentHashMap<>();
    private final HttpClient pureHttpClient;
    private final Bus bus;
    private boolean shutdown;
    private UseAsyncPolicy useAsyncPolicy;
    private int connectionTimeout=0;
    private int connectionMaxIdle=0;

    public JettyHttp2ConduitFactory() {
        this(null);

    }

    public JettyHttp2ConduitFactory(Bus bus){
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
        pureHttpClient = createClient(null);
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


    public HttpClient createClient(TLSClientParameters parameters){
        HTTP2Client http2Client=new HTTP2Client();
        SslContextFactory sslContextFactory = new SslContextFactory();
        if(parameters!=null) {
            sslContextFactory.setSslContext(createSSLContext(parameters));
        }
        HttpClient client= new HttpClient(new HttpClientTransportOverHTTP2(http2Client),sslContextFactory);
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
        return new JettyHttp2Conduit(bus,endpointInfo,endpointReferenceType,this);
    }

    public boolean isShutdown() {
        return shutdown;
    }


    public HttpClient getClient(TLSClientParameters tlsClientParameters) {
        if(tlsClientParameters==null){
            return pureHttpClient;
        }
        return clientPerTlsParams.computeIfAbsent(tlsClientParameters, this::createClient);
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

    public Object getUseAsyncPolicy() {
        return useAsyncPolicy;
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
            return ASYNC_ONLY;
        }
    };


    private void setProperties(Map<String, Object> s) {
        //properties that can be updated "live"
        if (s == null) {
            return;
        }
        Object st = s.get(USE_POLICY);
        if (st == null) {
            st = SystemPropertyAction.getPropertyOrNull(USE_POLICY);
        }
        useAsyncPolicy = UseAsyncPolicy.getPolicy(st);

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

}
