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

package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 31/08/2014.
 */
public class InstalledItemLink extends CouchbaseDocumentElement {
    /**
     *  id : Target item id
     */
    @DocumentProperty("targetId")
    private Property<String> targetId = new ImmutableProperty<>(InstalledItemLink.this);
    /**
     *  type : The type of link
     */
    @DocumentProperty("type")
    private Property<Type> type = new ImmutableProperty<>(InstalledItemLink.this);
    /**
     *  direction : Direction of the link
     */
    @DocumentProperty("direction")
    private Property<Direction> direction = new ImmutableProperty<>(InstalledItemLink.this);
    /**
     *  status : Link Status
     */
    @DocumentProperty("status")
    private Property<InstalledStatus> status = new StandardProperty<>(InstalledItemLink.this);
    /**
     *  statusHistory : history of statuses
     */
    @DocumentProperty("statusHistory")
    private ListProperty<InstalledStatus> statusHistory = new ArrayListProperty<>(InstalledItemLink.this);

    // id accessors
    public String getTargetId() { return targetId.get(); }
    public void setTargetId(String val) { targetId.set(val); }
    // type accessors
    public Type getType() { return type.get(); }
    public void setType(Type val) { type.set(val); }
    // direction accessors
    public Direction getDirection() { return direction.get(); }
    public void setDirection(Direction val) { direction.set(val); }
    // status accessors
    public InstalledStatus getStatus() { return status.get(); }
    public void setStatus(InstalledStatus val) { status.set(val); }
    /**
     * Getter of statusHistory
     * @return the content
     */
    public List<InstalledStatus> getStatusHistory() { return statusHistory.get(); }
    /**
     * Setter of statusHistory
     * @param vals the new collection of values
     */
    public void setStatusHistory(Collection<InstalledStatus> vals) { statusHistory.set(vals); }
    /**
     * Add a new entry to the property statusHistory
     * @param val the new entry to be added
     */
    public boolean addStatusHistory(InstalledStatus val){ return statusHistory.add(val); }
    /**
     * Remove an entry to the property statusHistory
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeStatusHistory(InstalledStatus val){ return statusHistory.remove(val); }


    public enum Type{
        RELIES, //The item is relying on the targetted item
        BRINGS, //The item has been automatically added the targetted item
        MIGRATE,//The item has been migrated to another item
        AGGREGATE,
        MOVED, //The item has been moved to a new Parent
        PARENT //Used for parent history (Atomic offer for ps,composite offer for others)
    }

    public enum Direction{
        FROM,
        TO
    }
}
