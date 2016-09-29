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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 30/09/2016.
 */
public class WebServerMetrics {
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final AbstractWebServer parentServer;
    private final Slf4jReporter logReporter;
    private final Set<String> rootKeys=new TreeSet<>();
    public WebServerMetrics(AbstractWebServer parentServer) {
        this.parentServer = parentServer;

        logReporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger(WebServerMetrics.class))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    //TODO
    /*public DaemonMetricsInfo getMetrics(){
        DaemonMetricsInfo infos = new DaemonMetricsInfo();
        infos.setMetricRegistry(metricRegistry);

        for(AbstractDaemonPlugin plugin:parentDaemon.getPlugins()){
            plugin.enrichMetrics(infos);
        }

        return infos;
    }*/

    public void reportNow(){
        logReporter.report();
    }

    public void startReporter(){
        logReporter.start(1,TimeUnit.MINUTES);
    }

    public void stopReporter(){
        logReporter.stop();
    }

    public void markRootKeys(){
        rootKeys.clear();
        rootKeys.addAll(metricRegistry.getMetrics().keySet());
    }

    public void cleanKeys(){
        metricRegistry.removeMatching((name, metric) -> !rootKeys.contains(name));
    }
}
