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
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.couchbase.core.catalog.model.v1.CatalogElement;
import com.google.common.base.Preconditions;

/**
 * Created by Christophe Jeunesse on 07/09/2014.
 */
public class ChangeSetItem extends CouchbaseDocumentElement {
    /**
     *  key : the catalog element key
     */
    @DocumentProperty("key")
    private Property<String> key = new StandardProperty<>(ChangeSetItem.this);
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
    /**
     * target : the target model
     */
    @DocumentProperty("target")
    private Property<EntityModelId> target= new StandardProperty<>(ChangeSetItem.this);


    // id accessors
    public String getKey() { return key.get(); }
    public void setKey(String val) { key.set(val); }
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

    /**
     * Getter of the attribute {@link #target}
     * @return the currentValue of {@link #target}
     */
    public EntityModelId getTarget(){
        return this.target.get();
    }

    /**
     * Setter of the attribute {@link #target}
     * @param newValue the newValue of {@link #target}
     */
    public void setTarget(EntityModelId newValue){
        this.target.set(newValue);
    }


    public static <T extends CatalogElement> ChangeSetItem build(T element){
        ChangeSetItem changeSetItem = new ChangeSetItem();
        Preconditions.checkNotNull(element.getBaseMeta().getKey(),"Please save and/or affect key for item "+element.getClass()+"/"+element.getId());
        Preconditions.checkNotNull(element.getBaseMeta().getKey(),"Please save and/or affect id for item type "+element.getClass());
        changeSetItem.setKey(element.getBaseMeta().getKey());
        changeSetItem.setTarget(EntityDef.build(element.getClass()).getModelId());
        changeSetItem.setId(element.getId());
        changeSetItem.setVersion(element.getVersion());
        return changeSetItem;
    }
}
