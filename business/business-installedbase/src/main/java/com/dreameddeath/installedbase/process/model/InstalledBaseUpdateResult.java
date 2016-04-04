package com.dreameddeath.installedbase.process.model;

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 22/03/2016.
 */
@DocumentDef(domain = "installedbase")
public class InstalledBaseUpdateResult extends VersionedDocumentElement {
    /**
     *  itemsUpdates : List of updates of items
     */
    @DocumentProperty("itemsUpdates")
    private ListProperty<InstalledItemUpdateResult> itemsUpdates = new ArrayListProperty<>(InstalledBaseUpdateResult.this);

    /**
     * Getter of itemsUpdates
     * @return the content
     */
    public List<InstalledItemUpdateResult> getItemsUpdates() { return itemsUpdates.get(); }
    /**
     * Setter of itemsUpdates
     * @param vals the new collection of values
     */
    public void setItemsUpdates(Collection<InstalledItemUpdateResult> vals) { itemsUpdates.set(vals); }
    /**
     * Add a new entry to the property itemsUpdates
     * @param val the new entry to be added
     */
    public boolean addItemsUpdates(InstalledItemUpdateResult val){ return itemsUpdates.add(val); }
    /**
     * Remove an entry to the property itemsUpdates
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeItemsUpdates(InstalledItemUpdateResult val){ return itemsUpdates.remove(val); }
}
