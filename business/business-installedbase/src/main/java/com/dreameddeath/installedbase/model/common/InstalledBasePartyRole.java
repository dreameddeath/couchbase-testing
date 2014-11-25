package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.party.model.base.PartyRole;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 10/08/2014.
 */
public class InstalledBasePartyRole extends PartyRole {
    /**
     *  roles : List of roles on this Installed base (or part of it)
     */
    @DocumentProperty("roles")
    private ListProperty<Roles> _roles = new ArrayListProperty<Roles>(InstalledBasePartyRole.this);
    /**
     *  installedBase : The installed base implied by the role
     */
    @DocumentProperty("installedBase")
    private Property<InstalledBaseLink> _installedBase = new StandardProperty<InstalledBaseLink>(InstalledBasePartyRole.this);

    // Roles Accessors
    public List<Roles> getRoles() { return _roles.get(); }
    public void setRoles(Collection<Roles> vals) { _roles.set(vals); }
    public boolean addRoles(Roles val){ return _roles.add(val); }
    public boolean removeRoles(Roles val){ return _roles.remove(val); }

    // installedBase accessors
    public InstalledBaseLink getInstalledBase() { return _installedBase.get(); }
    public void setInstalledBase(InstalledBaseLink val) { _installedBase.set(val); }


    public enum Roles{
        HOLDER,
        USER,
        MANAGER
    }
}
