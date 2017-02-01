/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.query.factory;

import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.query.service.IQueryService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 20/12/2016.
 */
public class QueryServiceFactory {
    private final Map<EntityModelId,IQueryService<?>> queryServiceEntityMap = new ConcurrentHashMap<>();
    private final Map<Class<?>,IQueryService<?>> queryServiceClassMap = new ConcurrentHashMap<>();

    public final void registerServiceMap(Class<?> clazz,IQueryService<?> service){
        queryServiceClassMap.putIfAbsent(clazz,service);
    }

    public final void registerServiceMap(EntityModelId modelId,IQueryService<?> service){
        //queryServiceClassMap.putIfAbsent(clazz,service);
    }


    public <T> IQueryService<T> getQueryService(EntityModelId model){
        return null;
    }

    public <T> IQueryService<T> getQueryService(Class<T> clazz){
        return null;
    }

}
