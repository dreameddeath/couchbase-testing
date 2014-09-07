package com.dreameddeath.billing.model.order;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 01/09/2014.
 */
public class BillingOrderItemFee extends BillingOrderItem {
    /**
     *  tariffId : The tariff id being billed
     */
    @DocumentProperty("tariffId")
    private Property<String> _tariffId = new StandardProperty<String>(BillingOrderItemFee.this);
    /**
     *  discountIds : The list of applicable billing item ids
     */
    @DocumentProperty("discountIds")
    private ListProperty<Long> _discountIds = new ArrayListProperty<Long>(BillingOrderItemFee.this);

    // tariffId accessors
    public String getTariffId() { return _tariffId.get(); }
    public void setTariffId(String val) { _tariffId.set(val); }
    // DiscountIds Accessors
    public List<Long> getDiscountIds() { return _discountIds.get(); }
    public void setDiscountIds(Collection<Long> vals) { _discountIds.set(vals); }
    public boolean addDiscountIds(Long val){ return _discountIds.add(val); }
    public boolean removeDiscountIds(Long val){ return _discountIds.remove(val); }


}
