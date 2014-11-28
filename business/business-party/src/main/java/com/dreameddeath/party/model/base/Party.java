package com.dreameddeath.party.model.base;

import com.dreameddeath.common.model.ExternalId;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocument;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 27/07/2014.
 */
//@JsonTypeInfo(use= JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@c")
public abstract class Party extends BusinessCouchbaseDocument {
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
