/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.installedbase.model.v1.common;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.party.model.base.PartyRole;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
public class InstalledBasePartyRole extends PartyRole {
    /**
     *  roles : List of roles on this Installed base (or part of it)
     */
    @DocumentProperty("roles")
    private ListProperty<Roles> roles = new ArrayListProperty<>(InstalledBasePartyRole.this);
    /**
     *  installedBase : The installed base implied by the role
     */
    @DocumentProperty("installedBase")
    private Property<InstalledBaseLink> installedBase = new StandardProperty<>(InstalledBasePartyRole.this);

    // Roles Accessors
    public List<Roles> getRoles() { return roles.get(); }
    public void setRoles(Collection<Roles> vals) { roles.set(vals); }
    public boolean addRoles(Roles val){ return roles.add(val); }
    public boolean removeRoles(Roles val){ return roles.remove(val); }

    // installedBase accessors
    public InstalledBaseLink getInstalledBase() { return installedBase.get(); }
    public void setInstalledBase(InstalledBaseLink val) { installedBase.set(val); }


    public enum Roles{
        HOLDER,
        USER,
        MANAGER
    }
}
