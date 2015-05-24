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

package com.dreameddeath.core.config.impl;

import com.dreameddeath.core.config.AbstractConfigProperty;
import com.dreameddeath.core.config.ConfigPropertyChangedCallback;
import com.dreameddeath.core.config.IConfigProperty;
import com.dreameddeath.core.config.internal.ExtendedPropertyWrapper;
import com.dreameddeath.core.config.internal.ReferencePropertyWrapper;

/**
 * Created by CEAJ8230 on 04/02/2015.
 */
public class LongConfigProperty extends AbstractConfigProperty<Long> {

    public LongConfigProperty(String name, Long defaultValue) {
        super(new ExtendedProperty(name,defaultValue));
    }
    public LongConfigProperty(String name, Long defaultValue, ConfigPropertyChangedCallback<Long> callback) {
        super(new ExtendedProperty(name,defaultValue),callback);
    }

    public LongConfigProperty(String name, IConfigProperty<Long> defaultValue) {
        super(new ExtendedRefProperty(name,defaultValue));
    }

    public LongConfigProperty(String name, IConfigProperty<Long> defaultValue, ConfigPropertyChangedCallback<Long> callback) {
        super(new ExtendedRefProperty(name,defaultValue),callback);
    }

    public long get(){return getValue();}

    protected static class ExtendedProperty extends ExtendedPropertyWrapper<Long> {
        public ExtendedProperty(String name, Long defaultValue){
            super(name,defaultValue);
        }
        @Override
        public Long getValue() {
            return prop.getLong(defaultValue);
        }
    }

    protected static class ExtendedRefProperty extends ReferencePropertyWrapper<Long> {
        public ExtendedRefProperty(String name,IConfigProperty<Long> defaultValue){
            super(name,defaultValue);
        }
        @Override
        public Long getLocalValue() {
            return prop.getLong();
        }
    }
}
