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

package com.dreameddeath.party.model.base;

import com.dreameddeath.common.model.ExternalId;
import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 27/07/2014.
 */
//@JsonTypeInfo(use= JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@c")
public abstract class Party extends BusinessDocument {
    @DocumentProperty("uid")
    private ImmutableProperty<String> _uid=new ImmutableProperty<String>(Party.this);
    @DocumentProperty(value="partyRoles")
    private List<PartyRole> _partyRoles = new ArrayListProperty<PartyRole>(Party.this);
    /**
     *  externalIds : List of Party external ids
     */
    @DocumentProperty("externalIds")
    private ListProperty<ExternalId> _externalIds = new ArrayListProperty<ExternalId>(Party.this);



    public String getUid() { return _uid.get(); }
    public void setUid(String uid) { _uid.set(uid); }

    public List<PartyRole> getPartyRoles() { return Collections.unmodifiableList(_partyRoles); }
    public void setPartyRoles(Collection<PartyRole> partyRoles){
        _partyRoles.clear();
        _partyRoles.addAll(partyRoles);
    }
    public void addPartyRole(PartyRole partyRole){
        _partyRoles.add(partyRole);
    }


    // ExternalIds Accessors
    public List<ExternalId> getExternalIds() { return _externalIds.get(); }
    public void setExternalIds(Collection<ExternalId> vals) { _externalIds.set(vals); }
    public boolean addExternalIds(ExternalId val){ return _externalIds.add(val); }
    public boolean removeExternalIds(ExternalId val){ return _externalIds.remove(val); }

    public PartyLink newLink(){
        return new PartyLink(this);
    }

}
