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

package com.dreameddeath.rating.model.context;

import com.dreameddeath.billing.model.v1.account.BillingAccountLink;
import com.dreameddeath.billing.model.v1.cycle.BillingCycle;
import com.dreameddeath.billing.model.v1.cycle.BillingCycleLink;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.util.Collections;
import java.util.List;


@JsonTypeInfo(use=Id.MINIMAL_CLASS, include=As.PROPERTY, property="@c")
public abstract class RatingContext extends CouchbaseDocument{
    @DocumentProperty(value="billingCycle",setter="setBillingCycleLink",getter="getBillingCycleLink")
    private ImmutableProperty<BillingCycleLink> _billingCycleLink=new ImmutableProperty<BillingCycleLink>(RatingContext.this);
    @DocumentProperty("billingAccount")
    private ImmutableProperty<BillingAccountLink> _billingAccountLink=new ImmutableProperty<BillingAccountLink>(RatingContext.this);
    @DocumentProperty("attributes")
    private List<RatingContextAttribute> _attributes=new ArrayListProperty<RatingContextAttribute>(RatingContext.this);
    @DocumentProperty("buckets")
    private List<RatingContextBucket> _buckets=new ArrayListProperty<RatingContextBucket>(RatingContext.this);

    public BillingCycleLink getBillingCycleLink(){ return _billingCycleLink.get(); }
    public void setBillingCycleLink(BillingCycleLink billingCycleLink){ _billingCycleLink.set(billingCycleLink); }
    public void setBillingCycle(BillingCycle billingCycle){ billingCycle.addRatingContext(this); }

    public BillingAccountLink getBillingAccountLink(){ return _billingAccountLink.get();}
    public void setBillingAccountLink(BillingAccountLink baLink){ _billingAccountLink.set(baLink);}
    
    public List<RatingContextBucket> getBuckets(){ return Collections.unmodifiableList(_buckets); }
    public void setBuckets(List<RatingContextBucket> buckets){_buckets.clear(); _buckets.addAll(buckets);}
    public void addBuckets(List<RatingContextBucket> buckets){ _buckets.addAll(buckets);}
    public void addBucket(RatingContextBucket bucket){ _buckets.add(bucket);}
    
    public List<RatingContextAttribute> getAttributes(){return Collections.unmodifiableList(_attributes);}
    public void setAttributes(List<RatingContextAttribute> attributes){_attributes.clear(); _attributes.addAll(attributes);}
    public void addAttributes(List<RatingContextAttribute> attributes){_attributes.addAll(attributes);}
    public void addAttribute(RatingContextAttribute attribute){_attributes.add(attribute);}
    
    public RatingContextLink newRatingContextLink(){ return new RatingContextLink(this);}
    
    @Override
    public String toString(){
        String result = super.toString()+"\n";
        result+="ba : "+getBillingAccountLink().toString()+",\n";
        result+="billCycle : "+getBillingCycleLink().toString()+",\n";
        return result;
    }
}