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
public class IntConfigProperty extends AbstractConfigProperty<Integer> {

    public IntConfigProperty(String name, Integer defaultValue) {
        super(new ExtendedProperty(name,defaultValue));
    }

    public IntConfigProperty(String name, Integer defaultValue, ConfigPropertyChangedCallback<Integer> callback) {
        super(new ExtendedProperty(name,defaultValue),callback);
    }

    public IntConfigProperty(String name, IConfigProperty<Integer> defaultValue) {
        super(new ExtendedRefProperty(name,defaultValue));
    }

    public IntConfigProperty(String name, IConfigProperty<Integer> defaultValue, ConfigPropertyChangedCallback<Integer> callback) {
        super(new ExtendedRefProperty(name,defaultValue),callback);
    }


    public int get(){return getValue();}

    protected static class ExtendedProperty extends ExtendedPropertyWrapper<Integer> {
        public ExtendedProperty(String name, Integer defaultValue){
            super(name,defaultValue);
        }
        @Override
        public Integer getValue() {
            return prop.getInteger(defaultValue);
        }
    }

    protected static class ExtendedRefProperty extends ReferencePropertyWrapper<Integer> {
        public ExtendedRefProperty(String name,IConfigProperty<Integer> defaultValue){
            super(name,defaultValue);
        }
        @Override
        public Integer getLocalValue() {
            return prop.getInteger();
        }
    }
}
