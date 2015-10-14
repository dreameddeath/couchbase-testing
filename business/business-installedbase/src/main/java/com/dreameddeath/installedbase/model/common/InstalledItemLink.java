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
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by Christophe Jeunesse on 31/08/2014.
 */
public class InstalledItemLink extends CouchbaseDocumentElement {
    /**
     *  id : Target item id
     */
    @DocumentProperty("id")
    private Property<String> id = new StandardProperty<String>(InstalledItemLink.this);
    /**
     *  type : The type of link
     */
    @DocumentProperty("type")
    private Property<Type> type = new StandardProperty<Type>(InstalledItemLink.this);
    /**
     *  direction : Direction of the link
     */
    @DocumentProperty("direction")
    private Property<Direction> direction = new StandardProperty<Direction>(InstalledItemLink.this);
    /**
     *  status : Link Status
     */
    @DocumentProperty("status")
    private Property<InstalledStatus> status = new StandardProperty<InstalledStatus>(InstalledItemLink.this);

    // id accessors
    public String getId() { return id.get(); }
    public void setId(String val) { id.set(val); }
    // type accessors
    public Type getType() { return type.get(); }
    public void setType(Type val) { type.set(val); }
    // direction accessors
    public Direction getDirection() { return direction.get(); }
    public void setDirection(Direction val) { direction.set(val); }
    // status accessors
    public InstalledStatus getStatus() { return status.get(); }
    public void setStatus(InstalledStatus val) { status.set(val); }

    public enum Type{
        RELIES, //The item is relying on the targetted item
        BRINGS, //The item has been automatically added the targetted item
        MIGRATE,//The item has been migrated to another item
        MOVED //The item has been moved to a new Parent
    }

    public enum Direction{
        FROM,
        TO
    }

}
