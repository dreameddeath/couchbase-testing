package com.dreameddeath.billing.model.installedbase;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 12/08/2014.
 */
public class BillingInstalledBaseItemFee extends BillingInstalledBaseItem {
    /**
     *  tariffId : Instance of the tariff of the installed base corresponding to the billingItem
     */
    @DocumentProperty("tariffId")
    private Property<String> _tariffId = new StandardProperty<String>(BillingInstalledBaseItemFee.this);
    /**
     *  discountsIds : List of discount items applicable
     */
    @DocumentProperty("discountsIds")
    private ListProperty<Long> _discountsIds = new ArrayListProperty<Long>(BillingInstalledBaseItemFee.this);


    // tariffId accessors
    public String getTariffId() { return _tariffId.get(); }
    public void setTariffId(String val) { _tariffId.set(val); }

    // DiscountsIds Accessors
    public List<Long> getDiscountsIds() { return _discountsIds.get(); }
    public void setDiscountsIds(Collection<Long> vals) { _discountsIds.set(vals); }
    public boolean addDiscountsIds(Long val){ return _discountsIds.add(val); }
    public boolean removeDiscountsIds(Long val){ return _discountsIds.remove(val); }
}
