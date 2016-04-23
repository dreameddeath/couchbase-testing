package com.dreameddeath.installedbase.process.model;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.installedbase.model.common.InstalledItemLink;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 22/03/2016.
 */
public class LinkUpdateResult extends CouchbaseDocumentElement {
    /**
     *  targetId : target of the link
     */
    @DocumentProperty("targetId")
    private Property<String> targetId = new ImmutableProperty<>(LinkUpdateResult.this);
    /**
     *  direction : direction of the link
     */
    @DocumentProperty("direction")
    private Property<InstalledItemLink.Direction> direction = new ImmutableProperty<>(LinkUpdateResult.this);
    /**
     *  type : link type
     */
    @DocumentProperty("type")
    private Property<InstalledItemLink.Type> type = new ImmutableProperty<>(LinkUpdateResult.this);
    /**
     *  status : List of updates on status
     */
    @DocumentProperty("status")
    private ListProperty<StatusUpdateResult> status = new ArrayListProperty<>(LinkUpdateResult.this);

    /**
     * Getter of targetId
     * @return the content
     */
    public String getTargetId() { return targetId.get(); }
    /**
     * Setter of targetId
     * @param val the new content
     */
    public void setTargetId(String val) { targetId.set(val); }

    /**
     * Getter of direction
     * @return the content
     */
    public InstalledItemLink.Direction getDirection() { return direction.get(); }
    /**
     * Setter of direction
     * @param val the new content
     */
    public void setDirection(InstalledItemLink.Direction val) { direction.set(val); }

    /**
     * Getter of type
     * @return the content
     */
    public InstalledItemLink.Type getType() { return type.get(); }
    /**
     * Setter of type
     * @param val the new content
     */
    public void setType(InstalledItemLink.Type val) { type.set(val); }
    /**
     * Getter of status
     * @return the content
     */
    public List<StatusUpdateResult> getStatus() { return status.get(); }
    /**
     * Setter of status
     * @param vals the new collection of values
     */
    public void setStatus(Collection<StatusUpdateResult> vals) { status.set(vals); }
    /**
     * Size of status
     * @return the curr size of the list
     */
    public int sizeOfStatus() { return status.size(); }
    /**
     * Add a new entry to the property status
     * @param val the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addStatus(StatusUpdateResult val){ return status.add(val); }
    /**
     * Add a new entry to the property status at the specified position
     * @param index the new entry to be added
     * @param val the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addStatus(int index,StatusUpdateResult val){ return status.add(val); }
    /**
     * Remove an entry to the property status
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeStatus(StatusUpdateResult val){ return status.remove(val); }
    /**
     * Remove an entry to the property status at the specified position
     * @param index the position of element to be removed
     * @return the entry removed if any
     */
    public StatusUpdateResult removeStatus(int index){ return status.remove(index); }


}
