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
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
public abstract class InstalledItem<T extends InstalledItemRevision> extends CouchbaseDocumentElement {
    @DocumentProperty("id")
    private Property<String> id = new StandardProperty<String>(InstalledItem.this, UUID.randomUUID().toString());
    @DocumentProperty("creationDate")
    private Property<DateTime> creationDate = new StandardProperty<DateTime>(InstalledItem.this);
    @DocumentProperty("lastModificationDate")
    private Property<DateTime> lastModificationDate = new StandardProperty<DateTime>(InstalledItem.this);
    /**
     *  status :
     */
    @DocumentProperty("status")
    private Property<InstalledStatus> status = new StandardProperty<InstalledStatus>(InstalledItem.this);
    /**
     *  code : The code of the item
     */
    @DocumentProperty("code")
    private Property<String> code = new StandardProperty<String>(InstalledItem.this);
    /**
     *  revisions : ItemRevisions
     */
    @DocumentProperty("revisions")
    private ListProperty<T> revisions = new ArrayListProperty<T>(InstalledItem.this);

    // id accessors
    public String getId() { return id.get(); }
    public void setId(String val) { id.set(val); }

    // creationDate accessors
    public DateTime getCreationDate() { return creationDate.get(); }
    public void setCreationDate(DateTime val) { creationDate.set(val); }

    // lastModificationDate accessors
    public DateTime getLastModificationDate() { return lastModificationDate.get(); }
    public void setLastModificationDate(DateTime val) { lastModificationDate.set(val); }

    // status accessors
    public InstalledStatus getStatus() { return status.get(); }
    public void setStatus(InstalledStatus val) { status.set(val); }

    // code accessors
    public String getCode() { return code.get(); }
    public void setCode(String val) { code.set(val); }

    // Revisions Accessors
    public List<T> getRevisions() { return revisions.get(); }
    public void setRevisions(Collection<T> vals) { revisions.set(vals); }
    public boolean addRevisions(T val){ return revisions.add(val); }
    public boolean removeRevisions(T val){ return revisions.remove(val); }

}
