package com.dreameddeath.installedbase.process.model.v1;

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.dreameddeath.installedbase.model.v1.common.InstalledStatus;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 22/03/2016.
 */
@DocumentEntity
public class StatusUpdateResult extends VersionedDocumentElement{
    /**
     *  action : The action on the status
     */
    @DocumentProperty("action") @NotNull
    private Property<Action> action = new ImmutableProperty<>(StatusUpdateResult.this);
    /**
     *  code : the status code
     */
    @DocumentProperty("code") @NotNull
    private Property<InstalledStatus.Code> code = new ImmutableProperty<>(StatusUpdateResult.this);
    /**
     *  startDate : the start date of the item (can never change)
     */
    @DocumentProperty("startDate") @NotNull
    private Property<DateTime> startDate = new ImmutableProperty<>(StatusUpdateResult.this);
    /**
     *  endDate : The new end date
     */
    @DocumentProperty("endDate") @NotNull
    private Property<DateTime> endDate = new StandardProperty<>(StatusUpdateResult.this);
    /**
     *  oldEndDate : The old end date (if not added)
     */
    @DocumentProperty("oldEndDate")
    private Property<DateTime> oldEndDate = new ImmutableProperty<>(StatusUpdateResult.this);


    /**
     * Getter of action
     * @return the content
     */
    public Action getAction() { return action.get(); }
    /**
     * Setter of action
     * @param val the new content
     */
    public void setAction(Action val) { action.set(val); }
    /**
     * Getter of code
     * @return the content
     */
    public InstalledStatus.Code getCode() { return code.get(); }
    /**
     * Setter of code
     * @param val the new content
     */
    public void setCode(InstalledStatus.Code val) { code.set(val); }
    /**
     * Getter of startDate
     * @return the value of startDate
     */
    public DateTime getStartDate() { return startDate.get(); }
    /**
     * Setter of startDate
     * @param val the new value for startDate
     */
    public void setStartDate(DateTime val) { startDate.set(val); }
    /**
     * Getter of endDate
     * @return the value of endDate
     */
    public DateTime getEndDate() { return endDate.get(); }
    /**
     * Setter of endDate
     * @param val the new value of endDate
     */
    public void setEndDate(DateTime val) { endDate.set(val); }
    /**
     * Getter of oldEndDate
     * @return the value of oldEndDate
     */
    public DateTime getOldEndDate() { return oldEndDate.get(); }
    /**
     * Setter of oldEndDate
     * @param val the new value for oldEndDate
     */
    public void setOldEndDate(DateTime val) { oldEndDate.set(val); }


    public enum Action{
        NEW,
        MODIFIED,
        DELETED
    }
}
