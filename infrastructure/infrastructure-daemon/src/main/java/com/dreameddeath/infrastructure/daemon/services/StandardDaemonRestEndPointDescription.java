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

package com.dreameddeath.infrastructure.daemon.services;

import com.dreameddeath.core.service.registrar.IRestEndPointDescription;
import com.dreameddeath.infrastructure.daemon.utils.ServerConnectorUtils;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import org.eclipse.jetty.server.ServerConnector;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 18/08/2015.
 */
public class StandardDaemonRestEndPointDescription implements IRestEndPointDescription {
    private final AtomicInteger instanceCounter=new AtomicInteger();

    private final AbstractWebServer server;
    private final ServerConnector connector;
    private final String path;

    public StandardDaemonRestEndPointDescription(AbstractWebServer server,ServerConnector connector,String path) {
        this.server = server;
        this.connector = connector;
        this.path = path;
    }

    public StandardDaemonRestEndPointDescription(AbstractWebServer server,ServerConnector connector) {
        this(server,connector,"");
    }

    @Override
    public int port() {
        return ServerConnectorUtils.getConnectorPort(connector);
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
        return IRestEndPointDescription.Utils.buildUid(this,instanceCounter.incrementAndGet());
    }
}
