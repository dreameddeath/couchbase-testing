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

package com.dreameddeath.core.transcoder.json;

import com.dreameddeath.core.json.BaseObjectMapperConfigurator;
import com.dreameddeath.core.json.IObjectMapperConfigurator;
import com.dreameddeath.core.model.entity.EntityVersionUpgradeManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 29/10/2015.
 */
public class CouchbaseDocumentObjectMapperConfigurator implements IObjectMapperConfigurator {
    public static final ConfiguratorType BASE_COUCHBASE_TYPE = ConfiguratorType.build("couchbase", BaseObjectMapperConfigurator.BASE_TYPE);
    public static final ConfiguratorType BASE_COUCHBASE_STORAGE = ConfiguratorType.build("couchbase-storage", BASE_COUCHBASE_TYPE);
    public static final ConfiguratorType BASE_COUCHBASE_INTERNAL = ConfiguratorType.build("couchbase-internal", BASE_COUCHBASE_TYPE);
    public static final ConfiguratorType BASE_COUCHBASE_PUBLIC = ConfiguratorType.build("couchbase-public", BASE_COUCHBASE_TYPE);

    private static final List<Class<? extends IObjectMapperConfigurator>> prerequisites = Arrays.asList(BaseObjectMapperConfigurator.class);

    @Override
    public List<ConfiguratorType> managedTypes() {
        return Arrays.asList(BASE_COUCHBASE_TYPE,BASE_COUCHBASE_INTERNAL,BASE_COUCHBASE_PUBLIC,BASE_COUCHBASE_STORAGE);
    }


    @Override
    public List<Class<? extends IObjectMapperConfigurator>> after() {
        return prerequisites;
    }

    @Override
    public boolean applicable(ConfiguratorType type) {
        return type.contains(BaseObjectMapperConfigurator.BASE_TYPE);
    }

    @Override
    public void configure(ObjectMapper mapper, ConfiguratorType type) {
        mapper.setConfig(mapper.getDeserializationConfig().withAttribute(EntityVersionUpgradeManager.class, new EntityVersionUpgradeManager()));
        if(type.contains(BASE_COUCHBASE_STORAGE)){
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
            mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
        mapper.setAnnotationIntrospector(new CouchbaseDocumentIntrospector());
        mapper.registerModule(new CouchbaseDocumentModule());
    }
}
