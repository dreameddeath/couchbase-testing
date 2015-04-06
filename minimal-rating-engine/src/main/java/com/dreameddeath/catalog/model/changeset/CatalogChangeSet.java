/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.catalog.model.changeset;

import com.dreameddeath.catalog.model.CatalogElement;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.MapProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.HashMapCollectionProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.List;
import java.util.Map;

/**
 * Created by ceaj8230 on 05/09/2014.
 */
public class CatalogChangeSet extends CatalogElement {
    /**
     *  name : name of the change set
     */
    @DocumentProperty("name")
    private Property<String> _name = new StandardProperty<String>(CatalogChangeSet.this);
    /**
     *  descr : Description of the changeset
     */
    @DocumentProperty("descr")
    private Property<String> _descr = new StandardProperty<String>(CatalogChangeSet.this);
    /**
     *  state : ChangeSet state
     */
    @DocumentProperty("state")
    private Property<State> _state = new StandardProperty<State>(CatalogChangeSet.this);
    /**
     *  domainChanges : List of changes per domain
     */
    @DocumentProperty("domainChanges")
    private MapProperty<String,List<ChangeSetItem>> _domainChanges = new HashMapCollectionProperty<String, List<ChangeSetItem>>(CatalogChangeSet.this,new ArrayListProperty.MapValueBuilder<ChangeSetItem>());


    // name accessors
    public String getName() { return _name.get(); }
    public void setName(String val) { _name.set(val); }
    // descr accessors
    public String getDescr() { return _descr.get(); }
    public void setDescr(String val) { _descr.set(val); }
    // state accessors
    public State getState() { return _state.get(); }
    public void setState(State val) { _state.set(val); }
    // DomainChanges Accessors
    public Map<String,List<ChangeSetItem>> getDomainChanges() { return _domainChanges.get(); }
    public void setDomainChanges(Map<String,List<ChangeSetItem>> vals) { _domainChanges.set(vals); }

    public enum State{
        DEV,
        QUALIF,
        PREPROD,
        PROD
    }
}
