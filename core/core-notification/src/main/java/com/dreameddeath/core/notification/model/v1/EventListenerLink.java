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

package com.dreameddeath.core.notification.model.v1;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;

/**
 * Created by Christophe Jeunesse on 21/08/2017.
 */
public class EventListenerLink extends CouchbaseDocumentElement {
    /**
     * name : the target listener name
     */
    @DocumentProperty("name")
    private Property<String> name = new ImmutableProperty<>(EventListenerLink.this);
    /**
     * domain : the target listener domain
     */
    @DocumentProperty("domain")
    private Property<String> domain = new ImmutableProperty<>(EventListenerLink.this);

    /**
     * Getter of the attribute name
     * return the currentValue of name
     */
    public String getName() {
        return this.name.get();
    }

    /**
     * Setter of the attribute name
     *
     * @param newValue the newValue of name
     */
    public void setName(String newValue) {
        this.name.set(newValue);
    }

    /**
     * Getter of the attribute domain
     * return the currentValue of domain
     */
    public String getDomain() {
        return this.domain.get();
    }

    /**
     * Setter of the attribute domain
     *
     * @param newValue the newValue of domain
     */
    public void setDomain(String newValue) {
        this.domain.set(newValue);
    }


    public static EventListenerLink build(String name,String domain){
        EventListenerLink result = new EventListenerLink();
        result.setDomain(domain);
        result.setName(name);
        return result;
    }

    public static EventListenerLink build(EventListenerLink src){
        return build(src.getName(),src.getDomain());
    }
}
