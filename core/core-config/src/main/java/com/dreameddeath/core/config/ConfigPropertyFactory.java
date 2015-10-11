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

import com.dreameddeath.core.config.impl.*;
import com.netflix.config.DynamicPropertyFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Christophe Jeunesse on 04/02/2015.
 */
public class ConfigPropertyFactory {
    private static ConfigPropertyFactory INSTANCE=null;
    private static ConcurrentMap<String, List<IConfigProperty>> _mapCallbackPerProperty = new ConcurrentHashMap<>();

    public static synchronized ConfigPropertyFactory getInstance(){
        if(INSTANCE==null){
            INSTANCE = new ConfigPropertyFactory();
        }
        return INSTANCE;
    }
    private static final ConcurrentHashMap<String, ConfigPropertyFactory> ALL_PROPS
            = new ConcurrentHashMap<>();



    private DynamicPropertyFactory _dynamicPropertyFactory;


    public ConfigPropertyFactory(){
        _dynamicPropertyFactory = DynamicPropertyFactory.getInstance();
    }


    public static BooleanConfigProperty getBooleanProperty(String name,boolean defaultValue,ConfigPropertyChangedCallback<Boolean> callback) {  return new BooleanConfigProperty(name, defaultValue,callback);}
    public static BooleanConfigProperty getBooleanProperty(String name,boolean defaultValue) {  return new BooleanConfigProperty(name, defaultValue);}
    public static BooleanConfigProperty getBooleanProperty(String name,IConfigProperty<Boolean> defaultValueRef,ConfigPropertyChangedCallback<Boolean> callback) {  return new BooleanConfigProperty(name, defaultValueRef,callback);}
    public static BooleanConfigProperty getBooleanProperty(String name,IConfigProperty<Boolean> defaultValueRef) {  return new BooleanConfigProperty(name, defaultValueRef);}



    public static IntConfigProperty getIntProperty(String name,int defaultValue,ConfigPropertyChangedCallback<Integer> callback) {  return new IntConfigProperty(name, defaultValue,callback);}
    public static IntConfigProperty getIntProperty(String name,int defaultValue) {  return new IntConfigProperty(name, defaultValue);}
    public static IntConfigProperty getIntProperty(String name,IConfigProperty<Integer> defaultValueRef,ConfigPropertyChangedCallback<Integer> callback) {  return new IntConfigProperty(name, defaultValueRef,callback);}
    public static IntConfigProperty getIntProperty(String name,IConfigProperty<Integer> defaultValueRef) {  return new IntConfigProperty(name, defaultValueRef);}

    
    public static LongConfigProperty getLongProperty(String name,long defaultValue,ConfigPropertyChangedCallback<Long> callback) {  return new LongConfigProperty(name, defaultValue,callback);}
    public static LongConfigProperty getLongProperty(String name,long defaultValue) {  return new LongConfigProperty(name, defaultValue);}
    public static LongConfigProperty getLongProperty(String name,IConfigProperty<Long> defaultValueRef,ConfigPropertyChangedCallback<Long> callback) {  return new LongConfigProperty(name, defaultValueRef,callback);}
    public static LongConfigProperty getLongProperty(String name,IConfigProperty<Long> defaultValueRef) {  return new LongConfigProperty(name, defaultValueRef);}


    public static FloatConfigProperty getFloatProperty(String name,float defaultValue,ConfigPropertyChangedCallback<Float> callback) {  return new FloatConfigProperty(name, defaultValue,callback);}
    public static FloatConfigProperty getFloatProperty(String name,float defaultValue) {  return new FloatConfigProperty(name, defaultValue);}
    public static FloatConfigProperty getFloatProperty(String name,IConfigProperty<Float> defaultValueRef,ConfigPropertyChangedCallback<Float> callback) {  return new FloatConfigProperty(name, defaultValueRef,callback);}
    public static FloatConfigProperty getFloatProperty(String name,IConfigProperty<Float> defaultValueRef) {  return new FloatConfigProperty(name, defaultValueRef);}


    public static DoubleConfigProperty getDoubleProperty(String name,double defaultValue,ConfigPropertyChangedCallback<Double> callback) {  return new DoubleConfigProperty(name, defaultValue,callback);}
    public static DoubleConfigProperty getDoubleProperty(String name,double defaultValue) {  return new DoubleConfigProperty(name, defaultValue);}
    public static DoubleConfigProperty getDoubleProperty(String name,IConfigProperty<Double> defaultValueRef,ConfigPropertyChangedCallback<Double> callback) {  return new DoubleConfigProperty(name, defaultValueRef,callback);}
    public static DoubleConfigProperty getDoubleProperty(String name,IConfigProperty<Double> defaultValueRef) {  return new DoubleConfigProperty(name, defaultValueRef);}

    
    public static StringConfigProperty getStringProperty(String name,String defaultValue,ConfigPropertyChangedCallback<String> callback) {  return new StringConfigProperty(name, defaultValue,callback);}
    public static StringConfigProperty getStringProperty(String name,String defaultValue) {  return new StringConfigProperty(name, defaultValue);}
    public static StringConfigProperty getStringProperty(String name) {  return new StringConfigProperty(name, (String)null);}
    public static StringConfigProperty getStringProperty(String name,IConfigProperty<String> defaultValueRef,ConfigPropertyChangedCallback<String> callback) {  return new StringConfigProperty(name, defaultValueRef,callback);}
    public static StringConfigProperty getStringProperty(String name,IConfigProperty<String> defaultValueRef) {  return new StringConfigProperty(name, defaultValueRef);}

    
    public static StringListConfigProperty getStringListProperty(String name,String defaultValue,ConfigPropertyChangedCallback<List<String>> callback) {  return new StringListConfigProperty(name, defaultValue,callback);}
    public static StringListConfigProperty getStringListProperty(String name,String defaultValue) {  return new StringListConfigProperty(name, defaultValue);}
    public static StringListConfigProperty getStringListProperty(String name) {  return new StringListConfigProperty(name, "");}
    public static StringListConfigProperty getStringListProperty(String name,AbstractConfigListProperty<String> defaultValueRef,ConfigPropertyChangedCallback<List<String>> callback) {  return new StringListConfigProperty(name, defaultValueRef,callback);}
    public static StringListConfigProperty getStringListProperty(String name,AbstractConfigListProperty<String> defaultValueRef) {  return new StringListConfigProperty(name, defaultValueRef);}


    public static <T,PTYPE extends IConfigProperty> ConfigPropertyWithTemplateName<T,PTYPE> getTemplateNameConfigProperty(Class<PTYPE> clazz,String templateName,T defaultValue){ return new ConfigPropertyWithTemplateName<>(clazz,templateName,defaultValue);}
    public static <T,PTYPE extends IConfigProperty> ConfigPropertyWithTemplateName<T,PTYPE> getTemplateNameConfigProperty(Class<PTYPE> clazz,String templateName,T defaultValue,ConfigPropertyChangedCallback<T> callback){ return new ConfigPropertyWithTemplateName<>(clazz,templateName,defaultValue,callback);}
    public static <T,PTYPE extends IConfigProperty> ConfigPropertyWithTemplateName<T,PTYPE> getTemplateNameConfigProperty(Class<PTYPE> clazz,String templateName,IConfigProperty<T> defaultValue){ return new ConfigPropertyWithTemplateName<>(clazz,templateName,defaultValue);}
    public static <T,PTYPE extends IConfigProperty> ConfigPropertyWithTemplateName<T,PTYPE> getTemplateNameConfigProperty(Class<PTYPE> clazz,String templateName,IConfigProperty<T> defaultValue,ConfigPropertyChangedCallback<T> callback){ return new ConfigPropertyWithTemplateName<>(clazz,templateName,defaultValue,callback);}
    public static <T,PTYPE extends IConfigProperty> ConfigPropertyWithTemplateName<T,PTYPE> getTemplateNameConfigProperty(Class<PTYPE> clazz,String templateName,ConfigPropertyWithTemplateName<T,PTYPE> defaultValueRefTemplate){ return new ConfigPropertyWithTemplateName<>(clazz,templateName,defaultValueRefTemplate);}
    public static <T,PTYPE extends IConfigProperty> ConfigPropertyWithTemplateName<T,PTYPE> getTemplateNameConfigProperty(Class<PTYPE> clazz,String templateName,ConfigPropertyWithTemplateName<T,PTYPE> defaultValueRefTemplate,ConfigPropertyChangedCallback<T> callback){ return new ConfigPropertyWithTemplateName<>(clazz,templateName,defaultValueRefTemplate,callback);}



    protected static void addPropertyToGlobalMap(IConfigProperty prop) {
        if (!ConfigPropertyFactory._mapCallbackPerProperty.containsKey(prop.getName())) {
            ConfigPropertyFactory._mapCallbackPerProperty.putIfAbsent(prop.getName(), new CopyOnWriteArrayList<>());
        }

        ConfigPropertyFactory._mapCallbackPerProperty.get(prop.getName()).add(prop);
    }

    protected static void removePropertyFromGlobalMap(IConfigProperty prop) {
        if (!ConfigPropertyFactory._mapCallbackPerProperty.containsKey(prop.getName())) {
            ConfigPropertyFactory._mapCallbackPerProperty.putIfAbsent(prop.getName(), new CopyOnWriteArrayList<>());
        }
        ConfigPropertyFactory._mapCallbackPerProperty.get(prop.getName()).remove(prop);
    }

    public static void fireCallback(String name, Object value) {
        if (ConfigPropertyFactory._mapCallbackPerProperty.containsKey(name)) {
            for (IConfigProperty<?> prop : ConfigPropertyFactory._mapCallbackPerProperty.get(name)) {
                for (ConfigPropertyChangedCallback callback : prop.getCallbacks()) {
                    callback.onChange(prop, prop.getValue(), value);
                }
            }
        }
    }

}
