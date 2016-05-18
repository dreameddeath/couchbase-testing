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

package com.dreameddeath.billing.model.v1.cycle;


import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.business.CouchbaseDocumentLink;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.SynchronizedLinkProperty;
import org.joda.time.DateTime;

public class BillingCycleLink extends CouchbaseDocumentLink<BillingCycle>{
    @DocumentProperty("startDate")
    private Property<DateTime> _startDate=new SynchronizedLinkProperty<DateTime,BillingCycle>(BillingCycleLink.this){
        @Override
        protected  DateTime getRealValue(BillingCycle cycle){
            return cycle.getStartDate();
        }
    };
    @DocumentProperty("endDate")
    private Property<DateTime> _endDate=new SynchronizedLinkProperty<DateTime,BillingCycle>(BillingCycleLink.this){
        @Override
        protected  DateTime getRealValue(BillingCycle cycle){
            return cycle.getEndDate();
        }
    };
    
    public DateTime getStartDate() { return _startDate.get(); }
    public void setStartDate(DateTime startDate) { _startDate.set(startDate); }
    
    public DateTime getEndDate() { return _endDate.get(); }
    public void setEndDate(DateTime endDate) { _endDate.set(endDate); }
    
    public BillingCycleLink(){}
    public BillingCycleLink(BillingCycle billCycle){ super(billCycle);}
    public BillingCycleLink(BillingCycleLink srcLink){
        super(srcLink);
        setStartDate(srcLink.getStartDate());
        setEndDate(srcLink.getEndDate());
    }
    
    public boolean isValidForDate(DateTime refDate){
        return BillingCycle.isValidForDate(refDate,_startDate.get(),_endDate.get());
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