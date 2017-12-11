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

package com.dreameddeath.installedbase.model.v1.common;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.query.annotation.QueryExpose;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.dreameddeath.installedbase.utils.InstalledBaseTools.Statuses.getMatchingStatuses;
import static com.dreameddeath.installedbase.utils.InstalledBaseTools.Statuses.getStatusFromHistory;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
@QueryExpose(rootPath = "dummy",isClassRootHierarchy = true,notDirecltyExposed = true,defaultOutputFieldMode = FieldGenMode.SIMPLE,superClassGenMode = SuperClassGenMode.UNWRAP)
public abstract class InstalledItem<T extends InstalledItemRevision> extends CouchbaseDocumentElement implements IHasStatus {
    @DocumentProperty("id") @NotNull
    private Property<String> id = new ImmutableProperty<>(InstalledItem.this, UUID.randomUUID().toString());
    @DocumentProperty("creationDate") @NotNull
    private Property<DateTime> creationDate = new ImmutableProperty<>(InstalledItem.this);
    @DocumentProperty("lastModificationDate") @NotNull
    private Property<DateTime> lastModificationDate = new StandardProperty<>(InstalledItem.this);
    /**
     *  statusHistory : history of statuses
     */
    @DocumentProperty("statuses")
    private ListProperty<InstalledStatus> statuses = new ArrayListProperty<>(InstalledItem.this);
    /**
     *  code : The code of the item
     */
    @DocumentProperty("code") @NotNull
    private Property<String> code = new StandardProperty<>(InstalledItem.this);
    /**
     *  revisions : planned revisions
     */
    @DocumentProperty("revisions")
    private ListProperty<T> revisions = new ArrayListProperty<>(InstalledItem.this);

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
    @JsonIgnore
    public InstalledStatus getStatus(DateTime ref) {
        return getStatusFromHistory(statuses,ref);
    }

    @Override
    public List<InstalledStatus> getStatuses(DateTime startDate, DateTime endDate) {
        return getMatchingStatuses(statuses,startDate,endDate);
    }

    @Override
    public List<InstalledStatus> getOverlappingStatuses(InstalledStatus refStatus) {
        return getMatchingStatuses(statuses,refStatus.getStartDate(),refStatus.getEndDate());
    }



    // code accessors
    public String getCode() { return code.get(); }
    public void setCode(String val) { code.set(val); }

    // Revisions Accessors
    public List<T> getRevisions() { return revisions.get(); }
    public void setRevisions(Collection<T> vals) { revisions.set(vals); }
    public boolean addRevision(T val){ return revisions.add(val); }
    public boolean removeRevision(T val){ return revisions.remove(val); }

    /**
     * Getter of statusHistory
     * @return the content
     */
    public List<InstalledStatus> getStatuses() { return statuses.get(); }
    /**
     * Setter of statusHistory
     * @param vals the new collection of values
     */
    public void setStatuses(Collection<InstalledStatus> vals) { statuses.set(vals); }
    /**
     * Add a new entry to the property statusHistory
     * @param val the new entry to be added
     */
    public boolean addStatus(InstalledStatus val){ return statuses.add(val); }
    /**
     * Remove an entry to the property statusHistory
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeStatus(InstalledStatus val){ return statuses.remove(val); }

}
