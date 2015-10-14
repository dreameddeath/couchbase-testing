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

import com.dreameddeath.billing.model.account.BillingAccount;
import com.dreameddeath.billing.model.account.BillingAccountLink;
import com.dreameddeath.core.business.dao.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.helper.annotation.dao.Counter;
import com.dreameddeath.core.helper.annotation.dao.DaoEntity;
import com.dreameddeath.core.helper.annotation.dao.ParentEntity;
import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

@DocumentDef(domain = "billing",name="cycle",version="1.0.0")
@DaoEntity(baseDao= BusinessCouchbaseDocumentDao.class,dbPath = "cycle/",idPattern = "\\d{5}",idFormat = "%05d")
@ParentEntity(c= BillingAccount.class,keyPath = "ba.key",separator = "/")
@Counter(name = "cnt",dbName = "cnt",isKeyGen = true)
public class BillingCycle extends BusinessDocument {
    @DocumentProperty(value="ba",getter = "getBillingAccountLink",setter="setBillingAccountLink")
    private ImmutableProperty<BillingAccountLink> baLink=new ImmutableProperty<>(BillingCycle.this);
    @DocumentProperty("startDate")
    private Property<DateTime> startDate = new StandardProperty<>(BillingCycle.this);
    @DocumentProperty("endDate")
    private Property<DateTime> endDate= new StandardProperty<>(BillingCycle.this);

    public BillingAccountLink getBillingAccountLink() { return baLink.get(); }
    public void setBillingAccountLink(BillingAccountLink baLink) { this.baLink.set(baLink); }

    public DateTime getStartDate() { return startDate.get(); }
    public void setStartDate(DateTime startDate) { this.startDate.set(startDate); }

    public DateTime getEndDate() { return endDate.get(); }
    public void setEndDate(DateTime endDate) { this.endDate.set(endDate); }

    public BillingCycleLink newLink(){
        return new BillingCycleLink(this);
    }
    
    public boolean isValidForDate(DateTime refDate){
        return BillingCycle.isValidForDate(refDate,startDate.get(),endDate.get());
    }
    
    public static boolean isValidForDate(DateTime refDate, DateTime startTime,DateTime endTime){
        return (refDate.compareTo(startTime) >= 0) && (refDate.compareTo(endTime) < 0);
    }
    
    @Override
    public String toString(){
        String result= super.toString()+",\n";
        result+="startDate:"+startDate+",\n";
        result+="startend:"+endDate+",\n";
        return result;
    }
}