package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.StandardProperty;

/**
 * Created by ceaj8230 on 10/08/2014.
 */
public abstract class InstalledItemRevision extends CouchbaseDocumentElement {
    /**
     *  orderId : Id of the order asking for a modification
     */
    @DocumentProperty("orderId")
    private Property<String> _orderId = new StandardProperty<String>(InstalledItemRevision.this);
    /**
     *  orderItemId : the id of the order item requesting a modification
     */
    @DocumentProperty("orderItemId")
    private Property<String> _orderItemId = new StandardProperty<String>(InstalledItemRevision.this);
    /**
     *  status : Status item linked to the revision
     */
    @DocumentProperty("status")
    private Property<InstalledStatus> _status = new StandardProperty<InstalledStatus>(InstalledItemRevision.this);

    // orderId accessors
    public String getOrderId() { return _orderId.get(); }
    public void setOrderId(String val) { _orderId.set(val); }

    // orderItemId accessors
    public String getOrderItemId() { return _orderItemId.get(); }
    public void setOrderItemId(String val) { _orderItemId.set(val); }

    // status accessors
    public InstalledStatus getStatus() { return _status.get(); }
    public void setStatus(InstalledStatus val) { _status.set(val); }

}
