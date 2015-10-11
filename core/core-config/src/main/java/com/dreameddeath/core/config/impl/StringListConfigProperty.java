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

package com.dreameddeath.core.config.impl;

import com.dreameddeath.core.config.AbstractConfigListProperty;
import com.dreameddeath.core.config.ConfigPropertyChangedCallback;
import com.google.common.base.Splitter;

import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 23/05/2015.
 */
public class StringListConfigProperty extends AbstractConfigListProperty<String> {
    public StringListConfigProperty(String name, String delimiterRegexp, String defaultValue) {
        super(name, delimiterRegexp, defaultValue);
    }

    public StringListConfigProperty(String name, String delimiterRegexp, AbstractConfigListProperty<String> defaultValueRef) {
        super(name, delimiterRegexp, defaultValueRef);
    }

    public StringListConfigProperty(String name, String delimiterRegexp, String defaultValue,ConfigPropertyChangedCallback<List<String>> callback) {
        super(name, delimiterRegexp, defaultValue,callback);
    }

    public StringListConfigProperty(String name, String delimiterRegexp, AbstractConfigListProperty<String> defaultValueRef,ConfigPropertyChangedCallback<List<String>> callback) {
        super(name, delimiterRegexp, defaultValueRef,callback);
    }



    public StringListConfigProperty(String name, String defaultValue) {
        super(name, defaultValue);
    }

    public StringListConfigProperty(String name, String defaultValue,ConfigPropertyChangedCallback<List<String>> callback) {
        super(name, defaultValue,callback);
    }




    public StringListConfigProperty(String name, AbstractConfigListProperty<String> defaultValueRef) {
        super(name, defaultValueRef);
    }

    public StringListConfigProperty(String name, AbstractConfigListProperty<String> defaultValueRef,ConfigPropertyChangedCallback<List<String>> callback) {
        super(name, defaultValueRef,callback);
    }

    @Override
    protected List<String> splitValue(Splitter splitter, String value) {
        if(value!=null) {
            return splitter.splitToList(value);
        }
        else{
            return Collections.emptyList();
        }
    }
}
