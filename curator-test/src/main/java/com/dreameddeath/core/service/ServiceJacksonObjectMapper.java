package com.dreameddeath.core.service;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import java.util.TimeZone;

/**
 * Created by CEAJ8230 on 14/01/2015.
 */
public class ServiceJacksonObjectMapper extends ObjectMapper {
    public ServiceJacksonObjectMapper(){
        super();
        registerModule(new JodaModule());
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS,false);
        configure(SerializationFeature.WRITE_NULL_MAP_VALUES,false);
        setTimeZone(TimeZone.getDefault());
        disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        registerModule(new JodaModule());
    }
}
