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
 * Created by Christophe Jeunesse on 03/02/2015.
 */
public class BooleanConfigProperty extends AbstractConfigProperty<Boolean> {

    public BooleanConfigProperty(String name, Boolean defaultValue) {
        super(new ExtendedProperty(name,defaultValue));
    }

    public BooleanConfigProperty(String name, Boolean defaultValue, ConfigPropertyChangedCallback<Boolean> callback) {
        super(new ExtendedProperty(name,defaultValue),callback);
    }

    public BooleanConfigProperty(String name, IConfigProperty<Boolean> defaultValue) {
        super(new ExtendedRefProperty(name,defaultValue));
    }

    public BooleanConfigProperty(String name, IConfigProperty<Boolean> defaultValue, ConfigPropertyChangedCallback<Boolean> callback) {
        super(new ExtendedRefProperty(name,defaultValue),callback);
    }


    public boolean get(){return getValue();}

    protected static class ExtendedProperty extends ExtendedPropertyWrapper<Boolean> {
        public ExtendedProperty(String name, Boolean defaultValue){
            super(name,defaultValue);
        }
        @Override
        public Boolean getValue() {
            return prop.getBoolean(defaultValue);
        }
    }

    protected static class ExtendedRefProperty extends ReferencePropertyWrapper<Boolean> {
        public ExtendedRefProperty(String name, IConfigProperty<Boolean> defaultValue){
            super(name,defaultValue);
        }
        @Override
        public Boolean getLocalValue() {
            return prop.getBoolean();
        }
    }
}
