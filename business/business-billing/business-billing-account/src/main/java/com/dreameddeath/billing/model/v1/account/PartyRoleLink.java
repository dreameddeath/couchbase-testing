/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.billing.model.v1.account;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.query.annotation.QueryExpose;

/**
 * Created by Christophe Jeunesse on 19/05/2016.
 */
@QueryExpose(rootPath = "",isPureSubClassMode = true)
public class PartyRoleLink extends CouchbaseDocumentElement{
    /**
     *  pid : the party id
     */
    @DocumentProperty("pid")
    private Property<String> pid = new ImmutableProperty<>(PartyRoleLink.this);
    /**
     *  roleUid : the linked role uid
     */
    @DocumentProperty("roleUid")
    private Property<String> roleUid = new ImmutableProperty<>(PartyRoleLink.this);

    /**
     * Getter of pid
     * @return the value of pid
     */
    public String getPid() { return pid.get(); }
    /**
     * Setter of pid
     * @param val the new value for pid
     */
    public void setPid(String val) { pid.set(val); }

    /**
     * Getter of roleUid
     * @return the value of roleUid
     */
    public String getRoleUid() { return roleUid.get(); }
    /**
     * Setter of roleUid
     * @param val the new value for roleUid
     */
    public void setRoleUid(String val) { roleUid.set(val); }
}
