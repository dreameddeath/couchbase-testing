/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.billing.model.v1.order;


import com.dreameddeath.billing.model.v1.account.BillingAccount;
import com.dreameddeath.billing.model.v1.account.BillingAccountLink;
import com.dreameddeath.core.business.dao.BusinessCouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.helper.annotation.dao.Counter;
import com.dreameddeath.core.helper.annotation.dao.DaoEntity;
import com.dreameddeath.core.helper.annotation.dao.ParentEntity;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.validation.annotation.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 01/09/2014.
 */
@DocumentEntity(name="order")
@DaoEntity(baseDao= BusinessCouchbaseDocumentWithKeyPatternDao.class,dbPath = "order/",idPattern = "\\d{5}",idFormat = "%05d")
@ParentEntity(c= BillingAccount.class,keyPath = "billingAccount.key",separator = "/")
@Counter(name = "cnt",dbName = "cnt",isKeyGen = true)
public class BillingOrder extends BusinessDocument {
    /**
     *  billingAccount : Link to the parent billing Account
     */
    @DocumentProperty("billingAccount") @NotNull
    private Property<BillingAccountLink> billingAccount = new StandardProperty<>(BillingOrder.this);
    /**
     *  items : List of order items
     */
    @DocumentProperty("items")
    private ListProperty<BillingOrderItem> items = new ArrayListProperty<>(BillingOrder.this);

    // Items Accessors
    public List<BillingOrderItem> getItems() { return items.get();  }
    public void setItems(Collection<BillingOrderItem> vals) { items.set(vals); }
    public boolean addItems(BillingOrderItem val){ return items.add(val); }
    public boolean removeItems(BillingOrderItem val){ return items.remove(val); }

    // billingAccount accessors
    public BillingAccountLink getBillingAccount() { return billingAccount.get(); }
    public void setBillingAccount(BillingAccountLink val) { billingAccount.set(val); }


}
