/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.installedbase.model.v1.common;

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
public abstract class InstalledItemRevision extends VersionedDocumentElement {
    /**
     *  orderId : Id of the order asking for a modification
     */
    @DocumentProperty("orderId")
    private Property<String> orderId = new StandardProperty<>(InstalledItemRevision.this);
    /**
     *  orderItemId : the id of the order item requesting a modification
     */
    @DocumentProperty("orderItemId")
    private Property<String> orderItemId = new StandardProperty<>(InstalledItemRevision.this);
    /**
     *  status : The wished status linked to the revision
     */
    @DocumentProperty("status")
    private Property<InstalledStatus.Code> status = new StandardProperty<>(InstalledItemRevision.this);
    /**
     *  revState : the current revision state
     */
    @DocumentProperty("revState")
    private Property<RevState> revState = new StandardProperty<>(InstalledItemRevision.this);
    /**
     *  effectiveDate : The effective date of the revision if future
     */
    @DocumentProperty("effectiveDate")
    private Property<DateTime> effectiveDate = new StandardProperty<>(InstalledItemRevision.this);
    /**
     *  rank : executed rank when applied
     */
    @DocumentProperty("rank")
    private Property<Integer> rank = new StandardProperty<>(InstalledItemRevision.this);
    /**
     *  runDate : date time when it has been runned
     */
    @DocumentProperty("runDate")
    private Property<DateTime> runDate = new StandardProperty<>(InstalledItemRevision.this);

    // orderId accessors
    public String getOrderId() { return orderId.get(); }
    public void setOrderId(String val) { orderId.set(val); }

    // orderItemId accessors
    public String getOrderItemId() { return orderItemId.get(); }
    public void setOrderItemId(String val) { orderItemId.set(val); }

    // status accessors
    public InstalledStatus.Code getStatus() { return status.get(); }
    public void setStatus(InstalledStatus.Code val) { status.set(val); }

    /**
     * Getter of revState
     * @return the content
     */
    public RevState getRevState() { return revState.get(); }
    /**
     * Setter of revState
     * @param val the new content
     */
    public void setRevState(RevState val) { revState.set(val); }

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
    /**
     * Getter of rank
     * @return the content
     */
    public Integer getRank() { return rank.get(); }
    /**
     * Setter of rank
     * @param val the new content
     */
    public void setRank(Integer val) { rank.set(val); }
    /**
     * Getter of runDate
     * @return the content
     */
    public DateTime getRunDate() { return runDate.get(); }
    /**
     * Setter of runDate
     * @param val the new content
     */
    public void setRunDate(DateTime val) { runDate.set(val); }


    /**
     * comparator of revisions
     * @param revision the target revision to compare with
     */
    public boolean isSame(InstalledItemRevision revision){
        return effectiveDate.equals(revision.effectiveDate)
                && status.equals(status)
                && revState.equals(revState)
                && orderId.equals(orderId)
                && orderId.equals(orderItemId)
                ;
    }

    public enum RevState {
        REQUESTED, // the revision is requested but not "planned" (order delivery not started)
        CANCELLED, //revision requested at one time but not planned
        PLANNED, //the revision is planned
        DONE
    }
}
