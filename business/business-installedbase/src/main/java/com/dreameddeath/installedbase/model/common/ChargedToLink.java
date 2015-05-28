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

package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.billing.model.account.BillingAccountLink;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 11/08/2014.
 */
public class ChargedToLink extends BillingAccountLink {
    /**
     *  startDate : date de debut d'application du lien
     */
    @DocumentProperty("startDate")
    private Property<DateTime> _startDate = new StandardProperty<>(ChargedToLink.this);
    /**
     *  endDate : date de fin de l'application du lien
     */
    @DocumentProperty("endDate")
    private Property<DateTime> _endDate = new StandardProperty<>(ChargedToLink.this);
    /**
     *  elements : List of applicable elements (empty list means all)
     */
    @DocumentProperty("elements")
    private ListProperty<ChargeableElementFilter> _elements = new ArrayListProperty<>(ChargedToLink.this);


    // startDate accessors
    public DateTime getStartDate() { return _startDate.get(); }
    public void setStartDate(DateTime val) { _startDate.set(val); }
    // endDate accessors
    public DateTime getEndDate() { return _endDate.get(); }
    public void setEndDate(DateTime val) { _endDate.set(val); }
    // Elements Accessors
    public List<ChargeableElementFilter> getElements() { return _elements.get(); }
    public void setElements(Collection<ChargeableElementFilter> vals) { _elements.set(vals); }
    public boolean addElements(ChargeableElementFilter val){ return _elements.add(val); }
    public boolean removeElements(ChargeableElementFilter val){ return _elements.remove(val); }

    public enum ChargeableElementFilter {
        RECURRING_TARIFFS,
        ONE_SHOT_TARIFFS,
        USAGES
    }
}
