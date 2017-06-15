/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.party.process.model.v1.roles.tasks;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.couchbase.core.process.remote.annotation.RestExpose;

/**
 * Created by Christophe Jeunesse on 10/05/2016.
 */
@RestExpose(rootPath = "",pureSubClassMode = DtoInOutMode.OUT,defaultInputFieldMode = FieldGenMode.SIMPLE)
public class PartyUpdateResult extends CouchbaseDocumentElement{
    /**
     *  uid : party uid
     */
    @DocumentProperty("uid")
    private Property<String> uid = new StandardProperty<>(PartyUpdateResult.this);

    /**
     * Getter of uid
     * @return the value of uid
     */
    public String getUid() { return uid.get(); }
    /**
     * Setter of uid
     * @param val the new value of uid
     */
    public void setUid(String val) { uid.set(val); }
}
