package com.dreameddeath.billing.model.installedbase;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 12/08/2014.
 */
public class BillingInstalledBaseRatingContext extends BillingInstalledBaseItem {
    /**
     *  productIds : List of product ids linked to this context
     */
    @DocumentProperty("productIds")
    private ListProperty<String> _productIds = new ArrayListProperty<String>(BillingInstalledBaseRatingContext.this);

    // ProductIds Accessors
    public List<String> getProductIds() { return _productIds.get(); }
    public void setProductIds(Collection<String> vals) { _productIds.set(vals); }
    public boolean addProductIds(String val){ return _productIds.add(val); }
    public boolean removeProductIds(String val){ return _productIds.remove(val); }

}
