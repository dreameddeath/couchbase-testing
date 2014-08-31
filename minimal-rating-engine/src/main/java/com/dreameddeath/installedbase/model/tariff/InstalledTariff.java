package com.dreameddeath.installedbase.model.tariff;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ArrayListProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.installedbase.model.common.InstalledItem;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 10/08/2014.
 */
public class InstalledTariff extends InstalledItem<InstalledTariffRevision> {
    /**
     *  discounts : list of discounts attached to the tariff
     */
    @DocumentProperty("discounts")
    private ListProperty<InstalledDiscount> _discounts = new ArrayListProperty<InstalledDiscount>(InstalledTariff.this);

    // Discounts Accessors
    public List<InstalledDiscount> getDiscounts() { return _discounts.get(); }
    public void setDiscounts(Collection<InstalledDiscount> vals) { _discounts.set(vals); }
    public boolean addDiscounts(InstalledDiscount val){ return _discounts.add(val); }
    public boolean removeDiscounts(InstalledDiscount val){ return _discounts.remove(val); }

}
