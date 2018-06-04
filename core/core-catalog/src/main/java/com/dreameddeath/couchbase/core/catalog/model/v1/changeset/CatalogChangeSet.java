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

package com.dreameddeath.couchbase.core.catalog.model.v1.changeset;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 05/09/2014.
 */
public class CatalogChangeSet extends CouchbaseDocumentElement {
    /**
     *  name : name of the change set
     */
    @DocumentProperty("name")
    private Property<String> name = new StandardProperty<>(CatalogChangeSet.this);
    /**
     *  descr : Description of the changeset
     */
    @DocumentProperty("descr")
    private Property<String> descr = new StandardProperty<>(CatalogChangeSet.this);
    /**
     *  domainChanges : List of changes per domain
     */
    @DocumentProperty("domainChanges")
    private ListProperty<ChangeSetItem> changes = new ArrayListProperty<>(CatalogChangeSet.this);


    // name accessors
    public String getName() { return name.get(); }
    public void setName(String val) { name.set(val); }
    // descr accessors
    public String getDescr() { return descr.get(); }
    public void setDescr(String val) { descr.set(val); }
    // DomainChanges Accessors
    public List<ChangeSetItem> getChanges() { return changes.get(); }
    public void setChanges(List<ChangeSetItem> vals) { changes.set(vals); }
    public void addChange(ChangeSetItem item) { changes.add(item); }
}
