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

package com.dreameddeath.infrastructure.daemon.metrics;

import com.codahale.metrics.Timer;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.util.component.ContainerLifeCycle;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 29/11/2015.
 */
public class InstrumentedConnectionFactory extends ContainerLifeCycle implements ConnectionFactory {
    private final ConnectionFactory connectionFactory;
    private final Timer timer;

    public InstrumentedConnectionFactory(ConnectionFactory connectionFactory, Timer timer) {
        this.connectionFactory = connectionFactory;
        this.timer = timer;
        addBean(connectionFactory);
    }

    @Override
    public String getProtocol() {
        return connectionFactory.getProtocol();
    }

    @Override
    public List<String> getProtocols() {
        return connectionFactory.getProtocols();
    }

    @Override
    public Connection newConnection(Connector connector, EndPoint endPoint) {
        final Connection connection = connectionFactory.newConnection(connector, endPoint);
        connection.addListener(new Connection.Listener() {
            private Timer.Context context;

            @Override
            public void onOpened(Connection connection) {
                this.context = timer.time();
            }

            @Override
            public void onClosed(Connection connection) {
                context.stop();
            }
        });
        return connection;
    }
}
