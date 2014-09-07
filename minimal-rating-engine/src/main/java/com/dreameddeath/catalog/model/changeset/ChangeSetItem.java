package com.dreameddeath.catalog.model.changeset;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by ceaj8230 on 07/09/2014.
 */
public class ChangeSetItem extends CouchbaseDocumentElement {
    /**
     *  id : Catalog element item id
     */
    @DocumentProperty("id")
    private Property<String> _id = new StandardProperty<String>(ChangeSetItem.this);
    /**
     *  version : Version in string format
     */
    @DocumentProperty("version")
    private Property<String> _version = new StandardProperty<String>(ChangeSetItem.this);


    // id accessors
    public String getId() { return _id.get(); }
    public void setId(String val) { _id.set(val); }
    // version accessors
    public String getVersion() { return _version.get(); }
    public void setVersion(String val) { _version.set(val); }
}
