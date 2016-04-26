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

package com.dreameddeath.installedbase.model.offer;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;

/**
 * Created by Christophe Jeunesse on 21/10/2014.
 */
@DocumentEntity(name = "atomicOffer")
public class InstalledAtomicOffer extends InstalledOffer {
    /**
     *  ps : Product service id
     */
    @DocumentProperty("ps")
    private Property<String> ps = new ImmutableProperty<>(InstalledAtomicOffer.this);

    /**
     * Getter of ps
     * @return the content
     */
    public String getPs() { return ps.get(); }
    /**
     * Setter of ps
     * @param val the new content
     */
    public void setPs(String val) { ps.set(val); }
}
