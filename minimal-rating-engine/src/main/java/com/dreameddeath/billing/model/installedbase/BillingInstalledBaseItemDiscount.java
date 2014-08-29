package com.dreameddeath.billing.model.installedbase;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.StandardProperty;

/**
 * Created by ceaj8230 on 12/08/2014.
 */
public class BillingInstalledBaseItemDiscount extends BillingInstalledBaseItem {
    /**
     *  discountId : The discount being billed
     */
    @DocumentProperty("discountId")
    private Property<String> _discountId = new StandardProperty<String>(BillingInstalledBaseItemDiscount.this);

    // discountId accessors
    public String getDiscountId() { return _discountId.get(); }
    public void setDiscountId(String val) { _discountId.set(val); }

}
