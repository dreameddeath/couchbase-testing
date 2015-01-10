package com.dreameddeath.billing.model.order;


import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.account.BillingAccountLink;
import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.annotation.dao.Counter;
import com.dreameddeath.core.annotation.dao.DaoEntity;
import com.dreameddeath.core.annotation.dao.ParentEntity;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocument;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 01/09/2014.
 */
@DocumentDef(domain = "billing",name="order",version = "1.0.0")
@DaoEntity(baseDao= BusinessCouchbaseDocumentDao.class,dbPath = "order/",idPattern = "\\d{5}",idFormat = "%05d")
@ParentEntity(c= BillingAccount.class,keyPath = "billingAccount.key",separator = "/")
@Counter(name = "cnt",dbName = "cnt",isKeyGen = true)
public class BillingOrder extends BusinessCouchbaseDocument {
    /**
     *  billingAccount : Link to the parent billing Account
     */
    @DocumentProperty("billingAccount") @NotNull
    private Property<BillingAccountLink> _billingAccount = new StandardProperty<BillingAccountLink>(BillingOrder.this);
    /**
     *  items : List of order items
     */
    @DocumentProperty("items")
    private ListProperty<BillingOrderItem> _items = new ArrayListProperty<BillingOrderItem>(BillingOrder.this);

    // Items Accessors
    public List<BillingOrderItem> getItems() { return _items.get();  }
    public void setItems(Collection<BillingOrderItem> vals) { _items.set(vals); }
    public boolean addItems(BillingOrderItem val){ return _items.add(val); }
    public boolean removeItems(BillingOrderItem val){ return _items.remove(val); }

    // billingAccount accessors
    public BillingAccountLink getBillingAccount() { return _billingAccount.get(); }
    public void setBillingAccount(BillingAccountLink val) { _billingAccount.set(val); }


}
