/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.billing.installedbase.model.v1;

import com.dreameddeath.billing.model.v1.account.BillingAccount;
import com.dreameddeath.billing.model.v1.account.BillingAccountContributor;
import com.dreameddeath.billing.model.v1.account.BillingAccountLink;
import com.dreameddeath.core.business.dao.BusinessCouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.annotation.dao.Counter;
import com.dreameddeath.core.dao.annotation.dao.DaoEntity;
import com.dreameddeath.core.dao.annotation.dao.ParentEntity;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.NumericProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardLongProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.dreameddeath.core.validation.annotation.Unique;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 12/08/2014.
 */
@DocumentEntity(domain = "billing",name="base",version = "1.0.0")
@DaoEntity(baseDao= BusinessCouchbaseDocumentWithKeyPatternDao.class,dbPath = "base/",idPattern = "\\d{5}",idFormat = "%05d")
@ParentEntity(c= BillingAccount.class,keyPath = "ba.key")
@Counter(name = "cnt",dbName = "cnt",isKeyGen = true)
public class BillingInstalledBase extends BillingAccountContributor {
    /**
     *  installedBaseKey : The installed base parent key
     */
    @DocumentProperty("installedBaseKey") @Unique(nameSpace = "billingInstalledBaseOrigKey")
    private Property<String> installedBaseKey = new ImmutableProperty<>(BillingInstalledBase.this);
    /**
     *  installedBaseRevision : The last processed installed base revision
     */
    private Property<Long> installedBaseRevision = new StandardProperty<>(BillingInstalledBase.this);
    /**
     *  ba : Link toward the parent billing account
     */
    @DocumentProperty(value = "ba",getter = "getBaLink",setter = "setBaLink") @NotNull
    private Property<BillingAccountLink> ba = new StandardProperty<>(BillingInstalledBase.this);
    /**
     *  billingItems : List the corresponding billing Items
     */
    @DocumentProperty("billingItems")
    private ListProperty<BillingInstalledBaseItem> billingInstalledBaseItems = new ArrayListProperty<BillingInstalledBaseItem>(BillingInstalledBase.this);
    /**
     *  itemIdNextKey : Next key for item ids
     */
    @DocumentProperty("itemIdNextKey")
    private NumericProperty<Long> itemIdNextKey = new StandardLongProperty(BillingInstalledBase.this);

    // BillingItems Accessors
    public List<BillingInstalledBaseItem> getBillingItems() { return billingInstalledBaseItems.get(); }
    public void setBillingItems(Collection<BillingInstalledBaseItem> vals) { billingInstalledBaseItems.set(vals); }
    public boolean addBillingItems(BillingInstalledBaseItem val){
        buildItemId(val);
        return billingInstalledBaseItems.add(val);
    }
    public boolean removeBillingItems(BillingInstalledBaseItem val){ return billingInstalledBaseItems.remove(val); }
    public BillingInstalledBaseItem getItemById(Long id){
        for(BillingInstalledBaseItem item:billingInstalledBaseItems){
            if(item.getId().equals(id)){return item;}
        }
        return null;
    }
    public <T extends BillingInstalledBaseItem> T getItemById(Long id,Class<T> clazz){
        return (T) getItemById(id);
    }

    /**
     * Getter of installedBaseKey
     * @return the value of installedBaseKey
     */
    public String getInstalledBaseKey() { return installedBaseKey.get(); }
    /**
     * Setter of installedBaseKey
     * @param val the new value for installedBaseKey
     */
    public void setInstalledBaseKey(String val) { installedBaseKey.set(val); }

    public Long getInstalledBaseRevision() {
        return installedBaseRevision.get();
    }

    public void setInstalledBaseRevision(Long installedBaseRevision) {
        this.installedBaseRevision.set(installedBaseRevision);
    }

    // ba accessors
    public BillingAccountLink getBaLink() { return ba.get(); }
    public void setBaLink(BillingAccountLink val) { ba.set(val); }
    // itemIdNextKey accessors
    public Long getItemIdNextKey() { return itemIdNextKey.get(); }
    public void setItemIdNextKey(Long val) { itemIdNextKey.set(val); }
    public void buildItemId(BillingInstalledBaseItem item) { item.setId(itemIdNextKey.inc(1).get());}

}
