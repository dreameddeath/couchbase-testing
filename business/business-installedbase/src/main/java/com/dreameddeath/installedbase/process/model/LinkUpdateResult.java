package com.dreameddeath.installedbase.process.model;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.installedbase.model.common.InstalledItemLink;

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
     *  status : status update result
     */
    @DocumentProperty("status")
    private Property<StatusUpdateResult> status = new StandardProperty<>(LinkUpdateResult.this);

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
    public StatusUpdateResult getStatus() { return status.get(); }
    /**
     * Setter of status
     * @param val the new content
     */
    public void setStatus(StatusUpdateResult val) { status.set(val); }

}
