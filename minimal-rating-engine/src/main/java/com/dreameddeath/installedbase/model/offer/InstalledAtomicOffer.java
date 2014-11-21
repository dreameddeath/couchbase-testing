package com.dreameddeath.installedbase.model.offer;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.installedbase.model.productservice.InstalledProductService;

/**
 * Created by ceaj8230 on 21/10/2014.
 */
public class InstalledAtomicOffer extends InstalledOffer {
    /**
     *  product : Installed Product
     */
    @DocumentProperty("product")
    private Property<InstalledProductService> _product = new StandardProperty<InstalledProductService>(InstalledAtomicOffer.this);

    // product accessors
    public InstalledProductService getProduct() { return _product.get(); }
    public void setProduct(InstalledProductService val) { _product.set(val); }
}
