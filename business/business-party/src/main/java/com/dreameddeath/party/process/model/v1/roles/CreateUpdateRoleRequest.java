package com.dreameddeath.party.process.model.v1.roles;

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.validation.annotation.NotNull;

/**
 * Created by Christophe Jeunesse on 09/05/2016.
 */
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
}
