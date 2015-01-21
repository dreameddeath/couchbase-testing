package com.dreamddeath.core.config;

import com.netflix.config.*;

/**
 * Created by CEAJ8230 on 20/01/2015.
 */
public class ConfigManagerFactory {
    static public final String CONFIGURATION_PROPERTY_PREFIX="com.dreameddeath";

    //static final private ConcurrentMapConfiguration _BASE_OVERRIDING_CONFIGURATION = new ConcurrentMapConfiguration();
    static{
        //*TODO manage configuration overloading
        ConcurrentCompositeConfiguration myConfiguration =
                (ConcurrentCompositeConfiguration) DynamicPropertyFactory.getInstance().getBackingConfigurationSource();
        //myConfiguration.addConfiguration(_BASE_OVERRIDING_CONFIGURATION);
        //ConfigurationManager.getConfigInstance().setProperty("for","bar");

    }

    static String buildFullName(String name){return CONFIGURATION_PROPERTY_PREFIX+"."+name;}


    static void addConfigurationEntry(String entry,Object value){ConfigurationManager.getConfigInstance().setProperty(buildFullName(entry), value);}
    static void addRawConfigurationEntry(String entry,Object value){ConfigurationManager.getConfigInstance().setProperty(entry, value);}

    static DynamicBooleanProperty getRawBooleanProperty(String name,boolean defaultValue,Runnable callback) {  return DynamicPropertyFactory.getInstance().getBooleanProperty(name, defaultValue, callback);}
    static DynamicBooleanProperty getRawBooleanProperty(String name,boolean defaultValue) {  return getRawBooleanProperty(name, defaultValue, null);}
    static DynamicBooleanProperty getBooleanProperty(String name,boolean defaultValue,Runnable callback) {  return getRawBooleanProperty(buildFullName(name), defaultValue, callback);}
    static DynamicBooleanProperty getBooleanProperty(String name,boolean defaultValue) {  return getBooleanProperty(name, defaultValue, null);}

    static DynamicIntProperty getRawIntProperty(String name,int defaultValue,Runnable callback) {  return DynamicPropertyFactory.getInstance().getIntProperty(name, defaultValue, callback);}
    static DynamicIntProperty getRawIntProperty(String name,int defaultValue) {  return getRawIntProperty(name, defaultValue, null);}
    static DynamicIntProperty getIntProperty(String name,int defaultValue,Runnable callback) {  return getRawIntProperty(buildFullName(name), defaultValue, callback);}
    static DynamicIntProperty getIntProperty(String name,int defaultValue) {  return getIntProperty(name, defaultValue, null);}

    static DynamicLongProperty getRawLongProperty(String name,long defaultValue,Runnable callback) {  return DynamicPropertyFactory.getInstance().getLongProperty(name, defaultValue, callback);}
    static DynamicLongProperty getRawLongProperty(String name,long defaultValue) {  return getRawLongProperty(name, defaultValue, null);}
    static DynamicLongProperty getLongProperty(String name,long defaultValue,Runnable callback) {  return getRawLongProperty(buildFullName(name), defaultValue, callback);}
    static DynamicLongProperty getLongProperty(String name,long defaultValue) {  return getLongProperty(name, defaultValue, null);}

    static DynamicStringProperty getRawStringProperty(String name,String defaultValue,Runnable callback) {  return DynamicPropertyFactory.getInstance().getStringProperty(name, defaultValue, callback);}
    static DynamicStringProperty getRawStringProperty(String name,String defaultValue) {  return getRawStringProperty(name, defaultValue, null);}
    static DynamicStringProperty getStringProperty(String name,String defaultValue,Runnable callback) {  return getRawStringProperty(buildFullName(name), defaultValue, callback);}
    static DynamicStringProperty getStringProperty(String name,String defaultValue) {  return getStringProperty(name, defaultValue, null);}

    static DynamicFloatProperty getRawFloatProperty(String name,float defaultValue,Runnable callback) {  return DynamicPropertyFactory.getInstance().getFloatProperty(name, defaultValue, callback);}
    static DynamicFloatProperty getRawFloatProperty(String name,float defaultValue) {  return getRawFloatProperty(name, defaultValue, null);}
    static DynamicFloatProperty getFloatProperty(String name,float defaultValue,Runnable callback) {  return getRawFloatProperty(buildFullName(name), defaultValue, callback);}
    static DynamicFloatProperty getFloatProperty(String name,float defaultValue) {  return getFloatProperty(name, defaultValue, null);}

    static DynamicDoubleProperty getRawDoubleProperty(String name,double defaultValue,Runnable callback) {  return DynamicPropertyFactory.getInstance().getDoubleProperty(name, defaultValue, callback);}
    static DynamicDoubleProperty getRawDoubleProperty(String name,double defaultValue) {  return getRawDoubleProperty(name, defaultValue, null);}
    static DynamicDoubleProperty getDoubleProperty(String name,double defaultValue,Runnable callback) {  return getRawDoubleProperty(buildFullName(name), defaultValue, callback);}
    static DynamicDoubleProperty getDoubleProperty(String name,double defaultValue) {  return getDoubleProperty(name, defaultValue, null);}

    static <T> DynamicContextualProperty<T> getRawContextualProperty(String name,T defaultValue,Runnable callback) {  return DynamicPropertyFactory.getInstance().getContextualProperty(name, defaultValue, callback);}
    static <T> DynamicContextualProperty<T> getRawContextualProperty(String name,T defaultValue) {  return getRawContextualProperty(name, defaultValue, null);}
    static <T> DynamicContextualProperty<T> getContextualProperty(String name,T defaultValue,Runnable callback) {  return getRawContextualProperty(buildFullName(name), defaultValue, callback);}
    static <T> DynamicContextualProperty<T> getContextualProperty(String name,T defaultValue) {  return getContextualProperty(name, defaultValue, null);}

}
