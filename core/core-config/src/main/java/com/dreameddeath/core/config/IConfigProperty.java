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

import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFound;
import org.joda.time.DateTime;

import java.util.Collection;

/**
 * Created by Christophe Jeunesse on 03/02/2015.
 */
public interface IConfigProperty<T> {
    T getValue();
    T getMandatoryValue(String errorMessage) throws ConfigPropertyValueNotFound;
    T getDefaultValue();
    String getName();
    DateTime getLastChangedDate();
    void addCallback(ConfigPropertyChangedCallback<T> callback);
    void removeAllCallbacks();
    Collection<ConfigPropertyChangedCallback<T>> getCallbacks();
}
