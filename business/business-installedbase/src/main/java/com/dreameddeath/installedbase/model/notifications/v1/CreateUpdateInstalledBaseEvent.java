/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.installedbase.model.notifications.v1;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.process.model.v1.notification.AbstractTaskEvent;

/**
 * Created by Christophe Jeunesse on 10/11/2016.
 */
@DocumentEntity
public class CreateUpdateInstalledBaseEvent extends AbstractTaskEvent {
    /**
     *  installedBaseKey : The installed base key being updated
     */
    @DocumentProperty("installedBaseKey")
    private Property<String> installedBaseKey = new ImmutableProperty<>(CreateUpdateInstalledBaseEvent.this);

    /**
     * Getter of installedBaseKey
     * @return the value of installedBaseKey
     */
    public String getInstalledBaseKey() { return installedBaseKey.get(); }
    /**
     * Setter of installedBaseKey
     * @param val the new value for installedBaseKey
     */
    public void setInstalledBaseKey(String val) { installedBaseKey.set(val); }
}
