/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.config;

import com.dreameddeath.core.config.annotation.ConfigPropertyClassReference;
import com.dreameddeath.core.config.impl.*;
import com.dreameddeath.core.java.utils.StringUtils;
import com.netflix.config.DynamicPropertyFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Christophe Jeunesse on 04/02/2015.
 */
public class ConfigPropertyFactory {
    public static final String LISTING_CONFIG_FILE="META-INF/core-config/configClasses";
    private static ConfigPropertyFactory INSTANCE=null;
    private final static ConcurrentMap<String, List<IConfigProperty>> mapCallbackPerProperty = new ConcurrentHashMap<>();

    public static synchronized ConfigPropertyFactory getInstance(){
        if(INSTANCE==null){
            INSTANCE = new ConfigPropertyFactory();
        }
        return INSTANCE;
    }

    private static void preloadConfigClass(String name,List<String>preloadedClasses){
        try {
            if(preloadedClasses.contains(name)) return;
            Class configClass = Thread.currentThread().getContextClassLoader().loadClass(name);
            ConfigPropertyClassReference refAnnot = (ConfigPropertyClassReference) configClass.getAnnotation(ConfigPropertyClassReference.class);
            if ((refAnnot != null) && refAnnot.value() != null) {
                for (int refPos = 0; refPos < refAnnot.value().length; ++refPos) {
                    String className = refAnnot.value()[refPos].getName();
                    if (!preloadedClasses.contains(className)) {
                        preloadConfigClass(className, preloadedClasses);
                    }
                }
            }
            configClass.newInstance();
            preloadedClasses.add(name);
        }
        catch (ClassNotFoundException|InstantiationException|IllegalAccessException e){
            throw new RuntimeException();
        }
    }

    public static void preloadConfigClasses(){
        try {
            List<String> initializedClasses = new ArrayList<>();
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(ConfigPropertyFactory.LISTING_CONFIG_FILE);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!StringUtils.isEmpty(line)) {
                        if (!initializedClasses.contains(line)) {
                            preloadConfigClass(line, initializedClasses);
                        }
                    }
                }
            }
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    private final DynamicPropertyFactory dynamicPropertyFactory;


    public ConfigPropertyFactory(){
        dynamicPropertyFactory = DynamicPropertyFactory.getInstance();
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
        if (!ConfigPropertyFactory.mapCallbackPerProperty.containsKey(prop.getName())) {
            ConfigPropertyFactory.mapCallbackPerProperty.putIfAbsent(prop.getName(), new CopyOnWriteArrayList<>());
        }

        ConfigPropertyFactory.mapCallbackPerProperty.get(prop.getName()).add(prop);
    }

    protected static void removePropertyFromGlobalMap(IConfigProperty prop) {
        if (!ConfigPropertyFactory.mapCallbackPerProperty.containsKey(prop.getName())) {
            ConfigPropertyFactory.mapCallbackPerProperty.putIfAbsent(prop.getName(), new CopyOnWriteArrayList<>());
        }
        ConfigPropertyFactory.mapCallbackPerProperty.get(prop.getName()).remove(prop);
    }

    public static void fireCallback(String name, Object value) {
        if(name==null){
            return;
        }
        if (ConfigPropertyFactory.mapCallbackPerProperty.containsKey(name)) {
            for (IConfigProperty<?> prop : ConfigPropertyFactory.mapCallbackPerProperty.get(name)) {
                for (ConfigPropertyChangedCallback callback : prop.getCallbacks()) {
                    callback.onChange(prop, prop.getValue(), value);
                }
            }
        }
    }

}
