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

package com.dreameddeath.couchbase.core.catalog.model.v1;

import com.dreameddeath.core.json.model.Version;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;

/**
 * Created by Christophe Jeunesse on 05/09/2014.
 */
public abstract class CatalogElement extends CouchbaseDocument {
    /**
     *  uid : unique element id (regarding to the object type)
     */
    @DocumentProperty("id")
    private Property<String> id = new ImmutableProperty<>(CatalogElement.this);
    /**
     *  version : Version of this item
     */
    @DocumentProperty("version")
    private Property<Version> version = new ImmutableProperty<>(CatalogElement.this);
    /**
     *  previousVersion : Give the immediate previous version (if any)
     */
    @DocumentProperty("previousVersion")
    private Property<Version> previousVersion = new ImmutableProperty<>(CatalogElement.this);

    // uid accessors
    public String getId() { return id.get(); }
    public void setId(String val) { id.set(val); }
    // version accessors
    public Version getVersion() { return version.get(); }
    public void setVersion(Version val) { version.set(val); }
    // previousVersion accessors
    public Version getPreviousVersion() { return previousVersion.get(); }
    public void setPreviousVersion(Version val) { previousVersion.set(val); }
}
