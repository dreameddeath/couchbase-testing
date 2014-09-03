package com.dreameddeath.billing.model.order;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ArrayListProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.StandardProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 01/09/2014.
 */
public class BillingOrderItem extends CouchbaseDocumentElement {
    /**
     *  id : The internal id of the order being billed
     */
    @DocumentProperty("id")
    private Property<Long> _id = new StandardProperty<Long>(BillingOrderItem.this);
    /**
     *  statuses : Statuses of the order item
     */
    @DocumentProperty("statuses")
    private ListProperty<BillingOrderItemStatus> _statuses = new ArrayListProperty<BillingOrderItemStatus>(BillingOrderItem.this);


    // id accessors
    public Long getId() { return _id.get(); }
    public void setId(Long val) { _id.set(val); }
    // Statuses Accessors
    public List<BillingOrderItemStatus> getStatuses() { return _statuses.get(); }
    public void setStatuses(Collection<BillingOrderItemStatus> vals) { _statuses.set(vals); }
    public boolean addStatuses(BillingOrderItemStatus val){ return _statuses.add(val); }
    public boolean removeStatuses(BillingOrderItemStatus val){ return _statuses.remove(val); }
}
