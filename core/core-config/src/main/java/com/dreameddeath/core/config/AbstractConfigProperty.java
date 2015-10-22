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

import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFoundException;
import com.dreameddeath.core.config.internal.ExtendedPropertyWrapper;
import com.dreameddeath.core.config.internal.ReferencePropertyWrapper;
import com.netflix.config.PropertyWrapper;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 03/02/2015.
 */
public abstract class AbstractConfigProperty<T> implements IConfigProperty<T> {
    private final PropertyWrapper<T> wrapper;
    private final List<ConfigPropertyChangedCallback<T>> callbacks = new CopyOnWriteArrayList<>();

    public static <T> PropertyWrapper<T> buildDefaultWrapper(String name,  T defaultValue) {
        return new ExtendedPropertyWrapper<T>(name, defaultValue) {
            private Class<T> clazz=null;
            @Override
            @SuppressWarnings("unchecked")
            public T getValue() {
                if(clazz==null){
                    clazz=(Class<T>) defaultValue.getClass();
                }
                T res = prop.getCachedValue(clazz).get();
                return res!=null?res:defaultValue;
            }
        };
    }


    public static <T> PropertyWrapper<T> buildDefaultWrapperWithRef(String name, final IConfigProperty<T> defaultValueRef) {
        return new ReferencePropertyWrapper<T>(name, defaultValueRef) {
            private Class<T> clazz=null;
            @Override
            @SuppressWarnings("unchecked")
            public T getLocalValue() {
                return prop.getCachedValue(clazz).get();
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
        this.wrapper = wrapper;
        ConfigPropertyFactory.addPropertyToGlobalMap(this);
        if(!(wrapper instanceof ReferencePropertyWrapper)&& (wrapper.getDefaultValue()!=null)) {
            ConfigManagerFactory.addDefaultConfigurationEntry(wrapper.getName(), wrapper.getDefaultValue());
        }
    }

    public AbstractConfigProperty(PropertyWrapper<T> wrapper, ConfigPropertyChangedCallback<T> callback) {
        this(wrapper);
        addCallback(callback);
    }

    @Override
    public T getValue() {
        return wrapper.getValue();
    }

    @Override
    public T getMandatoryValue(String errorMessage,Object ... params) throws ConfigPropertyValueNotFoundException {
        T value = getValue();
        if (value == null) {
            Pattern pattern = Pattern.compile("(\\{\\})");
            Matcher matcher =pattern.matcher(errorMessage);
            StringBuffer resultMessage = new StringBuffer();
            int pos=0;
            //System.err.println("Nb params :"+params.length);
            while(matcher.find()) {
                if(pos>params.length){
                    matcher.appendReplacement(resultMessage, "<undef>");
                }
                else{
                    Object paramValue = params[pos++];
                    matcher.appendReplacement(resultMessage, paramValue!=null?paramValue.toString():"<null>");
                }
            }
            matcher.appendTail(resultMessage);
            throw new ConfigPropertyValueNotFoundException(this, resultMessage.toString());
        }
        return value;
    }

    @Override
    public T getDefaultValue() {
        return wrapper.getDefaultValue();
    }

    @Override
    public String getName() {
        return wrapper.getName();
    }

    @Override
    public DateTime getLastChangedDate() {
        return new DateTime(wrapper.getChangedTimestamp());
    }

    @Override
    public void addCallback(final ConfigPropertyChangedCallback<T> callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeAllCallbacks() {
        wrapper.removeAllCallbacks();
    }

    @Override
    public Collection<ConfigPropertyChangedCallback<T>> getCallbacks(){
        return Collections.unmodifiableCollection(callbacks);
    }

}