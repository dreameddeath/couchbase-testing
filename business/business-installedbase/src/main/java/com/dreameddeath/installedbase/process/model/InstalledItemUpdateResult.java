package com.dreameddeath.installedbase.process.model;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 22/03/2016.
 */

public class InstalledItemUpdateResult extends IdentifiedItemUpdateResult{
    /**
     *  attributes : List of attributes updates if any
     */
    @DocumentProperty("attributes")
    private ListProperty<AttributeUpdateResult> attributes = new ArrayListProperty<>(InstalledItemUpdateResult.this);
    /**
     *  linkUpdates : list of link updates if any
     */
    @DocumentProperty("linkUpdates")
    private ListProperty<LinkUpdateResult> linkUpdates = new ArrayListProperty<>(InstalledItemUpdateResult.this);

    /**
     * Getter of attributes
     * @return the content
     */
    public List<AttributeUpdateResult> getAttributes() { return attributes.get(); }
    /**
     * Setter of attributes
     * @param vals the new collection of values
     */
    public void setAttributes(Collection<AttributeUpdateResult> vals) { attributes.set(vals); }
    /**
     * Add a new entry to the property attributes
     * @param val the new entry to be added
     */
    public boolean addAttributes(AttributeUpdateResult val){ return attributes.add(val); }
    /**
     * Remove an entry to the property attributes
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeAttributes(AttributeUpdateResult val){ return attributes.remove(val); }


    /**
     * Getter of linkUpdates
     * @return the content
     */
    public List<LinkUpdateResult> getLinkUpdates() { return linkUpdates.get(); }
    /**
     * Setter of linkUpdates
     * @param vals the new collection of values
     */
    public void setLinkUpdates(Collection<LinkUpdateResult> vals) { linkUpdates.set(vals); }
    /**
     * Add a new entry to the property linkUpdates
     * @param val the new entry to be added
     */
    public boolean addLinkUpdates(LinkUpdateResult val){ return linkUpdates.add(val); }
    /**
     * Remove an entry to the property linkUpdates
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeLinkUpdates(LinkUpdateResult val){ return linkUpdates.remove(val); }

}
