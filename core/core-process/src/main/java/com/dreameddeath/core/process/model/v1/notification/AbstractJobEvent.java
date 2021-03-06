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

package com.dreameddeath.core.process.model.v1.notification;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.annotation.HasEffectiveDomain;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.notification.model.v1.Event;

/**
 * Created by christophe jeunesse on 24/01/2017.
 */
public abstract class AbstractJobEvent extends Event implements HasEffectiveDomain {
    /**
     * domain : The origin domain of the event
     */
    @DocumentProperty("domain")
    private ImmutableProperty<String> domain = new ImmutableProperty<>(AbstractJobEvent.this);

    /**
     *  jobKey : The job Key
     */
    @DocumentProperty("jobKey")
    private Property<String> jobKey = new ImmutableProperty<>(AbstractJobEvent.this);

    /**
     * Getter of jobKey
     * @return the value of jobKey
     */
    public String getJobKey() { return jobKey.get(); }
    /**
     * Setter of jobKey
     * @param val the new value for jobKey
     */
    public void setJobKey(String val) { jobKey.set(val); }
    /**
     * Getter for property domain
     * @return The current value
     */
    public String getDomain(){
        return domain.get();
    }
    /**
     * Setter for property domain
     * @param newValue  the new value for the property
     */
    public void setDomain(String newValue){
        domain.set(newValue);
    }

    @Override
    public String getEffectiveDomain() {
        return domain.get();
    }
}
