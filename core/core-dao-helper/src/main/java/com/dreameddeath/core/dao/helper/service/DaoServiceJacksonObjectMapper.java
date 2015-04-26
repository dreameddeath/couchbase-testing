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

package com.dreameddeath.core.dao.helper.service;

import com.dreameddeath.core.transcoder.json.CouchbaseBusinessDocumentDeserializerModifier;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * Created by CEAJ8230 on 14/01/2015.
 */
public class DaoServiceJacksonObjectMapper extends ObjectMapper {
    private static Map<CouchbaseDocumentIntrospector.Domain,DaoServiceJacksonObjectMapper> OBJECT_MAPPER_PER_DOMAIN=new TreeMap<>();

    synchronized public static DaoServiceJacksonObjectMapper getInstance(CouchbaseDocumentIntrospector.Domain domain){
        if(!OBJECT_MAPPER_PER_DOMAIN.containsKey(domain)){
            OBJECT_MAPPER_PER_DOMAIN.put(domain,new DaoServiceJacksonObjectMapper(domain));
        }
        return OBJECT_MAPPER_PER_DOMAIN.get(domain);
    }


    private DaoServiceJacksonObjectMapper(CouchbaseDocumentIntrospector.Domain domain){
        super();
        registerModule(new JodaModule());
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        setTimeZone(TimeZone.getDefault());
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        registerModule(new JodaModule());

        setAnnotationIntrospector(new CouchbaseDocumentIntrospector());

        registerModule(new SimpleModule() {
            protected CouchbaseBusinessDocumentDeserializerModifier modifier = new CouchbaseBusinessDocumentDeserializerModifier();

            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                if (modifier != null) {
                    context.addBeanDeserializerModifier(modifier);
                }
            }
        });
    }
}
