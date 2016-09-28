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
public class JettyHttp2TransportFactory extends AbstractTransportFactory implements ConduitInitiator {
    public static final List<String> DEFAULT_NAMESPACES = Arrays
            .asList("http://cxf.apache.org/transports/http/http2-jetty-client");

    /**
     * This constant holds the prefixes served by this factory.
     */
    private static final Set<String> URI_PREFIXES = new HashSet<String>();

    public static final String JETTY_HTTP2_PREFIX = "jh2://";

    static {
        URI_PREFIXES.add(JETTY_HTTP2_PREFIX);
    }

    private final AtomicReference<JettyHttp2ConduitFactory> factory = new AtomicReference<>();

    public JettyHttp2TransportFactory() {
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
        if (address.startsWith(JETTY_HTTP2_PREFIX)) {
            address = address.substring(JETTY_HTTP2_PREFIX.length());
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

    private final JettyHttp2ConduitFactory getConduitFactory(Bus bus){
        return factory.updateAndGet(val->{
            if(val==null){
                HTTPConduitFactory conduitFactory=bus.getExtension(HTTPConduitFactory.class);
                if(conduitFactory instanceof JettyHttp2ConduitFactory){
                    return (JettyHttp2ConduitFactory)conduitFactory;
                }
                else{
                    return new JettyHttp2ConduitFactory(bus);
                }
            }
            else {
                return val;
            }
        });
    }
}
