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

package com.dreameddeath.core.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 29/10/2015.
 */
public class ObjectMapperFactory {
    public final static ObjectMapperFactory BASE_INSTANCE=new ObjectMapperFactory();

    private final ServiceLoader<IObjectMapperConfigurator> configurators;
    private final Map<IObjectMapperConfigurator.ConfiguratorType,ObjectMapper> objectMapperMap = new ConcurrentHashMap<>();

    public ObjectMapperFactory(){
        configurators=ServiceLoader.load(IObjectMapperConfigurator.class,Thread.currentThread().getContextClassLoader());
    }

    private ObjectMapper build(IObjectMapperConfigurator.ConfiguratorType type){
        synchronized (this.objectMapperMap) {
            ObjectMapper mapper = new ObjectMapper();

            List<Class<? extends IObjectMapperConfigurator>> configuratorRunned=new LinkedList<>();
            List<IObjectMapperConfigurator> listPostponedConfigurator=new LinkedList<>();
            for (IObjectMapperConfigurator configurator : configurators){
                listPostponedConfigurator.add(configurator);
            }
            while(listPostponedConfigurator.size()>0){
                Iterator<IObjectMapperConfigurator> iterator=listPostponedConfigurator.iterator();
                while(iterator.hasNext()){
                    IObjectMapperConfigurator configurator = iterator.next();
                    boolean toSkip=false;
                    for(Class<? extends IObjectMapperConfigurator> prerequisite:configurator.after()){
                        if(!configuratorRunned.contains((prerequisite))){
                            listPostponedConfigurator.add(configurator);
                            toSkip=true;
                            break;
                        }
                    }
                    if(toSkip){
                        continue;
                    }
                    if (configurator.applicable(type)) {
                        configurator.configure(mapper, type);
                    }
                    configuratorRunned.add(configurator.getClass());
                    iterator.remove();
                }
            }

            return mapper;
        }
    }

    public ObjectMapper getMapper(){
        return getMapper(BaseConfigurator.BASE_TYPE);
    }

    public ObjectMapper getMapper(IObjectMapperConfigurator.ConfiguratorType type){
        return objectMapperMap.computeIfAbsent(type,this::build);
    }

    public void reload(){
        configurators.reload();
    }
}
