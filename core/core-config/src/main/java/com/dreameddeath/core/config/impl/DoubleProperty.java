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

import com.dreameddeath.core.config.AbstractProperty;
import com.dreameddeath.core.config.PropertyChangedCallback;

/**
 * Created by CEAJ8230 on 04/02/2015.
 */
public class DoubleProperty extends AbstractProperty<Double> {

    public DoubleProperty(String name, Double defaultValue) {
        super(new DoubleExtendedProperty(name,defaultValue));
    }

    public DoubleProperty(String name, Double defaultValue,PropertyChangedCallback<Double>callback) {
        super(new DoubleExtendedProperty(name,defaultValue),callback);
    }

    public double get(){return getValue();}

    protected static class DoubleExtendedProperty extends ExtendedPropertyWrapper<Double>{
        public DoubleExtendedProperty(String name,Double defaultValue){
            super(name,defaultValue);
        }
        @Override
        public Double getValue() {
            return prop.getDouble(defaultValue);
        }
    }
}
