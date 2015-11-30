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

import com.codahale.metrics.json.MetricsModule;
import com.dreameddeath.core.json.BaseObjectMapperConfigurator;
import com.dreameddeath.core.json.IObjectMapperConfigurator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 29/11/2015.
 */
public class MetricObjectMapperConfigurator implements IObjectMapperConfigurator {


    @Override
    public List<ConfiguratorType> managedTypes() {
        return Arrays.asList(BaseObjectMapperConfigurator.BASE_TYPE);
    }

    @Override
    public List<Class<? extends IObjectMapperConfigurator>> after() {
        return Collections.emptyList();
    }

    @Override
    public boolean applicable(ConfiguratorType type) {
        return type.contains(BaseObjectMapperConfigurator.BASE_TYPE);
    }

    @Override
    public void configure(ObjectMapper mapper, ConfiguratorType type) {
        mapper.registerModule(new MetricsModule(TimeUnit.SECONDS,TimeUnit.SECONDS,true));
    }
}
