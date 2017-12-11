/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.billing.installedbase.service.model.v1;

import com.dreameddeath.billing.installedbase.model.v1.BillingInstalledBaseItemStatus;
import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 25/09/2017.
 */
public class CreateUpdateBillingInstalledBaseItemStatusUpdateResult extends VersionedDocumentElement{
    /**
     * action : The action on the status
     */
    @DocumentProperty("action")
    private Property<CreateUpdateBillingInstalledBaseAction> action = new ImmutableProperty<>(CreateUpdateBillingInstalledBaseItemStatusUpdateResult.this);
    /**
     * status : the billing item status
     */
    @DocumentProperty("status")
    private Property<BillingInstalledBaseItemStatus.Status> status = new ImmutableProperty<>(CreateUpdateBillingInstalledBaseItemStatusUpdateResult.this);
    /**
     * startDate : the status startDate
     */
    @DocumentProperty("startDate")
    private Property<DateTime> startDate = new ImmutableProperty<>(CreateUpdateBillingInstalledBaseItemStatusUpdateResult.this);
    /**
     * oldStartDate : The previous startDate
     */
    @DocumentProperty("oldStartDate")
    private Property<DateTime> oldStartDate = new ImmutableProperty<>(CreateUpdateBillingInstalledBaseItemStatusUpdateResult.this);
    /**
     * endDate : the end date of the status
     */
    @DocumentProperty("endDate")
    private Property<DateTime> endDate = new ImmutableProperty<>(CreateUpdateBillingInstalledBaseItemStatusUpdateResult.this);
    /**
     * oldEndDate : The old end date
     */
    @DocumentProperty("oldEndDate")
    private Property<DateTime> oldEndDate = new ImmutableProperty<>(CreateUpdateBillingInstalledBaseItemStatusUpdateResult.this);


    /**
     * Getter of the attribute {@link #action}
     * return the currentValue of {@link #action}
     */
    public CreateUpdateBillingInstalledBaseAction getAction(){
        return this.action.get();
    }

    /**
     * Setter of the attribute {@link #action}
     * @param newValue the newValue of {@link #action}
     */
    public void setAction(CreateUpdateBillingInstalledBaseAction newValue){
        this.action.set(newValue);
    }
    /**
     * Getter of the attribute {@link #status}
     * return the currentValue of {@link #status}
     */
    public BillingInstalledBaseItemStatus.Status getStatus(){
        return this.status.get();
    }

    /**
     * Setter of the attribute {@link #status}
     * @param newValue the newValue of {@link #status}
     */
    public void setStatus(BillingInstalledBaseItemStatus.Status newValue){
        this.status.set(newValue);
    }
    /**
     * Getter of the attribute {@link #startDate}
     * return the currentValue of {@link #startDate}
     */
    public DateTime getStartDate(){
        return this.startDate.get();
    }

    /**
     * Setter of the attribute {@link #startDate}
     * @param newValue the newValue of {@link #startDate}
     */
    public void setStartDate(DateTime newValue){
        this.startDate.set(newValue);
    }
    /**
     * Getter of the attribute {@link #oldStartDate}
     * return the currentValue of {@link #oldStartDate}
     */
    public DateTime getOldStartDate(){
        return this.oldStartDate.get();
    }

    /**
     * Setter of the attribute {@link #oldStartDate}
     * @param newValue the newValue of {@link #oldStartDate}
     */
    public void setOldStartDate(DateTime newValue){
        this.oldStartDate.set(newValue);
    }
    /**
     * Getter of the attribute {@link #endDate}
     * return the currentValue of {@link #endDate}
     */
    public DateTime getEndDate(){
        return this.endDate.get();
    }
    /**
     * Setter of the attribute {@link #endDate}
     * @param newValue the newValue of {@link #endDate}
     */
    public void setEndDate(DateTime newValue){
        this.endDate.set(newValue);
    }
    /**
     * Getter of the attribute {@link #oldEndDate}
     * return the currentValue of {@link #oldEndDate}
     */
    public DateTime getOldEndDate(){
        return this.oldEndDate.get();
    }
    /**
     * Setter of the attribute {@link #oldEndDate}
     * @param newValue the newValue of {@link #oldEndDate}
     */
    public void setOldEndDate(DateTime newValue){
        this.oldEndDate.set(newValue);
    }





}
