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

package com.dreameddeath.core.config.spring;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.IConfigProperty;
import org.springframework.core.env.PropertySource;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Christophe Jeunesse on 09/10/2015.
 */
public class ConfigPropertySource extends PropertySource {
    public static final Object LOCK=new Object();
    public static boolean classesPreloaded=false;

    public static void preloadConfigClasses(){
        synchronized (LOCK) {
            if (!classesPreloaded) {
                ConfigPropertyFactory.preloadConfigClasses();
                classesPreloaded = true;
            }
        }
    }

    private ConcurrentMap<String,IConfigProperty<String>> propertyMap = new ConcurrentHashMap<>();



    public ConfigPropertySource(String name) {
        super(name);
        preloadConfigClasses();
    }

    @Override
    public String getProperty(String name) {
        return propertyMap.computeIfAbsent(name,s-> ConfigPropertyFactory.getStringProperty(name,(String)null)).getValue();
    }
}
