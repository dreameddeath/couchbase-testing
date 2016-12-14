package com.dreameddeath.installedbase.process.model.v1;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.installedbase.model.v1.common.InstalledItemLink;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 22/03/2016.
 */
@DocumentEntity
public class LinkUpdateResult extends CouchbaseDocumentElement {
    /**
     *  targetId : target of the link
     */
    @DocumentProperty("targetId")
    private Property<String> targetId = new ImmutableProperty<>(LinkUpdateResult.this);
    /**
     *  isReverse : indicate if it's the reverse link
     */
    @DocumentProperty(value = "isReverse",getter = "isReverse",setter = "isReverse")
    private Property<Boolean> isReverse = new ImmutableProperty<>(LinkUpdateResult.this);
    /**
     *  type : link type
     */
    @DocumentProperty("type")
    private Property<InstalledItemLink.Type> type = new ImmutableProperty<>(LinkUpdateResult.this);
    /**
     *  status : List of updates on status
     */
    @DocumentProperty("statuses")
    private ListProperty<StatusUpdateResult> statuses = new ArrayListProperty<>(LinkUpdateResult.this);

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
     * Getter of isReverse
     * @return the value of isReverse
     */
    public Boolean isReverse() { return isReverse.get(); }
    /**
     * Setter of isReverse
     * @param val the new value for isReverse
     */
    public void isReverse(Boolean val) { isReverse.set(val); }

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
    public List<StatusUpdateResult> getStatuses() { return statuses.get(); }
    /**
     * Setter of status
     * @param vals the new collection of values
     */
    public void setStatuses(Collection<StatusUpdateResult> vals) { statuses.set(vals); }
    /**
     * Add a new entry to the property status
     * @param val the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addStatus(StatusUpdateResult val){ return statuses.add(val); }
    /**
     * Add a new entry to the property status at the specified position
     * @param index the new entry to be added
     * @param val the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addStatus(int index,StatusUpdateResult val){ return statuses.add(val); }
    /**
     * Remove an entry to the property status
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeStatus(StatusUpdateResult val){ return statuses.remove(val); }
    /**
     * Remove an entry to the property status at the specified position
     * @param index the position of element to be removed
     * @return the entry removed if any
     */
    public StatusUpdateResult removeStatus(int index){ return statuses.remove(index); }


}
