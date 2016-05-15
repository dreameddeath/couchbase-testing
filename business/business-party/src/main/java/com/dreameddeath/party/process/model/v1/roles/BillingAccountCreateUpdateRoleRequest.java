package com.dreameddeath.party.process.model.v1.roles;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.dreameddeath.party.model.v1.roles.BillingAccountPartyRole;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 09/05/2016.
 */
@DocumentEntity
public class BillingAccountCreateUpdateRoleRequest extends CreateUpdateRoleRequest {
    /**
     *  types : the type of roles on the billing account
     */
    @DocumentProperty("types")
    private ListProperty<BillingAccountPartyRole.RoleType> types = new ArrayListProperty<>(BillingAccountCreateUpdateRoleRequest.this);
    /**
     *  baId : the target billing account id (for creation only)
     */
    @DocumentProperty("baId") @NotNull
    private Property<String> baId = new ImmutableProperty<>(BillingAccountCreateUpdateRoleRequest.this);

    /**
     * Getter of types
     * @return the whole (immutable) list of types
     */
    public List<BillingAccountPartyRole.RoleType> getTypes() { return types.get(); }
    /**
     * Setter of types
     * @param newTypes the new collection of types
     */
    public void setTypes(Collection<BillingAccountPartyRole.RoleType> newTypes) { types.set(newTypes); }
    /**
     * Add a new entry to the property types
     * @param newTypes the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addTypes(BillingAccountPartyRole.RoleType newTypes){ return types.add(newTypes); }
    /**
     * Add a new entry to the property types at the specified position
     * @param index the new entry to be added
     * @param newTypes the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addTypes(int index,BillingAccountPartyRole.RoleType newTypes){ return types.add(newTypes); }
    /**
     * Remove an entry to the property types
     * @param oldTypes the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeTypes(BillingAccountPartyRole.RoleType oldTypes){ return types.remove(oldTypes); }
    /**
     * Remove an entry to the property types at the specified position
     * @param index the position of element to be removed
     * @return the entry removed if any
     */
    public BillingAccountPartyRole.RoleType removeTypes(int index){ return types.remove(index); }

    /**
     * Getter of baId
     * @return the value of baId
     */
    public String getBaId() { return baId.get(); }
    /**
     * Setter of baId
     * @param val the new value for baId
     */
    public void setBaId(String val) { baId.set(val); }
}
