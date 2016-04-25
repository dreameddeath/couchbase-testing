package com.dreameddeath.installedbase.process.model;

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.installedbase.model.common.InstalledValue;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 08/04/2016.
 */
public class ValueUpdateResult extends VersionedDocumentElement {
    /**
     *  value : the value updated
     */
    @DocumentProperty("value")
    private Property<String> value = new StandardProperty<>(ValueUpdateResult.this);
    /**
     *  action : action on the value
     */
    @DocumentProperty("action")
    private Property<Action> action = new StandardProperty<>(ValueUpdateResult.this);
    /**
     *  start : the start date
     */
    @DocumentProperty("start")
    private Property<DateTime> start = new StandardProperty<>(ValueUpdateResult.this);
    /**
     *  oldEnd : the previous end date
     */
    @DocumentProperty("oldEnd")
    private Property<DateTime> oldEnd = new StandardProperty<>(ValueUpdateResult.this);
    /**
     *  end : the new end date
     */
    @DocumentProperty("end")
    private Property<DateTime> end = new StandardProperty<>(ValueUpdateResult.this);


    public ValueUpdateResult() {
    }

    public ValueUpdateResult(InstalledValue value,Action action){
        this.setValue(value.getValue());
        this.setStart(value.getStartDate());
        if(action.equals(Action.ADD)){
            this.setEnd(value.getEndDate());
        }
        else{
            this.setOldEnd(value.getStartDate());
        }
        this.setAction(action);
    }

    /**
     * Getter of value
     * @return the content
     */
    public String getValue() { return value.get(); }
    /**
     * Setter of value
     * @param val the new content
     */
    public void setValue(String val) { value.set(val); }

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
     * Getter of start
     * @return the content
     */
    public DateTime getStart() { return start.get(); }
    /**
     * Setter of start
     * @param val the new content
     */
    public void setStart(DateTime val) { start.set(val); }
    /**
     * Getter of oldEnd
     * @return the content
     */
    public DateTime getOldEnd() { return oldEnd.get(); }
    /**
     * Setter of oldEnd
     * @param val the new content
     */
    public void setOldEnd(DateTime val) { oldEnd.set(val); }
    /**
     * Getter of end
     * @return the content
     */
    public DateTime getEnd() { return end.get(); }
    /**
     * Setter of end
     * @param val the new content
     */
    public void setEnd(DateTime val) { end.set(val); }


    public enum Action{
        ADD,
        REMOVE,
        MODIFY_DATES
    }
}
