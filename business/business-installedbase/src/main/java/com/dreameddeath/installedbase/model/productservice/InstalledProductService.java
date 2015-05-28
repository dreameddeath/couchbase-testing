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

package com.dreameddeath.installedbase.model.productservice;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.installedbase.model.common.InstalledItem;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
public class InstalledProductService extends InstalledItem<InstalledProductServiceRevision> {
    /**
     *  functions : give the list of functions attached to the Product
     */
    @DocumentProperty("functions")
    private ListProperty<InstalledFunction> _functions = new ArrayListProperty<InstalledFunction>(InstalledProductService.this);

    // Functions Accessors
    public List<InstalledFunction> getFunctions() { return _functions.get(); }
    public void setFunctions(Collection<InstalledFunction> vals) { _functions.set(vals); }
    public boolean addFunctions(InstalledFunction val){ return _functions.add(val); }
    public boolean removeFunctions(InstalledFunction val){ return _functions.remove(val); }

}
