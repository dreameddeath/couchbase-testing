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

import com.dreameddeath.core.config.AbstractConfigProperty;
import com.dreameddeath.core.config.ConfigPropertyChangedCallback;
import com.dreameddeath.core.config.IConfigProperty;
import com.dreameddeath.core.config.internal.ExtendedPropertyWrapper;
import com.dreameddeath.core.config.internal.ReferencePropertyWrapper;

/**
 * Created by Christophe Jeunesse on 04/02/2015.
 */
public class StringConfigProperty extends AbstractConfigProperty<String> {

    public StringConfigProperty(String name, String defaultValue) {
        super(new ExtendedProperty(name,defaultValue));
    }

    public StringConfigProperty(String name, String defaultValue, ConfigPropertyChangedCallback<String> callback) {
        super(new ExtendedProperty(name,defaultValue),callback);
    }

    public StringConfigProperty(String name, IConfigProperty<String> defaultValue) {
        super(new ExtendedRefProperty(name,defaultValue));
    }

    public StringConfigProperty(String name, IConfigProperty<String> defaultValue, ConfigPropertyChangedCallback<String> callback) {
        super(new ExtendedRefProperty(name,defaultValue),callback);
    }

    public String get(){return getValue();}

    protected static class ExtendedProperty extends ExtendedPropertyWrapper<String> {
        public ExtendedProperty(String name, String defaultValue){
            super(name,defaultValue);
        }
        @Override
        public String getValue() {
            return prop.getString(defaultValue);
        }
    }

    protected static class ExtendedRefProperty extends ReferencePropertyWrapper<String> {
        public ExtendedRefProperty(String name,IConfigProperty<String> defaultValue){
            super(name,defaultValue);
        }
        @Override
        public String getLocalValue() {
            return prop.getString();
        }
    }
}
