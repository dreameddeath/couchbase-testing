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

package com.dreameddeath.billing.model.account;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.party.model.base.PartyRole;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 27/07/2014.
 */
public class BillingAccountPartyRole extends PartyRole {
    @DocumentProperty("roles")
    private ListProperty<RoleType> _roles = new ArrayListProperty<>(BillingAccountPartyRole.this);
    @DocumentProperty("ba")
    private Property<BillingAccountLink> _ba=new ImmutableProperty<>(BillingAccountPartyRole.this);


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
        PAYER,
        BILL_RECEIVER
    }
}
