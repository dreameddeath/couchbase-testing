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

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.dreameddeath.couchbase.core.process.remote.annotation.RestExpose;

/**
 * Created by Christophe Jeunesse on 09/05/2016.
 */
@RestExpose(rootPath = "",pureSubClassMode = DtoInOutMode.IN,defaultInputFieldMode = FieldGenMode.SIMPLE,superClassGenMode = SuperClassGenMode.AUTO,forceGenerateMode = true,isClassHierarchyRoot = true)
@DocumentEntity
public abstract class CreateUpdateRoleRequest extends VersionedDocumentElement{
    /**
     *  partyId : the id of the party to update
     */
    @DocumentProperty("partyId") @NotNull
    private Property<String> partyId = new ImmutableProperty<>(CreateUpdateRoleRequest.this);
    /**
     *  roleUid : The uid of the role to update if any
     */
    @DocumentProperty("roleUid")
    private Property<String> roleUid = new ImmutableProperty<>(CreateUpdateRoleRequest.this);
    /**
     *  tempUid : temporary role uid
     */
    @DocumentProperty("tempUid")
    private Property<String> tempUid = new ImmutableProperty<>(CreateUpdateRoleRequest.this);



    /**
     * Getter of partyId
     * @return the value of partyId
     */
    public String getPartyId() { return partyId.get(); }
    /**
     * Setter of partyId
     * @param val the new value for partyId
     */
    public void setPartyId(String val) { partyId.set(val); }
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
}
