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
import com.dreameddeath.core.config.impl.StringConfigProperty;
import com.google.common.base.Splitter;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Christophe Jeunesse on 21/05/2015.
 */
public abstract class AbstractConfigListProperty<T> implements IConfigProperty<List<T>>{
    private static String DEFAULT_DELIMITER_REGEXP = ",";
    private final List<ConfigPropertyChangedCallback<List<T>>> _callbacks = new CopyOnWriteArrayList<>();

    private final StringConfigProperty _stringProp;
    private String _cachedValue=null;
    private List<T> _cachedList;
    private String _cachedDefaultValue=null;
    private List<T> _defaultValue=null;
    private final Splitter _splitter;

    protected abstract List<T> splitValue(Splitter splitter,String value);

    private static Splitter splitterBuilder(String delimiterRegexp){
        return Splitter.onPattern(delimiterRegexp).omitEmptyStrings().trimResults();
    }



    private AbstractConfigListProperty(String name,Splitter splitter,String defaultValue,ConfigPropertyChangedCallback<List<T>> callback) {
        _splitter = splitter;
        _stringProp= new StringConfigProperty(name,defaultValue);
        final AbstractConfigListProperty<T> curr = this;
        _stringProp.addCallback(new ConfigPropertyChangedCallback<String>(){
            @Override
            public void onChange(IConfigProperty<String> prop, String oldValue, String newValue) {
                List<T> oldValueList=null;
                if(oldValue!=null){
                    oldValueList = curr.splitValue(curr._splitter,oldValue);
                }
                List<T> newValueList=null;
                if(newValue!=null){
                    newValueList = curr.splitValue(curr._splitter,newValue);

                }
                for(ConfigPropertyChangedCallback<List<T>> callback:curr.getCallbacks()){
                    callback.onChange(curr,oldValueList,newValueList);
                }
            }
        });
        if(defaultValue!=null) {
            ConfigManagerFactory.addDefaultConfigurationEntry(name, defaultValue);
        }
        if(callback!=null) {
            addCallback(callback);
        }

    }

    private AbstractConfigListProperty(String name,Splitter splitter,AbstractConfigListProperty<T> defaultValueRef,ConfigPropertyChangedCallback<List<T>> callback) {
        _splitter = splitter;
        _stringProp=new StringConfigProperty(name,defaultValueRef._stringProp);
        if(callback!=null){
            addCallback(callback);
        }
    }


    private AbstractConfigListProperty(String name,Splitter splitter,String defaultValue) {
        this(name, splitter, defaultValue, null);
    }


    private AbstractConfigListProperty(String name,Splitter splitter,AbstractConfigListProperty<T> defaultValueRef) {
        this(name, splitter, defaultValueRef, null);
    }


    public AbstractConfigListProperty(String name,String delimiterRegexp,String defaultValue) {
        this(name, splitterBuilder(delimiterRegexp), defaultValue);
    }


    public AbstractConfigListProperty(String name,String delimiterRegexp,AbstractConfigListProperty<T> defaultValueRef) {
        this(name, splitterBuilder(delimiterRegexp), defaultValueRef);
    }

    public AbstractConfigListProperty(String name,String delimiterRegexp,String defaultValue,ConfigPropertyChangedCallback<List<T>> callback) {
        this(name, splitterBuilder(delimiterRegexp), defaultValue,callback);
    }

    public AbstractConfigListProperty(String name,String delimiterRegexp,AbstractConfigListProperty<T> defaultValueRef,ConfigPropertyChangedCallback<List<T>> callback) {
        this(name, splitterBuilder(delimiterRegexp), defaultValueRef,callback);
    }

    public AbstractConfigListProperty(String name,String defaultValue) {
        this(name, splitterBuilder(DEFAULT_DELIMITER_REGEXP), defaultValue);
    }

    public AbstractConfigListProperty(String name,String defaultValue,ConfigPropertyChangedCallback<List<T>> callback) {
        this(name, splitterBuilder(DEFAULT_DELIMITER_REGEXP), defaultValue,callback);
    }

    public AbstractConfigListProperty(String name,AbstractConfigListProperty<T> defaultValueRef) {
        this(name,splitterBuilder(DEFAULT_DELIMITER_REGEXP),defaultValueRef);
    }

    public AbstractConfigListProperty(String name,AbstractConfigListProperty<T> defaultValueRef,ConfigPropertyChangedCallback<List<T>> callback) {
        this(name,splitterBuilder(DEFAULT_DELIMITER_REGEXP),defaultValueRef,callback);
    }

    @Override
    public List<T> getValue() {
        String res = _stringProp.getValue();
        if(res==null){
            return getDefaultValue();
        }
        else if(!res.equals(_cachedValue)){
            _cachedValue = res;
            _cachedList = splitValue(_splitter,res);
        }
        return _cachedList;
    }

    @Override
    public List<T> getMandatoryValue(String errorMessage) throws ConfigPropertyValueNotFoundException {
        List<T> res = getValue();
        if((res==null)||(res.size()==0)){
            throw new ConfigPropertyValueNotFoundException(this,errorMessage);
        }
        return res;
    }

    @Override
    public List<T> getDefaultValue() {
        String defaultValue = _stringProp.getDefaultValue();
        if(_cachedDefaultValue.equals(defaultValue)){
            _cachedDefaultValue = defaultValue;
            _defaultValue = splitValue(_splitter,_cachedDefaultValue);
        }
        return _defaultValue;
    }

    @Override
    public String getName() {
        return _stringProp.getName();
    }

    @Override
    public DateTime getLastChangedDate() {
        return _stringProp.getLastChangedDate();
    }

    @Override
    public void addCallback(final ConfigPropertyChangedCallback<List<T>> callback) {
        _callbacks.add(callback);
    }

    @Override
    public void removeAllCallbacks() {
        _stringProp.removeAllCallbacks();
    }

    @Override
    public Collection<ConfigPropertyChangedCallback<List<T>>> getCallbacks(){
        return Collections.unmodifiableCollection(_callbacks);
    }

    public List<T> get(){
        return getValue();
    }
}
