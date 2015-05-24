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
public class FloatConfigProperty extends AbstractConfigProperty<Float> {

    public FloatConfigProperty(String name, Float defaultValue) {
        super(new ExtendedProperty(name,defaultValue));
    }

    public FloatConfigProperty(String name, Float defaultValue, ConfigPropertyChangedCallback<Float> callback) {
        super(new ExtendedProperty(name,defaultValue),callback);
    }

    public FloatConfigProperty(String name, IConfigProperty<Float> defaultValue) {
        super(new ExtendedRefProperty(name,defaultValue));
    }

    public FloatConfigProperty(String name, IConfigProperty<Float> defaultValue, ConfigPropertyChangedCallback<Float> callback) {
        super(new ExtendedRefProperty(name,defaultValue),callback);
    }


    public float get(){return getValue();}

    protected static class ExtendedProperty extends ExtendedPropertyWrapper<Float> {
        public ExtendedProperty(String name, Float defaultValue) {
            super(name,defaultValue);
        }
        @Override
        public Float getValue() {
            return prop.getFloat(defaultValue);
        }
    }

    protected static class ExtendedRefProperty extends ReferencePropertyWrapper<Float> {
        public ExtendedRefProperty(String name,IConfigProperty<Float> defaultValue){
            super(name,defaultValue);
        }
        @Override
        public Float getLocalValue() {
            return prop.getFloat();
        }
    }
}
