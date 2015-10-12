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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import io.swagger.models.Model;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.util.ModelDeserializer;
import io.swagger.util.ParameterDeserializer;
import io.swagger.util.PropertyDeserializer;
import io.swagger.util.SecurityDefinitionDeserializer;

import java.util.TimeZone;

/**
 * Created by Christophe Jeunesse on 31/03/2015.
 */
public class ServiceInstanceJacksonMapper extends ObjectMapper {
    private static ServiceInstanceJacksonMapper OBJECT_MAPPER=null;

    synchronized public static ServiceInstanceJacksonMapper getInstance(){
        if(OBJECT_MAPPER==null){
            OBJECT_MAPPER = new ServiceInstanceJacksonMapper();
        }
        return OBJECT_MAPPER;
    }


    private ServiceInstanceJacksonMapper(){
        super();
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS,false);
        configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        setSerializationInclusion(JsonInclude.Include.NON_NULL);
        setTimeZone(TimeZone.getDefault());
        //disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        registerModule(new JodaModule());
        SimpleModule swaggerModule = new SimpleModule();
        swaggerModule.addDeserializer(Property.class, new PropertyDeserializer());
        swaggerModule.addDeserializer(Model.class, new ModelDeserializer());
        swaggerModule.addDeserializer(Parameter.class, new ParameterDeserializer());
        swaggerModule.addDeserializer(SecuritySchemeDefinition.class, new SecurityDefinitionDeserializer());
        registerModule(swaggerModule);
    }
}
