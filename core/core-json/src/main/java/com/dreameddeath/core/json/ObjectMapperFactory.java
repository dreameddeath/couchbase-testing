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

/**
 * Created by Christophe Jeunesse on 29/10/2015.
 */
public class ObjectMapperFactory {
    public final static ObjectMapperFactory BASE_INSTANCE=new ObjectMapperFactory();

    private final ServiceLoader<IObjectMapperConfigurator> configurators;
    private final Map<IObjectMapperConfigurator.ConfiguratorType,ObjectMapper> objectMapperMap = new HashMap<>();


    public ObjectMapperFactory(){
        configurators=ServiceLoader.load(IObjectMapperConfigurator.class,Thread.currentThread().getContextClassLoader());
    }

    private IObjectMapperConfigurator.ConfiguratorType getType(String name){
        for (IObjectMapperConfigurator configurator : configurators){
            for(IObjectMapperConfigurator.ConfiguratorType cfgType: configurator.managedTypes()){
                if(cfgType.getName().equals(name)){
                    return cfgType;
                }
            }
        }
        throw new IllegalArgumentException("Cannot find configurator type :"+name);
    }

    private void configure(ObjectMapper mapper,IObjectMapperConfigurator.ConfiguratorType type){
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
    }

    public ObjectMapper getMapper(){
        return getMapper(BaseObjectMapperConfigurator.BASE_TYPE);
    }

    public ObjectMapper getMapper(String name){
        return getMapper(getType(name));
    }

    public ObjectMapper getMapper(IObjectMapperConfigurator.ConfiguratorType type){
        ObjectMapper result = objectMapperMap.get(type);
        if(result==null){
            synchronized (objectMapperMap){
                if(!objectMapperMap.containsKey(type)){
                    ObjectMapper mapper = new ObjectMapper();
                    objectMapperMap.put(type,mapper);
                    configure(mapper,type);
                }
            }
        }
        return objectMapperMap.get(type);
    }



    public void reload(){
        configurators.reload();
    }
}
