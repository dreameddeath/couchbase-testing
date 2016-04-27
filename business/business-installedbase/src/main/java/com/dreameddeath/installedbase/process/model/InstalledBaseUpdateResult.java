package com.dreameddeath.installedbase.process.model;

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 22/03/2016.
 */
@DocumentEntity(domain = "installedbase")
public class InstalledBaseUpdateResult extends VersionedDocumentElement {
    /**
     *  contract : contract updates
     */
    @DocumentProperty("contract")
    private Property<InstalledItemUpdateResult> contract = new StandardProperty<>(InstalledBaseUpdateResult.this);
    /**
     *  offers : List of updates of offers
     */
    @DocumentProperty("offers")
    private ListProperty<InstalledItemUpdateResult> offersUpdates = new ArrayListProperty<>(InstalledBaseUpdateResult.this);
    /**
     *  products : list of updates on products
     */
    @DocumentProperty("products")
    private ListProperty<InstalledItemUpdateResult> products = new ArrayListProperty<>(InstalledBaseUpdateResult.this);
    /**
     *  tariffs : list of tariff updates if any
     */
    @DocumentProperty("tariffs")
    private ListProperty<TariffUpdateResult> tariffsUpdates = new ArrayListProperty<>(InstalledBaseUpdateResult.this);
    /**
     *  discounts : Updates upon discounts
     */
    @DocumentProperty("discounts")
    private ListProperty<DiscountUpdateResult> discountsUpdates = new ArrayListProperty<>(InstalledBaseUpdateResult.this);
    /**
     *  revisions : the updates on revisions
     */
    @DocumentProperty("revisions")
    private ListProperty<RevisionUpdateResult> revisions = new ArrayListProperty<>(InstalledBaseUpdateResult.this);

    /**
     * Getter of contract
     * @return the content
     */
    public InstalledItemUpdateResult getContract() { return contract.get(); }
    /**
     * Setter of contract
     * @param val the new content
     */
    public void setContract(InstalledItemUpdateResult val) { contract.set(val); }
    /**
     * Getter of offers
     * @return the content
     */
    public List<InstalledItemUpdateResult> getOffersUpdates() { return offersUpdates.get(); }
    /**
     * Setter of offers
     * @param vals the new collection of values
     */
    public void setOffersUpdates(Collection<InstalledItemUpdateResult> vals) { offersUpdates.set(vals); }
    /**
     * Add a new entry to the property offers
     * @param val the new entry to be added
     */
    public boolean addOfferUpdate(InstalledItemUpdateResult val){ return offersUpdates.add(val); }
    /**
     * Remove an entry to the property offers
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeOffersUpdate(InstalledItemUpdateResult val){ return offersUpdates.remove(val); }

    /**
     * Getter of products
     * @return the content
     */
    public List<InstalledItemUpdateResult> getProducts() { return products.get(); }
    /**
     * Setter of products
     * @param vals the new collection of values
     */
    public void setProducts(Collection<InstalledItemUpdateResult> vals) { products.set(vals); }
    /**
     * Add a new entry to the property products
     * @param val the new entry to be added
     */
    public boolean addProducts(InstalledItemUpdateResult val){ return products.add(val); }
    /**
     * Remove an entry to the property products
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeProducts(InstalledItemUpdateResult val){ return products.remove(val); }



    /**
     * Getter of tariffs
     * @return the content
     */
    public List<TariffUpdateResult> getTariffsUpdates() { return tariffsUpdates.get(); }
    /**
     * Setter of tariffs
     * @param vals the new collection of values
     */
    public void setTariffsUpdates(Collection<TariffUpdateResult> vals) { tariffsUpdates.set(vals); }
    /**
     * Add a new entry to the property tariffs
     * @param val the new entry to be added
     */
    public boolean addTariffs(TariffUpdateResult val){ return tariffsUpdates.add(val); }
    /**
     * Remove an entry to the property tariffs
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeTariffs(TariffUpdateResult val){ return tariffsUpdates.remove(val); }

    /**
     * Getter of discounts
     * @return the content
     */
    public List<DiscountUpdateResult> getDiscountsUpdates() { return discountsUpdates.get(); }
    /**
     * Setter of discounts
     * @param vals the new collection of values
     */
    public void setDiscountsUpdates(Collection<DiscountUpdateResult> vals) { discountsUpdates.set(vals); }
    /**
     * Add a new entry to the property discounts
     * @param val the new entry to be added
     */
    public boolean addDiscounts(DiscountUpdateResult val){ return discountsUpdates.add(val); }
    /**
     * Remove an entry to the property discounts
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeDiscounts(DiscountUpdateResult val){ return discountsUpdates.remove(val); }

    /**
     * Getter of revisions
     * @return the content
     */
    public List<RevisionUpdateResult> getRevisions() { return revisions.get(); }
    /**
     * Setter of revisions
     * @param vals the new collection of values
     */
    public void setRevisions(Collection<RevisionUpdateResult> vals) { revisions.set(vals); }
    /**
     * Add a new entry to the property revisions
     * @param val the new entry to be added
     */
    public boolean addRevisions(RevisionUpdateResult val){ return revisions.add(val); }
    /**
     * Remove an entry to the property revisions
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeRevisions(RevisionUpdateResult val){ return revisions.remove(val); }
}
