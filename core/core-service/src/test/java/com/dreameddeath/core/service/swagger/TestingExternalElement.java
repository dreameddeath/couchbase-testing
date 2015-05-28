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

package com.dreameddeath.core.service.swagger;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 03/03/2015.
 */
public class TestingExternalElement extends CouchbaseDocumentElement {
    /**
     *  date : DateTime test
     */
    @DocumentProperty("date")
    private ListProperty<DateTime> _date = new ArrayListProperty<DateTime>(TestingExternalElement.this);

    // Date Accessors
    public List<DateTime> getDate() { return _date.get(); }
    public void setDate(Collection<DateTime> vals) { _date.set(vals); }
    public boolean addDate(DateTime val){ return _date.add(val); }
    public boolean removeDate(DateTime val){ return _date.remove(val); }

}
