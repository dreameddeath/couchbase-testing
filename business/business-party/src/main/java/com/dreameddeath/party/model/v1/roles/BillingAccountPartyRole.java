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

package com.dreameddeath.party.model.v1.roles;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.dreameddeath.party.model.v1.PartyRole;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 27/07/2014.
 */
@DocumentEntity
public class BillingAccountPartyRole extends PartyRole {
    @DocumentProperty("roles")
    private ListProperty<RoleType> roles = new ArrayListProperty<>(BillingAccountPartyRole.this);
    @DocumentProperty("baUid") @NotNull
    private Property<String> baUid =new ImmutableProperty<>(BillingAccountPartyRole.this);


    public List<RoleType> getRoles(){
        return roles.get();
    }
    public void setRoles(List<RoleType> roles){
        this.roles.set(roles);
    }
    public void addRole(RoleType role){
        if(roles.indexOf(role)<0){
            roles.add(role);
        }
    }

    public void setBaUid(String baId){
        baUid.set(baId);
    }
    public String getBaUid(){
        return baUid.get();
    }

    public enum RoleType{
        HOLDER,
        PAYER,
        BILL_RECEIVER
    }
}
