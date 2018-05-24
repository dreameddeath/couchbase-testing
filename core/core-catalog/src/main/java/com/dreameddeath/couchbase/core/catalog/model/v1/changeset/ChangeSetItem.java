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

package com.dreameddeath.couchbase.core.catalog.model.v1.changeset;

import com.dreameddeath.core.json.model.Version;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by Christophe Jeunesse on 07/09/2014.
 */
public class ChangeSetItem extends CouchbaseDocumentElement {
    /**
     *  id : Catalog element item id
     */
    @DocumentProperty("id")
    private Property<String> id = new StandardProperty<>(ChangeSetItem.this);
    /**
     *  version : Version in string format
     */
    @DocumentProperty("version")
    private Property<Version> version = new StandardProperty<>(ChangeSetItem.this);
    /**
     * descr : the description of the change
     */
    @DocumentProperty("descr")
    private Property<String> descr = new StandardProperty<>(ChangeSetItem.this);

    // id accessors
    public String getId() { return id.get(); }
    public void setId(String val) { id.set(val); }
    // version accessors
    public Version getVersion() { return version.get(); }
    public void setVersion(Version val) { version.set(val); }
    /**
     * Getter of the attribute {@link #descr}
     * @return the currentValue of {@link #descr}
     */
    public String getDescr(){
        return this.descr.get();
    }

    /**
     * Setter of the attribute {@link #descr}
     * @param newValue the newValue of {@link #descr}
     */
    public void setDescr(String newValue){
        this.descr.set(newValue);
    }

}
