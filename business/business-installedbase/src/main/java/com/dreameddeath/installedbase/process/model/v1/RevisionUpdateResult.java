package com.dreameddeath.installedbase.process.model.v1;

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.installedbase.model.v1.common.InstalledItemRevision;

/**
 * Created by Christophe Jeunesse on 01/04/2016.
 */
public class RevisionUpdateResult  extends VersionedDocumentElement {
    /**
     *  action : Type of action applied
     */
    @DocumentProperty("action")
    private Property<UpdateAction> action = new StandardProperty<>(RevisionUpdateResult.this);

    /**
     *  oldRevision : The old revision data if any
     */
    @DocumentProperty("oldRevision")
    private Property<InstalledItemRevision> oldRevision = new StandardProperty<>(RevisionUpdateResult.this);

    /**
     *  revision : The full revision data
     */
    @DocumentProperty("revision")
    private Property<InstalledItemRevision> revision = new StandardProperty<>(RevisionUpdateResult.this);

    /**
     * Getter of revision
     * @return the content
     */
    public InstalledItemRevision getRevision() { return revision.get(); }
    /**
     * Setter of revision
     * @param val the new content
     */
    public void setRevision(InstalledItemRevision val) { revision.set(val); }

    /**
     * Getter of oldRevision
     * @return the content
     */
    public InstalledItemRevision getOldRevision() { return oldRevision.get(); }
    /**
     * Setter of oldRevision
     * @param val the new content
     */
    public void setOldRevision(InstalledItemRevision val) { oldRevision.set(val); }
    /**
     * Getter of action
     * @return the content
     */
    public UpdateAction getAction() { return action.get(); }
    /**
     * Setter of action
     * @param val the new content
     */
    public void setAction(UpdateAction val) { action.set(val); }

    public enum UpdateAction{
        CREATED,
        REPLACED,
        APPLIED,
        CREATED_AND_APPLIED,
        UNCHANGED
    }
}
