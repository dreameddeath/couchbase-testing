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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Christophe Jeunesse on 29/10/2015.
 */
public interface IObjectMapperConfigurator {
    List<Class<? extends IObjectMapperConfigurator>> after();
    boolean applicable(ConfiguratorType type);
    void configure(ObjectMapper mapper,ConfiguratorType type);
    List<ConfiguratorType> managedTypes();


    class ConfiguratorType{
        private final Set<ConfiguratorType> parents=new HashSet<>();
        private final String name;

        private ConfiguratorType(String name,ConfiguratorType ... parents){
            this.name = name;
            for(ConfiguratorType parent:parents) {
                this.parents.add(parent);
                this.parents.addAll(parent.parents);
            }
        }

        public String getName() {
            return name;
        }

        public boolean contains(ConfiguratorType type){
            return type.equals(this) || parents.contains(type);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConfiguratorType that = (ConfiguratorType) o;

            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        public static ConfiguratorType build(String name,ConfiguratorType ...parents){
            return new ConfiguratorType(name,parents);
        }
    }
}
