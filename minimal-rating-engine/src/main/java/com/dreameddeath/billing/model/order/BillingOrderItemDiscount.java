package com.dreameddeath.billing.model.order;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.StandardProperty;

/**
 * Created by ceaj8230 on 01/09/2014.
 */
public class BillingOrderItemDiscount extends BillingOrderItem {
    /**
     *  discountId : The order discount being billed
     */
    @DocumentProperty("discountId")
    private Property<String> _discountId = new StandardProperty<String>(BillingOrderItemDiscount.this);

    // discountId accessors
    public String getDiscountId() { return _discountId.get(); }
    public void setDiscountId(String val) { _discountId.set(val); }
}
