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

package com.dreameddeath.infrastructure.daemon.services;

import com.dreameddeath.core.service.registrar.IEndPointDescription;
import com.dreameddeath.infrastructure.daemon.utils.ServerConnectorUtils;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import com.google.common.collect.ImmutableSet;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.ServerConnector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 18/08/2015.
 */
public class StandardDaemonRestEndPointDescription implements IEndPointDescription {
    private static final List<String> HTTP2_PROTOCOLS= Arrays.asList("h2c","h2");
    private static final List<String> HTTP1_PROTOCOLS= Collections.singletonList(HttpVersion.HTTP_1_1.asString());
    private static final AtomicInteger instanceCounter=new AtomicInteger();

    private final AbstractWebServer server;
    private final ServerConnector connector;
    private final ServerConnector securedConnector;
    private final String path;
    private final Set<Protocol> protocols;

    public StandardDaemonRestEndPointDescription(AbstractWebServer server,String path) {
        this.server = server;
        this.connector = server.getServerConnector();
        this.securedConnector = server.getSecuredServerConnector();
        this.path = path;
        List<Protocol> connectorProtocols=new ArrayList<>();
        for (ConnectionFactory connectionFactory:connector.getConnectionFactories()){
            String protocol=connectionFactory.getProtocol();
            if(HTTP2_PROTOCOLS.contains(protocol)){
                connectorProtocols.add(Protocol.HTTP_2);
            }
            else if(HTTP1_PROTOCOLS.contains(protocol)){
                connectorProtocols.add(Protocol.HTTP_1);
            }
        }

        protocols= ImmutableSet.copyOf(connectorProtocols);
    }

    public StandardDaemonRestEndPointDescription(AbstractWebServer server) {
        this(server,"");
    }

    @Override
    public int port() {
        return ServerConnectorUtils.getConnectorPort(connector);
    }

    @Override
    public Integer securedPort() {
        return securedConnector!=null?ServerConnectorUtils.getConnectorPort(securedConnector):null;
    }

    @Override
    public Set<Protocol> protocols() {
        return protocols;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String host() {
        return ServerConnectorUtils.getConnectorHost(connector);
    }

    @Override
    public String daemonUid() {
        return server.getParentDaemon().getUuid().toString();
    }

    @Override
    public String webserverUid() {
        return server.getUuid().toString();
    }

    @Override
    public String buildInstanceUid(){
        return IEndPointDescription.Utils.buildUid(this,instanceCounter.incrementAndGet());
    }
}
