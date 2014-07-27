package com.dreameddeath.billing.model;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.property.ArrayListProperty;
import com.dreameddeath.common.model.property.ImmutableProperty;
import com.dreameddeath.common.model.property.Property;
import com.dreameddeath.party.model.PartyRole;

import java.util.Collections;
import java.util.List;

/**
 * Created by ceaj8230 on 27/07/2014.
 */
public class BillingAccountPartyRole extends PartyRole {
    @DocumentProperty("roles")
    private List<RoleType> _roles = new ArrayListProperty<RoleType>(BillingAccountPartyRole.this);
    @DocumentProperty("ba")
    private Property<BillingAccountLink> _ba=new ImmutableProperty<BillingAccountLink>(BillingAccountPartyRole.this);


    public List<RoleType> getRoles(){ return Collections.unmodifiableList(_roles);}
    public void setRoles(List<RoleType> roles){_roles.clear();_roles.addAll(roles);}

    public void setBa(BillingAccountLink baLink){_ba.set(baLink);}
    public BillingAccountLink getBa(){return _ba.get();}

    public enum RoleType{
        HOLDER,
        PAYER
    }
}
