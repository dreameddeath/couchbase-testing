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

package com.dreameddeath.party.model.v1;

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 27/07/2014.
 */
public abstract class PartyRole extends VersionedDocumentElement {
    @DocumentProperty("uid")
    private ImmutableProperty<String> uid=new ImmutableProperty<>(PartyRole.this, UUID.randomUUID().toString());
    @DocumentProperty("startDate")
    private Property<DateTime> startDate = new StandardProperty<>(PartyRole.this);
    @DocumentProperty("endDate")
    private Property<DateTime> endDate= new StandardProperty<>(PartyRole.this);
    /**
     *  level : the level of the role
     */
    @DocumentProperty("level")
    private Property<PartyRoleLevel> level = new ImmutableProperty<>(PartyRole.this);

    /**
     *  type : the type of party role
     */
    @DocumentProperty("type")
    private Property<String> type = new ImmutableProperty<>(PartyRole.this);
    /**
     *  key : The target owned object
     */
    @DocumentProperty("key")
    private Property<String> key = new ImmutableProperty<>(PartyRole.this);

    public String getUid() { return uid.get(); }
    public void setUid(String uid) { this.uid.set(uid); }

    public DateTime getStartDate() { return startDate.get(); }
    public void setStartDate(DateTime startDate) { this.startDate.set(startDate); }

    public DateTime getEndDate() { return endDate.get(); }
    public void setEndDate(DateTime endDate) { this.endDate.set(endDate); }

    /**
     * Getter of type
     * @return the value of type
     */
    public String getType() { return type.get(); }
    /**
     * Setter of type
     * @param val the new value for type
     */
    public void setType(String val) { type.set(val); }

    /**
     * Getter of key
     * @return the value of key
     */
    public String getKey() { return key.get(); }
    /**
     * Setter of key
     * @param val the new value of key
     */
    public void setKey(String val) { key.set(val); }
    /**
     * Getter of level
     * @return the value of level
     */
    public PartyRoleLevel getLevel() { return level.get(); }
    /**
     * Setter of level
     * @param val the new value for level
     */
    public void setLevel(PartyRoleLevel val) { level.set(val); }


    public enum PartyRoleLevel {
        OWNER,
        MANAGER,
        USER,
        INFO
    }
}
