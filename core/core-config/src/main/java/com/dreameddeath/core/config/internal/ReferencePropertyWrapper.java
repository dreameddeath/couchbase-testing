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

import com.dreameddeath.core.config.ConfigPropertyChangedCallback;
import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.IConfigProperty;

/**
 * Created by Christophe Jeunesse on 22/05/2015.
 */
public abstract class ReferencePropertyWrapper<T> extends ExtendedPropertyWrapper<T> {
    private IConfigProperty<T> defaultRefProperty;

    protected ReferencePropertyWrapper(String name, IConfigProperty<T> defaultRefProperty) {
        super(name, defaultRefProperty.getDefaultValue());
        this.defaultRefProperty = defaultRefProperty;
        defaultRefProperty.addCallback(new RefChangeCallBack<>(this));
    }

    public abstract T getLocalValue();

    public final T getValue(T overrideDefaultValue){
        T res = getLocalValue();
        return res !=null ? res : getDefaultValue();
    }

    @Override
    public T getDefaultValue() {
        return defaultRefProperty.getValue();
    }

    protected static class RefChangeCallBack<T> implements ConfigPropertyChangedCallback<T> {
        private ReferencePropertyWrapper<T> refProperty;

        protected RefChangeCallBack(ReferencePropertyWrapper<T> ref) {
            refProperty = ref;
        }

        @Override
        public void onChange(IConfigProperty<T> prop, T oldValue, T newValue) {
            if (refProperty.getLocalValue() == null) {
                refProperty.propertyChanged(newValue);
                ConfigPropertyFactory.fireCallback(refProperty.getName(), newValue);
            }
        }
    }
}
