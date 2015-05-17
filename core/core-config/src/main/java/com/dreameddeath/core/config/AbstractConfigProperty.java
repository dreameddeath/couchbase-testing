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

import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFound;
import com.netflix.config.PropertyWrapper;
import org.joda.time.DateTime;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by CEAJ8230 on 03/02/2015.
 */
public abstract class AbstractConfigProperty<T> implements IConfigProperty<T> {
    private static ConcurrentMap<String,List<AbstractConfigProperty>> _mapCallbackPerProperty=new ConcurrentHashMap<>();

    private static void addPropertyToGlobalMap(AbstractConfigProperty prop){
        if(!_mapCallbackPerProperty.containsKey(prop.getName())){
            _mapCallbackPerProperty.putIfAbsent(prop.getName(),new CopyOnWriteArrayList<>());
        }

        _mapCallbackPerProperty.get(prop.getName()).add(prop);
    }

    private static void removePropertyFromGlobalMap(AbstractConfigProperty prop){
        if(!_mapCallbackPerProperty.containsKey(prop.getName())){
            _mapCallbackPerProperty.putIfAbsent(prop.getName(),new CopyOnWriteArrayList<>());
        }
        _mapCallbackPerProperty.get(prop.getName()).remove(prop);
    }

    public static void fireCallback(String name,Object value){
        if(_mapCallbackPerProperty.containsKey(name)){
            for(AbstractConfigProperty<Object> prop:_mapCallbackPerProperty.get(name)){
                for(ConfigPropertyChangedCallback<Object> callback:prop._callbacks){
                    callback.onChange(prop,prop.getValue(),value);
                }
            }
        }
    }

    private final ExtendedPropertyWrapper<T> _wrapper;
    private final List<ConfigPropertyChangedCallback<T>> _callbacks=new CopyOnWriteArrayList<>();

    public static <T> ExtendedPropertyWrapper<T> buildDefaultWrapper(String name,T defaultValue){
        return new ExtendedPropertyWrapper<T>(name,defaultValue) {
            @Override @SuppressWarnings("unchecked")
            public T getValue() {
                return prop.getCachedValue( (Class<T>)defaultValue.getClass()).get();
            }
        };
    }

    public AbstractConfigProperty(String name, T defaultValue){
        this(buildDefaultWrapper(name,defaultValue));
    }

    public AbstractConfigProperty(String name, T defaultValue, ConfigPropertyChangedCallback<T> callback){
        this(buildDefaultWrapper(name,defaultValue),callback);
    }


    public AbstractConfigProperty(ExtendedPropertyWrapper<T> wrapper){
        _wrapper = wrapper;
        addPropertyToGlobalMap(this);
    }

    public AbstractConfigProperty(ExtendedPropertyWrapper<T> wrapper, ConfigPropertyChangedCallback<T> callback){
        this(wrapper);
        addCallback(callback);
    }

    @Override
    public T getValue() {
        return _wrapper.getValue();
    }

    @Override
    public T getMandatoryValue(String errorMessage) throws ConfigPropertyValueNotFound {
        T value = getValue();
        if(value==null){
            throw new ConfigPropertyValueNotFound(this,errorMessage);
        }
        return value;
    }

    @Override
    public T getDefaultValue() {
        return _wrapper.getDefaultValue();
    }

    @Override
    public String getName() {
        return _wrapper.getName();
    }

    @Override
    public DateTime getLastChangedDate() {
        return new DateTime(_wrapper.getChangedTimestamp());
    }

    @Override
    public void addCallback(final ConfigPropertyChangedCallback<T> callback) {
        //final AbstractProperty<T> current = this;
        _callbacks.add(callback);
        //addPropertyCallback(this.getName(),callback);
        //_wrapper.addCallback(() -> callback.onChange(current,_wrapper.getOldValue(),current.getValue()));
    }

    @Override
    public void removeAllCallbacks() {
        _wrapper.removeAllCallbacks();
    }

    public static abstract class ExtendedPropertyWrapper<T> extends PropertyWrapper<T>{
        private T _oldValue=null;

        public ExtendedPropertyWrapper(String name,T defaultValue){
            super(name,defaultValue);
        }

        protected void propertyChanged(T newValue) {
            _oldValue = getValue();
        }


        public T getOldValue() {
            return _oldValue;
        }
    }
}
