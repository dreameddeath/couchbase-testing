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
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;
import java.io.IOException;


/**
 * Created by Christophe Jeunesse on 20/01/2015.
 */
public class ConfigManagerFactory {

    private final static BaseConfiguration defaultValueConfig;
    private final static ConcurrentCompositeConfiguration centralizedConfig;
    private final static ConcurrentCompositeConfiguration localDefault;
    private final static PropertiesConfiguration localOverride;
    private final static BaseConfiguration localTempOverride;

    public static final String LOCAL_OVERRIDE_PROPERTIES_FILENAME = "local.override.properties";

    static{
        defaultValueConfig = new BaseConfiguration();
        centralizedConfig = new ConcurrentCompositeConfiguration();
        localDefault = new ConcurrentCompositeConfiguration();
        PropertiesConfiguration newLocalOverride;
        try {
            newLocalOverride = new PropertiesConfiguration(LOCAL_OVERRIDE_PROPERTIES_FILENAME);
            newLocalOverride.setAutoSave(true);
        }
        catch(ConfigurationException e){
            File cfgFile = new File(LOCAL_OVERRIDE_PROPERTIES_FILENAME);
            if(cfgFile.exists()){
                cfgFile.delete();
            }
            try {
                cfgFile.createNewFile();
                newLocalOverride = new PropertiesConfiguration(LOCAL_OVERRIDE_PROPERTIES_FILENAME);
                newLocalOverride.setAutoSave(true);
            }
            catch(ConfigurationException|IOException newE){
                throw new RuntimeException("Cannot init configuration",newE);
            }
        }
        localOverride = newLocalOverride;
        localTempOverride=new BaseConfiguration();
        ConcurrentCompositeConfiguration myConfiguration =
                (ConcurrentCompositeConfiguration) DynamicPropertyFactory.getInstance().getBackingConfigurationSource();

        if(ConfigurationManager.getConfigInstance() instanceof ConcurrentCompositeConfiguration){
            ConcurrentCompositeConfiguration config = (ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance();
            config.addConfiguration(localTempOverride,"localOverride");
            config.addConfiguration(localOverride,"localOverride");
            config.addConfiguration(localDefault,"localDefault");
            config.addConfiguration(centralizedConfig,"centralized");
            config.addConfiguration(defaultValueConfig,"compileDefault");
        }
        myConfiguration.addConfigurationListener(configurationEvent -> {
            if(configurationEvent.isBeforeUpdate()){
                String name=configurationEvent.getPropertyName();
                Object value=configurationEvent.getPropertyValue();
                ConfigPropertyFactory.fireCallback(name, value);
            }
        });

    }

    public static void addPersistentConfigurationEntry(String entry, Object value){localOverride.setProperty(entry, value);}
    public static void addTemporaryConfigurationEntry(String entry, Object value){localTempOverride.setProperty(entry, value);}
    public static void addDefaultConfigurationEntry(String entry,Object value){defaultValueConfig.setProperty(entry, value);}
    public static void addConfiguration(AbstractConfiguration configuration,String name,PriorityDomain priority){
        switch (priority) {
            case LOCAL_DEFAULT:
                localDefault.addConfiguration(configuration,name);
                break;
            case CENTRALIZED:
                centralizedConfig.addConfiguration(configuration,name);
                break;
            default:
                throw new IllegalArgumentException("Forbidden add of configuration <"+name+"> with priority <"+priority+">");
        }
    }

    public static void cleanLocalProperty(){
        localOverride.clear();
    }

    public static void changeLocalPropertyFilename(String filename) throws ConfigurationException{
        File oldFile = localOverride.getFile();
        localOverride.setFileName(filename);
        localOverride.save(filename);
        oldFile.delete();
    }


    public static AbstractConfiguration getConfig(PriorityDomain priority){
        switch (priority) {
            case ALL:
                return ConfigurationManager.getConfigInstance();
            case COMPILE_DEFAULT:
                return defaultValueConfig;
            case LOCAL_DEFAULT:
                return localDefault;
            case CENTRALIZED:
                return centralizedConfig;
            case LOCAL_OVERRIDE:
                return localOverride;
            case LOCAL_TEMP_OVERRIDE:
                return localTempOverride;
            default:
                throw new IllegalArgumentException("Unknown priority <"+priority+">");
        }
    }

    public enum PriorityDomain{
        COMPILE_DEFAULT,
        CENTRALIZED,
        LOCAL_DEFAULT,
        LOCAL_OVERRIDE,
        LOCAL_TEMP_OVERRIDE,
        ALL
    }


}
