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

package com.dreameddeath.catalog.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by Christophe Jeunesse on 05/09/2014.
 */
public abstract class CatalogElement extends CouchbaseDocument {
    /**
     *  uid : unique element id (regarding dao domain)
     */
    @DocumentProperty("uid")
    private Property<String> _uid = new StandardProperty<String>(CatalogElement.this);
    /**
     *  version : Version of this item
     */
    @DocumentProperty("version")
    private Property<CatalogItemVersion> _version = new StandardProperty<CatalogItemVersion>(CatalogElement.this);
    /**
     *  previousVersion : Give the immediate previous version (if any)
     */
    @DocumentProperty("previousVersion")
    private Property<String> _previousVersion = new StandardProperty<String>(CatalogElement.this);

    // uid accessors
    public String getUid() { return _uid.get(); }
    public void setUid(String val) { _uid.set(val); }
    // version accessors
    public CatalogItemVersion getVersion() { return _version.get(); }
    public void setVersion(CatalogItemVersion val) { _version.set(val); }
    // previousVersion accessors
    public String getPreviousVersion() { return _previousVersion.get(); }
    public void setPreviousVersion(String val) { _previousVersion.set(val); }
}
