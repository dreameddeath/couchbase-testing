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

package com.dreameddeath.party.process.model.v1.roles;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.couchbase.core.process.remote.annotation.RestExpose;

/**
 * Created by Christophe Jeunesse on 19/05/2016.
 */
@RestExpose(rootPath = "",pureSubClassMode = DtoInOutMode.OUT,defaultOutputFieldMode= FieldGenMode.SIMPLE)
public class CreateUpdateRoleResult extends CouchbaseDocumentElement {
    /**
     *  tempUid : The temporary id of the created role if any
     */
    @DocumentProperty("tempUid")
    private Property<String> tempUid = new ImmutableProperty<>(CreateUpdateRoleResult.this);
    /**
     *  roleUid : the updated role uid
     */
    @DocumentProperty("roleUid")
    private Property<String> roleUid = new ImmutableProperty<>(CreateUpdateRoleResult.this);

    /**
     * Getter of tempUid
     * @return the value of tempUid
     */
    public String getTempUid() { return tempUid.get(); }
    /**
     * Setter of tempUid
     * @param val the new value for tempUid
     */
    public void setTempUid(String val) { tempUid.set(val); }

    /**
     * Getter of roleUid
     * @return the value of roleUid
     */
    public String getRoleUid() { return roleUid.get(); }
    /**
     * Setter of roleUid
     * @param val the new value for roleUid
     */
    public void setRoleUid(String val) { roleUid.set(val); }
}
