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
import org.eclipse.jetty.server.ServerConnector;

/**
 * Created by Christophe Jeunesse on 18/08/2015.
 */
public class StandardDaemonRestEndPointDescription implements IRestEndPointDescription {
    private final ServerConnector _connector;
    private final String _path;

    public StandardDaemonRestEndPointDescription(ServerConnector connector,String path) {
        _connector = connector;
        _path = path;
    }

    public StandardDaemonRestEndPointDescription(ServerConnector connector) {
        this(connector,"");
    }

    @Override
    public int port() {
        return ServerConnectorUtils.getConnectorPort(_connector);
    }

    @Override
    public String path() {
        return _path;
    }

    @Override
    public String host() {
        return ServerConnectorUtils.getConnectorHost(_connector);
    }
}
