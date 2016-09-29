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

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.eclipse.jetty.http.BadMessageException;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;

/**
 * Created by Christophe Jeunesse on 30/09/2016.
 */
public class InstrumentedConnectionFactoryWithUpgrading<T extends ConnectionFactory & ConnectionFactory.Upgrading> extends InstrumentedConnectionFactory implements ConnectionFactory.Upgrading {
    private final T connectionFactory;
    private final Counter upgradesCounter;
    public InstrumentedConnectionFactoryWithUpgrading(T connectionFactory, MetricRegistry registry, String prefix) {
        super(connectionFactory, registry,prefix);
        this.connectionFactory=connectionFactory;
        upgradesCounter=registry.counter(prefix+".contections_upgrates");
    }

    @Override
    public Connection upgradeConnection(Connector connector, EndPoint endPoint, MetaData.Request upgradeRequest, HttpFields responseFields) throws BadMessageException {
        upgradesCounter.inc();
        Connection connection=connectionFactory.upgradeConnection(connector, endPoint, upgradeRequest, responseFields);
        addListener(connection);
        return connection;
    }
}
