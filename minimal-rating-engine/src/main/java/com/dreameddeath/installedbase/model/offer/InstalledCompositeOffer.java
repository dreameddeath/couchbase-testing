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

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 31/08/2014.
 */
public class InstalledCompositeOffer extends InstalledOffer {
    /**
     *  children : List of children offers id
     */
    @DocumentProperty("children")
    private ListProperty<String> _children = new ArrayListProperty<String>(InstalledCompositeOffer.this);

    // Children Accessors
    public List<String> getChildren() { return _children.get(); }
    public void setChildren(Collection<String> vals) { _children.set(vals); }
    public boolean addChildren(String val){ return _children.add(val); }
    public boolean removeChildren(String val){ return _children.remove(val); }

}
