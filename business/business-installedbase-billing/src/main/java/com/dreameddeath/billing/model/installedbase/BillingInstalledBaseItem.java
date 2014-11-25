package com.dreameddeath.billing.model.installedbase;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 12/08/2014.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.MINIMAL_CLASS, include= JsonTypeInfo.As.PROPERTY, property="@c")
public class BillingInstalledBaseItem extends CouchbaseDocumentElement {
    /**
     *  id : internal id of the item for crossrefs
     */
    @DocumentProperty("id")
    private Property<Long> _id = new StandardProperty<Long>(BillingInstalledBaseItem.this);
    /**
     *  statuses : List of statuses (history by dates)
     */
    @DocumentProperty("statuses")
    private ListProperty<BillingInstalledBaseItemStatus> _statuses = new ArrayListProperty<BillingInstalledBaseItemStatus>(BillingInstalledBaseItem.this);

    // id accessors
    public Long getId() { return _id.get(); }
    public void setId(Long val) { _id.set(val); }
    // Statuses Accessors
    public List<BillingInstalledBaseItemStatus> getStatuses() { return _statuses.get(); }
    public void setStatuses(Collection<BillingInstalledBaseItemStatus> vals) { _statuses.set(vals); }
    public boolean addStatuses(BillingInstalledBaseItemStatus val){ return _statuses.add(val); }
    public boolean removeStatuses(BillingInstalledBaseItemStatus val){ return _statuses.remove(val); }

    public BillingInstalledBaseItem getItemById(Long id){ return Helper.getFirstParentOfClass(this,BillingInstalledBase.class).getItemById(id);}
    public <T extends BillingInstalledBaseItem> T getItemById(Long id,Class<T> clazz){ return (T)getItemById(id);}
}
