package com.dreameddeath.billing.model.installedbase;

import com.dreameddeath.billing.model.account.BillingAccountLink;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.*;
import com.dreameddeath.installedbase.model.InstalledBaseLink;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 12/08/2014.
 */
public class BillingInstalledBase extends CouchbaseDocument {
    /**
     *  ba : Link toward the parent billing account
     */
    @DocumentProperty("ba")
    private Property<BillingAccountLink> _ba = new StandardProperty<BillingAccountLink>(BillingInstalledBase.this);
    /**
     *  installedBaseLink : Link toward the installed based being billed
     */
    @DocumentProperty("installedBaseLink")
    private Property<InstalledBaseLink> _installedBaseLink = new StandardProperty<InstalledBaseLink>(BillingInstalledBase.this);
    /**
     *  billingItems : List the corresponding billing Items
     */
    @DocumentProperty("billingItems")
    private ListProperty<BillingInstalledBaseItem> _billingInstalledBaseItems = new ArrayListProperty<BillingInstalledBaseItem>(BillingInstalledBase.this);
    /**
     *  itemIdNextKey : Next key for item ids
     */
    @DocumentProperty("itemIdNextKey")
    private NumericProperty<Long> _itemIdNextKey = new StandardLongProperty(BillingInstalledBase.this);


    // BillingItems Accessors
    public List<BillingInstalledBaseItem> getBillingItems() { return _billingInstalledBaseItems.get(); }
    public void setBillingItems(Collection<BillingInstalledBaseItem> vals) { _billingInstalledBaseItems.set(vals); }
    public boolean addBillingItems(BillingInstalledBaseItem val){
        buildItemId(val);
        return _billingInstalledBaseItems.add(val);
    }
    public boolean removeBillingItems(BillingInstalledBaseItem val){ return _billingInstalledBaseItems.remove(val); }
    public BillingInstalledBaseItem getItemById(Long id){
        for(BillingInstalledBaseItem item:_billingInstalledBaseItems){
            if(item.getId().equals(id)){return item;}
        }
        return null;
    }
    public <T extends BillingInstalledBaseItem> T getItemById(Long id,Class<T> clazz){
        return (T) getItemById(id);
    }

    // installedBaseLink accessors
    public InstalledBaseLink getInstalledBaseLink() { return _installedBaseLink.get(); }
    public void setInstalledBaseLink(InstalledBaseLink val) { _installedBaseLink.set(val); }

    // ba accessors
    public BillingAccountLink getBaLink() { return _ba.get(); }
    public void setBaLink(BillingAccountLink val) { _ba.set(val); }
    // itemIdNextKey accessors
    public Long getItemIdNextKey() { return _itemIdNextKey.get(); }
    public void setItemIdNextKey(Long val) { _itemIdNextKey.set(val); }
    public void buildItemId(BillingInstalledBaseItem item) { item.setId(_itemIdNextKey.inc(1).get());}

    public BillingInstalledBaseLink newLink(){return new BillingInstalledBaseLink(this);}
}
