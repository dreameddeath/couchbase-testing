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

package com.dreameddeath.infrastructure.daemon.utils;

import org.eclipse.jetty.server.ServerConnector;

import java.net.InetAddress;

/**
 * Created by Christophe Jeunesse on 19/09/2015.
 */
public class ServerConnectorUtils {
    public static String getConnectorHost(ServerConnector connector) {
        try {
            if (connector.getHost() != null) {
                return connector.getHost();
            }
            else {
                return InetAddress.getLocalHost().getHostAddress();
            }
        } catch (Exception e) {
            return "localhost";
        }
    }

    public static String getConnectorPortString(ServerConnector connector){
        return String.valueOf(getConnectorPort(connector));
    }

    public static int getConnectorPort(ServerConnector connector) {
        return connector.getLocalPort();
    }
}