package com.dreameddeath.core.config;

import com.dreameddeath.core.config.impl.*;
import com.netflix.config.DynamicPropertyFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by CEAJ8230 on 04/02/2015.
 */
public class PropertyFactory {
    private static PropertyFactory INSTANCE=null;

    public static synchronized PropertyFactory getInstance(){
        if(INSTANCE==null){
            INSTANCE = new PropertyFactory();
        }
        return INSTANCE;
    }
    private static final ConcurrentHashMap<String, PropertyFactory> ALL_PROPS
            = new ConcurrentHashMap<String, PropertyFactory>();



    private DynamicPropertyFactory _dynamicPropertyFactory;


    public PropertyFactory(){
        _dynamicPropertyFactory = DynamicPropertyFactory.getInstance();
    }

    static String buildPropName(String name,boolean withPrefix){return withPrefix?ConfigManagerFactory.buildFullName(name):name;}

    public static BooleanProperty getBooleanProperty(String name,boolean defaultValue,PropertyChangedCallback<Boolean> callback) {  return new BooleanProperty(name, defaultValue,callback);}
    public static BooleanProperty getBooleanProperty(String name,boolean defaultValue) {  return new BooleanProperty(name, defaultValue);}
    public static BooleanProperty getBooleanProperty(String name,boolean defaultValue,boolean withPrefix) {  return new BooleanProperty(buildPropName(name,withPrefix), defaultValue);}
    public static BooleanProperty getBooleanProperty(String name,boolean defaultValue,PropertyChangedCallback<Boolean> callback,boolean withPrefix) {  return new BooleanProperty(buildPropName(name,withPrefix), defaultValue,callback);}


    public static IntProperty getIntProperty(String name,int defaultValue,PropertyChangedCallback<Integer> callback) {  return new IntProperty(name, defaultValue,callback);}
    public static IntProperty getIntProperty(String name,int defaultValue) {  return new IntProperty(name, defaultValue);}
    public static IntProperty getIntProperty(String name,int defaultValue,boolean withPrefix) {  return new IntProperty(buildPropName(name,withPrefix), defaultValue);}
    public static IntProperty getIntProperty(String name,int defaultValue,PropertyChangedCallback<Integer> callback,boolean withPrefix) {  return new IntProperty(buildPropName(name,withPrefix), defaultValue,callback);}

    public static LongProperty getLongProperty(String name,long defaultValue,PropertyChangedCallback<Long> callback) {  return new LongProperty(name, defaultValue,callback);}
    public static LongProperty getLongProperty(String name,long defaultValue) {  return new LongProperty(name, defaultValue);}
    public static LongProperty getLongProperty(String name,long defaultValue,boolean withPrefix) {  return new LongProperty(buildPropName(name,withPrefix), defaultValue);}
    public static LongProperty getLongProperty(String name,long defaultValue,PropertyChangedCallback<Long> callback,boolean withPrefix) {  return new LongProperty(buildPropName(name,withPrefix), defaultValue,callback);}


    public static FloatProperty getFloatProperty(String name,float defaultValue,PropertyChangedCallback<Float> callback) {  return new FloatProperty(name, defaultValue,callback);}
    public static FloatProperty getFloatProperty(String name,float defaultValue) {  return new FloatProperty(name, defaultValue);}
    public static FloatProperty getFloatProperty(String name,float defaultValue,boolean withPrefix) {  return new FloatProperty(buildPropName(name,withPrefix), defaultValue);}
    public static FloatProperty getFloatProperty(String name,float defaultValue,PropertyChangedCallback<Float> callback,boolean withPrefix) {  return new FloatProperty(buildPropName(name,withPrefix), defaultValue,callback);}


    public static DoubleProperty getDoubleProperty(String name,double defaultValue,PropertyChangedCallback<Double> callback) {  return new DoubleProperty(name, defaultValue,callback);}
    public static DoubleProperty getDoubleProperty(String name,double defaultValue) {  return new DoubleProperty(name, defaultValue);}
    public static DoubleProperty getDoubleProperty(String name,double defaultValue,boolean withPrefix) {  return new DoubleProperty(buildPropName(name,withPrefix), defaultValue);}
    public static DoubleProperty getDoubleProperty(String name,double defaultValue,PropertyChangedCallback<Double> callback,boolean withPrefix) {  return new DoubleProperty(buildPropName(name,withPrefix), defaultValue,callback);}

    public static StringProperty getStringProperty(String name,String defaultValue,PropertyChangedCallback<String> callback) {  return new StringProperty(name, defaultValue,callback);}
    public static StringProperty getStringProperty(String name,String defaultValue) {  return new StringProperty(name, defaultValue);}
    public static StringProperty getStringProperty(String name,String defaultValue,boolean withPrefix) {  return new StringProperty(buildPropName(name,withPrefix), defaultValue);}
    public static StringProperty getStringProperty(String name,String defaultValue,PropertyChangedCallback<String> callback,boolean withPrefix) {  return new StringProperty(buildPropName(name,withPrefix), defaultValue,callback);}

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
