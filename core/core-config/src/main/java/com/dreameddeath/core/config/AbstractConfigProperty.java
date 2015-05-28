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

import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFound;
import com.dreameddeath.core.config.internal.ExtendedPropertyWrapper;
import com.dreameddeath.core.config.internal.ReferencePropertyWrapper;
import com.netflix.config.PropertyWrapper;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Christophe Jeunesse on 03/02/2015.
 */
public abstract class AbstractConfigProperty<T> implements IConfigProperty<T> {
    private final PropertyWrapper<T> _wrapper;
    private final List<ConfigPropertyChangedCallback<T>> _callbacks = new CopyOnWriteArrayList<>();

    public static <T> PropertyWrapper<T> buildDefaultWrapper(String name,  T defaultValue) {
        return new ExtendedPropertyWrapper<T>(name, defaultValue) {
            private Class<T> _class=null;
            @Override
            @SuppressWarnings("unchecked")
            public T getValue() {
                if(_class==null){
                    _class=(Class<T>) defaultValue.getClass();
                }
                T res = prop.getCachedValue(_class).get();
                return res!=null?res:defaultValue;
            }
        };
    }


    public static <T> PropertyWrapper<T> buildDefaultWrapperWithRef(String name, final IConfigProperty<T> defaultValueRef) {
        return new ReferencePropertyWrapper<T>(name, defaultValueRef) {
            private Class<T> _class=null;
            @Override
            @SuppressWarnings("unchecked")
            public T getLocalValue() {
                return prop.getCachedValue(_class).get();
            }
        };
    }


    public AbstractConfigProperty(String name, T defaultValue) {
        this(buildDefaultWrapper(name, defaultValue));
    }

    public AbstractConfigProperty(String name, IConfigProperty<T> defaultValue) {
        this(buildDefaultWrapperWithRef(name, defaultValue));
    }

    public AbstractConfigProperty(String name, T defaultValue, ConfigPropertyChangedCallback<T> callback) {
        this(buildDefaultWrapper(name, defaultValue), callback);
    }

    public AbstractConfigProperty(String name, IConfigProperty<T> defaultValue, ConfigPropertyChangedCallback<T> callback) {
        this(buildDefaultWrapperWithRef(name, defaultValue), callback);
    }



    public AbstractConfigProperty(PropertyWrapper<T> wrapper) {
        _wrapper = wrapper;
        ConfigPropertyFactory.addPropertyToGlobalMap(this);
    }

    public AbstractConfigProperty(PropertyWrapper<T> wrapper, ConfigPropertyChangedCallback<T> callback) {
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
        if (value == null) {
            throw new ConfigPropertyValueNotFound(this, errorMessage);
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
        _callbacks.add(callback);
    }

    @Override
    public void removeAllCallbacks() {
        _wrapper.removeAllCallbacks();
    }

    @Override
    public Collection<ConfigPropertyChangedCallback<T>> getCallbacks(){
        return Collections.unmodifiableCollection(_callbacks);
    }

}