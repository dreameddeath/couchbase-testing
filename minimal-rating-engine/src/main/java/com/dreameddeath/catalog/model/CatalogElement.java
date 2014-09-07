package com.dreameddeath.catalog.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by ceaj8230 on 05/09/2014.
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
