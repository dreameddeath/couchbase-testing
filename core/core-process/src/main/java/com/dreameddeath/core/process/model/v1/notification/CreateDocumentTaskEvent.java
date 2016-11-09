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

package com.dreameddeath.core.process.model.v1.notification;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;

/**
 * Created by Christophe Jeunesse on 09/11/2016.
 */
@DocumentEntity
public class CreateDocumentTaskEvent extends AbstractTaskEvent {
    /**
     *  docKey : The document key
     */
    @DocumentProperty("docKey")
    private Property<String> docKey = new ImmutableProperty<>(CreateDocumentTaskEvent.this);

    /**
     * Getter of docKey
     * @return the value of docKey
     */
    public String getDocKey() { return docKey.get(); }
    /**
     * Setter of docKey
     * @param val the new value for docKey
     */
    public void setDocKey(String val) { docKey.set(val); }

}
