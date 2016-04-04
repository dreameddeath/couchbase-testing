package com.dreameddeath.installedbase.process.model;

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.installedbase.model.common.InstalledStatus;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 22/03/2016.
 */
public class StatusUpdateResult extends VersionedDocumentElement{
    /**
     *  oldStatus : the old status
     */
    @DocumentProperty("oldStatus")
    private Property<InstalledStatus.Code> oldStatus = new StandardProperty<>(StatusUpdateResult.this);
    /**
     *  newStatus : the new status
     */
    @DocumentProperty("newStatus")
    private Property<InstalledStatus.Code> newStatus = new StandardProperty<>(StatusUpdateResult.this);
    /**
     *  effectiveDate : effective update date if known (empty if planned update)
     */
    @DocumentProperty("effectiveDate")
    private Property<DateTime> effectiveDate = new StandardProperty<>(StatusUpdateResult.this);

    /**
     * Getter of oldStatus
     * @return the content
     */
    public InstalledStatus.Code getOldStatus() { return oldStatus.get(); }
    /**
     * Setter of oldStatus
     * @param val the new content
     */
    public void setOldStatus(InstalledStatus.Code val) { oldStatus.set(val); }

    /**
     * Getter of newStatus
     * @return the content
     */
    public InstalledStatus.Code getNewStatus() { return newStatus.get(); }
    /**
     * Setter of newStatus
     * @param val the new content
     */
    public void setNewStatus(InstalledStatus.Code val) { newStatus.set(val); }
    /**
     * Getter of effectiveDate
     * @return the content
     */
    public DateTime getEffectiveDate() { return effectiveDate.get(); }
    /**
     * Setter of effectiveDate
     * @param val the new content
     */
    public void setEffectiveDate(DateTime val) { effectiveDate.set(val); }


}
