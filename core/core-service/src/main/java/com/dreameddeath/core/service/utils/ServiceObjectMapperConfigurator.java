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

package com.dreameddeath.core.service.utils;

import com.dreameddeath.core.json.BaseObjectMapperConfigurator;
import com.dreameddeath.core.json.IObjectMapperConfigurator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.models.Model;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.util.*;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 02/11/2015.
 */
public class ServiceObjectMapperConfigurator implements IObjectMapperConfigurator {
    public static ConfiguratorType SERVICE_MAPPER_CONFIGURATOR=ConfiguratorType.build("service", BaseObjectMapperConfigurator.BASE_TYPE);

    @Override
    public List<ConfiguratorType> managedTypes() {
        return Arrays.asList(SERVICE_MAPPER_CONFIGURATOR);
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
        SimpleModule swaggerModule = new SimpleModule();
        swaggerModule.addDeserializer(Path.class, new PathDeserializer());
        swaggerModule.addDeserializer(Response.class, new ResponseDeserializer());
        swaggerModule.addDeserializer(Property.class, new PropertyDeserializer());
        swaggerModule.addDeserializer(Model.class, new ModelDeserializer());
        swaggerModule.addDeserializer(Parameter.class, new ParameterDeserializer());
        swaggerModule.addDeserializer(SecuritySchemeDefinition.class, new SecurityDefinitionDeserializer());
        mapper.registerModule(swaggerModule);

        SimpleModule serviceInstanceModule = new SimpleModule();
        serviceInstanceModule.setMixInAnnotation(ServiceInstance.class,ServiceInstanceMixIn.class);
        mapper.registerModule(serviceInstanceModule);
    }
}
