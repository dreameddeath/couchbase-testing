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

package com.dreameddeath.core.config;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import org.apache.commons.configuration.BaseConfiguration;

/**
 * Created by Christophe Jeunesse on 20/01/2015.
 */
public class ConfigManagerFactory {
    private final static BaseConfiguration defaultValueConfig;
    static{
        //*TODO manage configuration overloading
        defaultValueConfig = new BaseConfiguration();

        ConcurrentCompositeConfiguration myConfiguration =
                (ConcurrentCompositeConfiguration) DynamicPropertyFactory.getInstance().getBackingConfigurationSource();

        if(ConfigurationManager.getConfigInstance() instanceof ConcurrentCompositeConfiguration){
            ConcurrentCompositeConfiguration config = (ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance();
            config.addConfiguration(defaultValueConfig);
        }
        myConfiguration.addConfigurationListener(configurationEvent -> {
            if(configurationEvent.isBeforeUpdate()){
                String name=configurationEvent.getPropertyName();
                Object value=configurationEvent.getPropertyValue();
                ConfigPropertyFactory.fireCallback(name, value);
            }
        });

    }

    public static void addConfigurationEntry(String entry,Object value){ConfigurationManager.getConfigInstance().setProperty(entry, value);}
    public static void addDefaultConfigurationEntry(String entry,Object value){defaultValueConfig.setProperty(entry, value);}
}
