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

package com.dreameddeath.couchbase.catalog.common.model.v1;


import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.couchbase.core.catalog.model.v1.CatalogElement;

/**
 * Created by Christophe Jeunesse on 05/09/2014.
 */
public class Tariff extends CatalogElement {
    /**
     * application : The applicaton context of the tariff
     */
    @DocumentProperty("application")
    private Property<ApplicationContext> application = new ImmutableProperty<>(Tariff.this);

    /**
     * Getter of the attribute {@link #application}
     * return the currentValue of {@link #application}
     */
    public ApplicationContext getApplication(){
        return this.application.get();
    }

    /**
     * Setter of the attribute {@link #application}
     * @param newValue the newValue of {@link #application}
     */
    public void setApplication(ApplicationContext newValue){
        this.application.set(newValue);
    }

    public enum ApplicationContext{
        ORDER,
        INSTALLED_BASE
    }
}
