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

package com.dreameddeath.common.model.v1;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by Christophe Jeunesse on 11/08/2014.
 */
public class ExternalId extends CouchbaseDocumentElement {
    /**
     *  id : The external id
     */
    @DocumentProperty("id")
    private Property<String> id = new StandardProperty<String>(ExternalId.this);
    /**
     *  refCode : Id of the referential for this code
     */
    @DocumentProperty("referentialCode")
    private Property<String> refCode = new StandardProperty<String>(ExternalId.this);
    /**
     *  referentialInstance : the instance id of the referential
     */
    @DocumentProperty("referentialInstance")
    private Property<String> referentialInstance = new StandardProperty<String>(ExternalId.this);

    // id accessors
    public String getId() { return id.get(); }
    public void setId(String val) { id.set(val); }

    // refCode accessors
    public String getRefCode() { return refCode.get(); }
    public void setRefCode(String val) { refCode.set(val); }

    // referentialInstance accessors
    public String getReferentialInstance() { return referentialInstance.get(); }
    public void setReferentialInstance(String val) { referentialInstance.set(val); }

}
