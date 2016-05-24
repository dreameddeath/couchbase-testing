package com.dreameddeath.party.process.model.v1.roles.tasks;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.process.model.v1.tasks.DocumentUpdateTask;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.dreameddeath.party.model.v1.Party;
import com.dreameddeath.party.process.model.v1.roles.CreateUpdateRoleResult;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 10/05/2016.
 */
@DocumentEntity
public class CreateUpdatePartyRolesTask extends DocumentUpdateTask<Party> {
    /**
     *  partyId : party id to update the roles for
     */
    @DocumentProperty("partyId") @NotNull
    private Property<String> partyId = new ImmutableProperty<>(CreateUpdatePartyRolesTask.this);
    /**
     *  createUpdateRoles : list of roles to being updated
     */
    @DocumentProperty("createUpdateRoles")
    private ListProperty<CreateUpdateRoleResult> createUpdateRoles = new ArrayListProperty<>(CreateUpdatePartyRolesTask.this);

    /**
     * Getter of partyId
     * @return the value of partyId
     */
    public String getPartyId() { return partyId.get(); }
    /**
     * Setter of partyId
     * @param val the new value of partyId
     */
    public void setPartyId(String val) { partyId.set(val); }

    /**
     * Getter of createUpdateRoles
     * @return the whole (immutable) list of createUpdateRoles
     */
    public List<CreateUpdateRoleResult> getCreateUpdateRoles() { return createUpdateRoles.get(); }
    /**
     * Setter of createUpdateRoles
     * @param newCreateUpdateRoles the new collection of createUpdateRoles
     */
    public void setCreateUpdateRoles(Collection<CreateUpdateRoleResult> newCreateUpdateRoles) { createUpdateRoles.set(newCreateUpdateRoles); }
    /**
     * Add a new entry to the property createUpdateRoles
     * @param newCreateUpdateRoles the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addCreateUpdateRoles(CreateUpdateRoleResult newCreateUpdateRoles){ return createUpdateRoles.add(newCreateUpdateRoles); }
    /**
     * Add a new entry to the property createUpdateRoles at the specified position
     * @param index the new entry to be added
     * @param newCreateUpdateRoles the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addCreateUpdateRoles(int index,CreateUpdateRoleResult newCreateUpdateRoles){ return createUpdateRoles.add(newCreateUpdateRoles); }
    /**
     * Remove an entry to the property createUpdateRoles
     * @param oldCreateUpdateRoles the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeCreateUpdateRoles(CreateUpdateRoleResult oldCreateUpdateRoles){ return createUpdateRoles.remove(oldCreateUpdateRoles); }
    /**
     * Remove an entry to the property createUpdateRoles at the specified position
     * @param index the position of element to be removed
     * @return the entry removed if any
     */
    public CreateUpdateRoleResult removeCreateUpdateRoles(int index){ return createUpdateRoles.remove(index); }


}
