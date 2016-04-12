package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 25/03/2016.
 */
public class InstalledValueRevision extends CouchbaseDocumentElement {
    /**
     *  value : the value itself
     */
    @DocumentProperty("value")
    private Property<String> value = new StandardProperty<>(InstalledValueRevision.this);
    /**
     *  startDate : The value startDate
     */
    @DocumentProperty("startDate")
    private Property<DateTime> startDate = new StandardProperty<>(InstalledValueRevision.this);
    /**
     *  endDate : The value endDate
     */
    @DocumentProperty("endDate")
    private Property<DateTime> endDate = new StandardProperty<>(InstalledValueRevision.this);
    /**
     *  keyType : Type of public key if applicable
     */
    @DocumentProperty("keyType")
    private Property<String> keyType = new StandardProperty<>(InstalledValueRevision.this);
    /**
     *  action : The revision action if precised
     */
    @DocumentProperty("action")
    private Property<Action> action = new StandardProperty<>(InstalledValueRevision.this);

    // value accessors
    public String getValue() { return value.get(); }
    public void setValue(String val) { value.set(val); }

    // startDate accessors
    public DateTime getStartDate() { return startDate.get(); }
    public void setStartDate(DateTime val) { startDate.set(val); }

    // endDate accessors
    public DateTime getEndDate() { return endDate.get(); }
    public void setEndDate(DateTime val) { endDate.set(val); }

    // keyType accessors
    public String getKeyType() { return keyType.get(); }
    public void setKeyType(String val) { keyType.set(val); }

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

    public enum Action{
        ADD,
        REMOVE,
        MODIFY
    }

    public boolean isSame(InstalledValueRevision target){
        return value.equals(target.value)
                && startDate.equals(target.startDate)
                && endDate.equals(target.endDate)
                && keyType.equals(target.keyType)
                && action.equals(target.action);
    }

    public static boolean isSameRevisionList(List<InstalledValueRevision> src,List<InstalledValueRevision> target){
        int nbTargetValuesMatched=0;
        for(InstalledValueRevision srcValueRev:src){
            boolean found=false;
            for(InstalledValueRevision targetValueRev:target){
                if(!srcValueRev.isSame(targetValueRev)) {
                    return false;
                }
                else{
                    ++nbTargetValuesMatched;
                    found=true;
                    break;
                }
            }
            if(!found){
                return  false;
            }
        }

        return nbTargetValuesMatched!=target.size();
    }
}
