package com.dreameddeath.common.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by ceaj8230 on 11/08/2014.
 */
public class ExternalId extends CouchbaseDocumentElement {
    /**
     *  id : The external id
     */
    @DocumentProperty("id")
    private Property<String> _id = new StandardProperty<String>(ExternalId.this);
    /**
     *  refCode : Id of the referential for this code
     */
    @DocumentProperty("referentialCode")
    private Property<String> _refCode = new StandardProperty<String>(ExternalId.this);
    /**
     *  referentialInstance : the instance id of the referential
     */
    @DocumentProperty("referentialInstance")
    private Property<String> _referentialInstance = new StandardProperty<String>(ExternalId.this);

    // id accessors
    public String getId() { return _id.get(); }
    public void setId(String val) { _id.set(val); }

    // refCode accessors
    public String getRefCode() { return _refCode.get(); }
    public void setRefCode(String val) { _refCode.set(val); }

    // referentialInstance accessors
    public String getReferentialInstance() { return _referentialInstance.get(); }
    public void setReferentialInstance(String val) { _referentialInstance.set(val); }

}
