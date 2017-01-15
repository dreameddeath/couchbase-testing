/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.installedbase.process.model.v1;

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.installedbase.model.v1.common.InstalledValue;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 08/04/2016.
 */
@DocumentEntity
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
     *  startDocument : the startDocument date
     */
    @DocumentProperty("startDate")
    private Property<DateTime> startDate = new StandardProperty<>(ValueUpdateResult.this);
    /**
     *  oldEnd : the previous end date
     */
    @DocumentProperty("oldEndDate")
    private Property<DateTime> oldEndDate = new StandardProperty<>(ValueUpdateResult.this);
    /**
     *  end : the new end date
     */
    @DocumentProperty("endDate")
    private Property<DateTime> endDate = new StandardProperty<>(ValueUpdateResult.this);


    public ValueUpdateResult() {
    }

    public ValueUpdateResult(InstalledValue value,Action action){
        this.setValue(value.getValue());
        this.setStartDate(value.getStartDate());
        if(action.equals(Action.ADD)){
            this.setEndDate(value.getEndDate());
        }
        else{
            this.setOldEndDate(value.getStartDate());
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
    public DateTime getStartDate() { return startDate.get(); }
    /**
     * Setter of start
     * @param val the new content
     */
    public void setStartDate(DateTime val) { startDate.set(val); }
    /**
     * Getter of oldEnd
     * @return the content
     */
    public DateTime getOldEndDate() { return oldEndDate.get(); }
    /**
     * Setter of oldEnd
     * @param val the new content
     */
    public void setOldEndDate(DateTime val) { oldEndDate.set(val); }
    /**
     * Getter of end
     * @return the content
     */
    public DateTime getEndDate() { return endDate.get(); }
    /**
     * Setter of end
     * @param val the new content
     */
    public void setEndDate(DateTime val) { endDate.set(val); }


    public enum Action{
        ADD,
        REMOVE,
        MODIFY_DATES
    }
}
