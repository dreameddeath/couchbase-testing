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

package com.dreameddeath.core.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Christophe Jeunesse on 29/10/2015.
 */
public class BaseObjectMapperConfigurator implements IObjectMapperConfigurator{
    public static final ConfiguratorType BASE_TYPE=ConfiguratorType.build("default-type");


    @Override
    public List<ConfiguratorType> managedTypes() {
        return Collections.singletonList(BASE_TYPE);
    }

    @Override
    public List<Class<? extends IObjectMapperConfigurator>> after() {
        return Collections.emptyList();
    }

    @Override
    public boolean applicable(ConfiguratorType type) {
        return type.contains(BASE_TYPE);
    }

    @Override
    public void configure(ObjectMapper mapper, ConfiguratorType type) {
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //mapper.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        mapper.setTimeZone(TimeZone.getDefault());
        mapper.registerModule(new JodaModule());
        mapper.setAnnotationIntrospector(new DefaultGetterSetterIntrospector());
    }
}
