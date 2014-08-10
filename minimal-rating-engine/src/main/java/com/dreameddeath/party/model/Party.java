package com.dreameddeath.party.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.ArrayListProperty;
import com.dreameddeath.core.model.property.ImmutableProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 27/07/2014.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@c")
public abstract class Party extends CouchbaseDocument {
    @DocumentProperty("uid")
    private ImmutableProperty<String> _uid=new ImmutableProperty<String>(Party.this);
    @DocumentProperty(value="partyRoles")
    private List<PartyRole> _partyRoles = new ArrayListProperty<PartyRole>(Party.this);


    public String getUid() { return _uid.get(); }
    public void setUid(String uid) { _uid.set(uid); }

    public List<PartyRole> getPartyRoles() { return Collections.unmodifiableList(_partyRoles); }
    public void setPartyRoleLinks(Collection<PartyRole> partyRoles){
        _partyRoles.clear();
        _partyRoles.addAll(partyRoles);
    }

    public void addPartyRole(PartyRole partyRole){
        _partyRoles.add(partyRole);
    }

    public PartyLink newPartyLink(){
        return new PartyLink(this);
    }

}
