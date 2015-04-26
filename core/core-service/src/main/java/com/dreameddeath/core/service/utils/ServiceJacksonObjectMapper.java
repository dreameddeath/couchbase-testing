/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import java.util.TimeZone;

/**
 * Created by CEAJ8230 on 14/01/2015.
 */
public class ServiceJacksonObjectMapper extends ObjectMapper {
    private static ServiceJacksonObjectMapper OBJECT_MAPPER=null;

    synchronized public static ServiceJacksonObjectMapper getInstance(){
        if(OBJECT_MAPPER==null){
            OBJECT_MAPPER = new ServiceJacksonObjectMapper();
        }
        return OBJECT_MAPPER;
    }


    private ServiceJacksonObjectMapper(){
        super();
        registerModule(new JodaModule());
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        setTimeZone(TimeZone.getDefault());
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        registerModule(new JodaModule());
    }



}
