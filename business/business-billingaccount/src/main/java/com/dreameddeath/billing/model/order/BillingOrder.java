/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.billing.model.order;


import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.account.BillingAccountLink;
import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.dao.helper.annotation.Counter;
import com.dreameddeath.core.dao.helper.annotation.DaoEntity;
import com.dreameddeath.core.dao.helper.annotation.ParentEntity;
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
    private Property<BillingAccountLink> _billingAccount = new StandardProperty<>(BillingOrder.this);
    /**
     *  items : List of order items
     */
    @DocumentProperty("items")
    private ListProperty<BillingOrderItem> _items = new ArrayListProperty<>(BillingOrder.this);

    // Items Accessors
    public List<BillingOrderItem> getItems() { return _items.get();  }
    public void setItems(Collection<BillingOrderItem> vals) { _items.set(vals); }
    public boolean addItems(BillingOrderItem val){ return _items.add(val); }
    public boolean removeItems(BillingOrderItem val){ return _items.remove(val); }

    // billingAccount accessors
    public BillingAccountLink getBillingAccount() { return _billingAccount.get(); }
    public void setBillingAccount(BillingAccountLink val) { _billingAccount.set(val); }


}
