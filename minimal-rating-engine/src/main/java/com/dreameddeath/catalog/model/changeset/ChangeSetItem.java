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

package com.dreameddeath.catalog.model.changeset;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by Christophe Jeunesse on 07/09/2014.
 */
public class ChangeSetItem extends BaseCouchbaseDocumentElement {
    /**
     *  id : Catalog element item id
     */
    @DocumentProperty("id")
    private Property<String> _id = new StandardProperty<String>(ChangeSetItem.this);
    /**
     *  version : Version in string format
     */
    @DocumentProperty("version")
    private Property<String> _version = new StandardProperty<String>(ChangeSetItem.this);


    // id accessors
    public String getId() { return _id.get(); }
    public void setId(String val) { _id.set(val); }
    // version accessors
    public String getVersion() { return _version.get(); }
    public void setVersion(String val) { _version.set(val); }
}
