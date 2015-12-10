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

package com.dreameddeath.infrastructure.daemon.metrics;

import com.codahale.metrics.MetricRegistry;
import org.apache.cxf.Bus;
import org.apache.cxf.metrics.codahale.CodahaleMetricsProvider;

/**
 * Created by Christophe Jeunesse on 30/11/2015.
 */
public class CxfMetricsProvider extends CodahaleMetricsProvider {

    private static  Bus updateBus(Bus bus,MetricRegistry registry){
        bus.setExtension(registry,MetricRegistry.class);
        return bus;
    }

    public CxfMetricsProvider(Bus bus, MetricRegistry registry){
        super(updateBus(bus,registry));
    }
}
