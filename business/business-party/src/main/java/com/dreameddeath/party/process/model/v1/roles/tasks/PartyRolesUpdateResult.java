package com.dreameddeath.party.process.model.v1.roles.tasks;

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.party.process.model.v1.roles.CreateUpdateRoleResult;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 10/05/2016.
 */
public class PartyRolesUpdateResult extends VersionedDocumentElement {
    /**
     *  roles : result of roles updates
     */
    @DocumentProperty("roles")
    private ListProperty<CreateUpdateRoleResult> roles = new ArrayListProperty<>(PartyRolesUpdateResult.this);

    /**
     * Getter of roles
     * @return the whole (immutable) list of roles
     */
    public List<CreateUpdateRoleResult> getRoles() { return roles.get(); }
    /**
     * Setter of roles
     * @param newRoles the new collection of roles
     */
    public void setRoles(Collection<CreateUpdateRoleResult> newRoles) { roles.set(newRoles); }
    /**
     * Add a new entry to the property roles
     * @param newRoles the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addRoles(CreateUpdateRoleResult newRoles){ return roles.add(newRoles); }
    /**
     * Add a new entry to the property roles at the specified position
     * @param index the new entry to be added
     * @param newRoles the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addRoles(int index,CreateUpdateRoleResult newRoles){ return roles.add(newRoles); }
    /**
     * Remove an entry to the property roles
     * @param oldRoles the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeRoles(CreateUpdateRoleResult oldRoles){ return roles.remove(oldRoles); }
    /**
     * Remove an entry to the property roles at the specified position
     * @param index the position of element to be removed
     * @return the entry removed if any
     */
    public CreateUpdateRoleResult removeRoles(int index){ return roles.remove(index); }
    
}
