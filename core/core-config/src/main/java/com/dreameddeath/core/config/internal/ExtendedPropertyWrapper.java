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

package com.dreameddeath.core.config.internal;

import com.netflix.config.PropertyWrapper;

/**
 * Created by Christophe Jeunesse on 22/05/2015.
 */
public abstract class ExtendedPropertyWrapper<T> extends PropertyWrapper<T> {
    private T oldValue = null;

    public ExtendedPropertyWrapper(String name, T defaultValue) {
        super(name, defaultValue);
    }

    @Override
    protected void propertyChanged(T newValue) {
        oldValue = getValue();
    }

    public T getOldValue() {
        return oldValue;
    }
}
