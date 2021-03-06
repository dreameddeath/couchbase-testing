package com.dreameddeath.installedbase.process.model.v1;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 22/03/2016.
 */
@DocumentEntity
public class InstalledItemUpdateResult extends IdentifiedItemUpdateResult{
    /**
     *  attributes : List of attributes updates if any
     */
    @DocumentProperty("attributes")
    private ListProperty<AttributeUpdateResult> attributes = new ArrayListProperty<>(InstalledItemUpdateResult.this);
    /**
     *  linkUpdates : list of link updates if any
     */
    @DocumentProperty("links")
    private ListProperty<LinkUpdateResult> links = new ArrayListProperty<>(InstalledItemUpdateResult.this);

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
    public List<LinkUpdateResult> getLinks() { return links.get(); }
    /**
     * Setter of linkUpdates
     * @param vals the new collection of values
     */
    public void setLinks(Collection<LinkUpdateResult> vals) { links.set(vals); }
    /**
     * Add a new entry to the property linkUpdates
     * @param val the new entry to be added
     */
    public boolean addLink(LinkUpdateResult val){ return links.add(val); }
    /**
     * Remove an entry to the property linkUpdates
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeLink(LinkUpdateResult val){ return links.remove(val); }

}
