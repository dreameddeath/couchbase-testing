package com.dreameddeath.billing.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ArrayListProperty;
import com.dreameddeath.core.model.property.ImmutableProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.party.model.PartyRole;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 27/07/2014.
 */
public class BillingAccountPartyRole extends PartyRole {
    @DocumentProperty("roles")
    private ListProperty<RoleType> _roles = new ArrayListProperty<RoleType>(BillingAccountPartyRole.this);
    @DocumentProperty("ba")
    private Property<BillingAccountLink> _ba=new ImmutableProperty<BillingAccountLink>(BillingAccountPartyRole.this);


    public List<RoleType> getRoles(){ return _roles.get();}
    public void setRoles(List<RoleType> roles){_roles.set(roles);}
    public void addRole(RoleType role){
        if(_roles.indexOf(role)<0){
            _roles.add(role);
        }
    }

    public void setBa(BillingAccountLink baLink){_ba.set(baLink);}
    public BillingAccountLink getBa(){return _ba.get();}

    public enum RoleType{
        HOLDER,
        PAYER
    }
}
