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
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractTransportFactory;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HTTPConduitConfigurer;
import org.apache.cxf.transport.http.HTTPConduitFactory;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Christophe Jeunesse on 20/09/2016.
 */
public class JettyHttpClientTransportFactory extends AbstractTransportFactory implements ConduitInitiator {
    public static final String HTTP2_TRANSPORT="http://cxf.apache.org/transports/http/jetty-http-client/http2";
    public static final String HTTP1_TRANSPORT="http://cxf.apache.org/transports/http/jetty-http-client/http1";
    public static final List<String> DEFAULT_NAMESPACES = Arrays
            .asList("http://cxf.apache.org/transports/http/jetty-http-client",
                    "http://cxf.apache.org/transports/http/jetty-http-client/http1",
                    HTTP2_TRANSPORT
                    );

    /**
     * This constant holds the prefixes served by this factory.
     */
    private static final Set<String> URI_PREFIXES = new HashSet<String>();

    public static final String JETTY_HTTP_PREFIX = "jhc://";
    public static final String JETTY_HTTP2_PREFIX = "jhc2://";
    public static final String JETTY_HTTP1_PREFIX = "jhc1://";


    static {
        URI_PREFIXES.add(JETTY_HTTP_PREFIX);
        URI_PREFIXES.add(JETTY_HTTP2_PREFIX);
        URI_PREFIXES.add(JETTY_HTTP1_PREFIX);
    }

     static Set<String> getUriPrefixesList() {
        return URI_PREFIXES;
    }

    private final AtomicReference<JettyHttpClientConduitFactory> factory = new AtomicReference<>();

    public JettyHttpClientTransportFactory() {
        super(DEFAULT_NAMESPACES);
    }

    /**
     * This call is used by CXF ExtensionManager to inject the activationNamespaces
     * @param ans The transport ids.
     */
    public void setActivationNamespaces(Collection<String> ans) {
        setTransportIds(new ArrayList<String>(ans));
    }

    public Set<String> getUriPrefixes() {
        return URI_PREFIXES;
    }

    protected void configure(Bus b, Object bean) {
        configure(b, bean, null, null);
    }

    protected void configure(Bus bus, Object bean, String name, String extraName) {
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(name, bean);
            if (extraName != null) {
                configurer.configureBean(extraName, bean);
            }
        }
    }

    protected String getAddress(EndpointInfo endpointInfo) {
        String address = endpointInfo.getAddress();
        if (address.startsWith(JETTY_HTTP_PREFIX)) {
            address = address.substring(JETTY_HTTP_PREFIX.length());
        }
        return address;
    }

    @Override
    public Conduit getConduit(EndpointInfo endpointInfo, Bus bus) throws IOException {
        return getConduit(endpointInfo, endpointInfo.getTarget(), bus);
    }

    @Override
    public Conduit getConduit(EndpointInfo endpointInfo, EndpointReferenceType target, Bus bus)
            throws IOException {

        HTTPConduit conduit = null;
        // need to updated the endpointInfo
        endpointInfo.setAddress(getAddress(endpointInfo));

        conduit = getConduitFactory(bus).createConduit(bus, endpointInfo, target);

        // Spring configure the conduit.
        String address = conduit.getAddress();
        if (address != null && address.indexOf('?') != -1) {
            address = address.substring(0, address.indexOf('?'));
        }
        HTTPConduitConfigurer c1 = bus.getExtension(HTTPConduitConfigurer.class);
        if (c1 != null) {
            c1.configure(conduit.getBeanName(), address, conduit);
        }
        configure(bus, conduit, conduit.getBeanName(), address);
        conduit.finalizeConfig();
        return conduit;
    }

    private final JettyHttpClientConduitFactory getConduitFactory(Bus bus){
        return factory.updateAndGet(val->{
            if(val==null){
                HTTPConduitFactory conduitFactory=bus.getExtension(HTTPConduitFactory.class);
                if(conduitFactory instanceof JettyHttpClientConduitFactory){
                    return (JettyHttpClientConduitFactory)conduitFactory;
                }
                else{
                    return new JettyHttpClientConduitFactory(bus);
                }
            }
            else {
                return val;
            }
        });
    }
}
