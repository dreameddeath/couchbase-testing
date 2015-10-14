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

package com.dreameddeath.billing.model.cycle;


import com.dreameddeath.core.business.model.BusinessDocumentLink;
import com.dreameddeath.core.business.model.property.impl.SynchronizedLinkProperty;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import org.joda.time.DateTime;

public class BillingCycleLink extends BusinessDocumentLink<BillingCycle> {
    @DocumentProperty("startDate")
    private Property<DateTime> startDate=new SynchronizedLinkProperty<DateTime,BillingCycle>(BillingCycleLink.this){
        @Override
        protected  DateTime getRealValue(BillingCycle cycle){
            return cycle.getStartDate();
        }
    };
    @DocumentProperty("endDate")
    private Property<DateTime> endDate=new SynchronizedLinkProperty<DateTime,BillingCycle>(BillingCycleLink.this){
        @Override
        protected  DateTime getRealValue(BillingCycle cycle){
            return cycle.getEndDate();
        }
    };
    
    public DateTime getStartDate() { return startDate.get(); }
    public void setStartDate(DateTime startDate) { this.startDate.set(startDate); }
    
    public DateTime getEndDate() { return endDate.get(); }
    public void setEndDate(DateTime endDate) { this.endDate.set(endDate); }
    
    public BillingCycleLink(){}
    public BillingCycleLink(BillingCycle billCycle){ super(billCycle);}
    public BillingCycleLink(BillingCycleLink srcLink){
        super(srcLink);
        setStartDate(srcLink.getStartDate());
        setEndDate(srcLink.getEndDate());
    }
    
    public boolean isValidForDate(DateTime refDate){
        return BillingCycle.isValidForDate(refDate,startDate.get(),endDate.get());
    }
    
    
    @Override
    public String toString(){
        String result = "{\n"+super.toString()+",\n";
        result+="startDate : "+getStartDate()+",\n";
        result+="endDate : "+getEndDate()+",\n";
        result+="}\n";
        return result;
    }
    
}