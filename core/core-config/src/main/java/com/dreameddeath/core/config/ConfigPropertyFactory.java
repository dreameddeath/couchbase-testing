/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.config;

import com.dreameddeath.core.config.impl.*;
import com.netflix.config.DynamicPropertyFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by CEAJ8230 on 04/02/2015.
 */
public class ConfigPropertyFactory {
    private static ConfigPropertyFactory INSTANCE=null;

    public static synchronized ConfigPropertyFactory getInstance(){
        if(INSTANCE==null){
            INSTANCE = new ConfigPropertyFactory();
        }
        return INSTANCE;
    }
    private static final ConcurrentHashMap<String, ConfigPropertyFactory> ALL_PROPS
            = new ConcurrentHashMap<String, ConfigPropertyFactory>();



    private DynamicPropertyFactory _dynamicPropertyFactory;


    public ConfigPropertyFactory(){
        _dynamicPropertyFactory = DynamicPropertyFactory.getInstance();
    }

    static String buildPropName(String name,boolean withPrefix){return withPrefix?ConfigManagerFactory.buildFullName(name):name;}

    public static BooleanConfigProperty getBooleanProperty(String name,boolean defaultValue,ConfigPropertyChangedCallback<Boolean> callback) {  return new BooleanConfigProperty(name, defaultValue,callback);}
    public static BooleanConfigProperty getBooleanProperty(String name,boolean defaultValue) {  return new BooleanConfigProperty(name, defaultValue);}
    public static BooleanConfigProperty getBooleanProperty(String name,boolean defaultValue,boolean withPrefix) {  return new BooleanConfigProperty(buildPropName(name,withPrefix), defaultValue);}
    public static BooleanConfigProperty getBooleanProperty(String name,boolean defaultValue,ConfigPropertyChangedCallback<Boolean> callback,boolean withPrefix) {  return new BooleanConfigProperty(buildPropName(name,withPrefix), defaultValue,callback);}


    public static IntConfigProperty getIntProperty(String name,int defaultValue,ConfigPropertyChangedCallback<Integer> callback) {  return new IntConfigProperty(name, defaultValue,callback);}
    public static IntConfigProperty getIntProperty(String name,int defaultValue) {  return new IntConfigProperty(name, defaultValue);}
    public static IntConfigProperty getIntProperty(String name,int defaultValue,boolean withPrefix) {  return new IntConfigProperty(buildPropName(name,withPrefix), defaultValue);}
    public static IntConfigProperty getIntProperty(String name,int defaultValue,ConfigPropertyChangedCallback<Integer> callback,boolean withPrefix) {  return new IntConfigProperty(buildPropName(name,withPrefix), defaultValue,callback);}

    public static LongConfigProperty getLongProperty(String name,long defaultValue,ConfigPropertyChangedCallback<Long> callback) {  return new LongConfigProperty(name, defaultValue,callback);}
    public static LongConfigProperty getLongProperty(String name,long defaultValue) {  return new LongConfigProperty(name, defaultValue);}
    public static LongConfigProperty getLongProperty(String name,long defaultValue,boolean withPrefix) {  return new LongConfigProperty(buildPropName(name,withPrefix), defaultValue);}
    public static LongConfigProperty getLongProperty(String name,long defaultValue,ConfigPropertyChangedCallback<Long> callback,boolean withPrefix) {  return new LongConfigProperty(buildPropName(name,withPrefix), defaultValue,callback);}


    public static FloatConfigProperty getFloatProperty(String name,float defaultValue,ConfigPropertyChangedCallback<Float> callback) {  return new FloatConfigProperty(name, defaultValue,callback);}
    public static FloatConfigProperty getFloatProperty(String name,float defaultValue) {  return new FloatConfigProperty(name, defaultValue);}
    public static FloatConfigProperty getFloatProperty(String name,float defaultValue,boolean withPrefix) {  return new FloatConfigProperty(buildPropName(name,withPrefix), defaultValue);}
    public static FloatConfigProperty getFloatProperty(String name,float defaultValue,ConfigPropertyChangedCallback<Float> callback,boolean withPrefix) {  return new FloatConfigProperty(buildPropName(name,withPrefix), defaultValue,callback);}


    public static DoubleConfigProperty getDoubleProperty(String name,double defaultValue,ConfigPropertyChangedCallback<Double> callback) {  return new DoubleConfigProperty(name, defaultValue,callback);}
    public static DoubleConfigProperty getDoubleProperty(String name,double defaultValue) {  return new DoubleConfigProperty(name, defaultValue);}
    public static DoubleConfigProperty getDoubleProperty(String name,double defaultValue,boolean withPrefix) {  return new DoubleConfigProperty(buildPropName(name,withPrefix), defaultValue);}
    public static DoubleConfigProperty getDoubleProperty(String name,double defaultValue,ConfigPropertyChangedCallback<Double> callback,boolean withPrefix) {  return new DoubleConfigProperty(buildPropName(name,withPrefix), defaultValue,callback);}

    public static StringConfigProperty getStringProperty(String name,String defaultValue,ConfigPropertyChangedCallback<String> callback) {  return new StringConfigProperty(name, defaultValue,callback);}
    public static StringConfigProperty getStringProperty(String name,String defaultValue) {  return new StringConfigProperty(name, defaultValue);}
    public static StringConfigProperty getStringProperty(String name,String defaultValue,boolean withPrefix) {  return new StringConfigProperty(buildPropName(name,withPrefix), defaultValue);}
    public static StringConfigProperty getStringProperty(String name,String defaultValue,ConfigPropertyChangedCallback<String> callback,boolean withPrefix) {  return new StringConfigProperty(buildPropName(name,withPrefix), defaultValue,callback);}

    //static BooleanProperty getBooleanProperty(String name,boolean defaultValue,PropertyChangedCallback<Boolean> callback) {  return DynamicPropertyFactory.getInstance().getBooleanProperty(name, defaultValue, callback);}

    /*static BooleanProperty getRawBooleanProperty(String name,boolean defaultValue,Runnable callback) {  return DynamicPropertyFactory.getInstance().getBooleanProperty(name, defaultValue, callback);}
    static BooleanProperty getRawBooleanProperty(String name,boolean defaultValue) {  return getRawBooleanProperty(name, defaultValue, null);}
    static BooleanProperty getBooleanProperty(String name,boolean defaultValue,Runnable callback) {  return getRawBooleanProperty(buildFullName(name), defaultValue, callback);}
    static BooleanProperty getBooleanProperty(String name,boolean defaultValue) {  return getBooleanProperty(name, defaultValue, null);}

    static IntProperty getRawIntProperty(String name,int defaultValue,Runnable callback) {  return DynamicPropertyFactory.getInstance().getIntProperty(name, defaultValue, callback);}
    static IntProperty getRawIntProperty(String name,int defaultValue) {  return getRawIntProperty(name, defaultValue, null);}
    static IntProperty getIntProperty(String name,int defaultValue,Runnable callback) {  return getRawIntProperty(buildFullName(name), defaultValue, callback);}
    static IntProperty getIntProperty(String name,int defaultValue) {  return getIntProperty(name, defaultValue, null);}

    static LongProperty getRawLongProperty(String name,long defaultValue,Runnable callback) {  return DynamicPropertyFactory.getInstance().getLongProperty(name, defaultValue, callback);}
    static LongProperty getRawLongProperty(String name,long defaultValue) {  return getRawLongProperty(name, defaultValue, null);}
    static LongProperty getLongProperty(String name,long defaultValue,Runnable callback) {  return getRawLongProperty(buildFullName(name), defaultValue, callback);}
    static LongProperty getLongProperty(String name,long defaultValue) {  return getLongProperty(name, defaultValue, null);}

    static StringProperty getRawStringProperty(String name,String defaultValue,PropertyChangedCallback<String> callback) {  return DynamicPropertyFactory.getInstance().getStringProperty(name, defaultValue, callback);}
    static StringProperty getRawStringProperty(String name,String defaultValue) {  return getRawStringProperty(name, defaultValue, null);}
    static StringProperty getStringProperty(String name,String defaultValue,PropertyChangedCallback<String> callback) {  return getRawStringProperty(buildFullName(name), defaultValue, callback);}
    static StringProperty getStringProperty(String name,String defaultValue) {  return getStringProperty(name, defaultValue, null);}

    static StringProperty getRawStringProperty(String name,string defaultValue,PropertyChangedCallback<String> callback) {  return DynamicPropertyFactory.getInstance().getStringProperty(name, defaultValue, callback);}
    static StringProperty getRawStringProperty(String name,string defaultValue) {  return getRawStringProperty(name, defaultValue, null);}
    static StringProperty getStringProperty(String name,string defaultValue,Runnable callback) {  return getRawStringProperty(buildFullName(name), defaultValue, callback);}
    static StringProperty getStringProperty(String name,string defaultValue) {  return getStringProperty(name, defaultValue, null);}

    static DoubleProperty getRawDoubleProperty(String name,double defaultValue,Runnable callback) {  return DynamicPropertyFactory.getInstance().getDoubleProperty(name, defaultValue, callback);}
    static DoubleProperty getRawDoubleProperty(String name,double defaultValue) {  return getRawDoubleProperty(name, defaultValue, null);}
    static DoubleProperty getDoubleProperty(String name,double defaultValue,Runnable callback) {  return getRawDoubleProperty(buildFullName(name), defaultValue, callback);}
    static DoubleProperty getDoubleProperty(String name,double defaultValue) {  return getDoubleProperty(name, defaultValue, null);}
*/

}
