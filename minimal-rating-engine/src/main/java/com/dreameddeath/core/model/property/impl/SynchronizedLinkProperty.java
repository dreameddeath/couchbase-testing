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

package com.dreameddeath.core.model.property.impl;

import com.dreameddeath.core.model.business.CouchbaseDocumentLink;
import com.dreameddeath.core.model.document.BaseCouchbaseDocument;

public abstract class SynchronizedLinkProperty<T,TDOC extends BaseCouchbaseDocument> extends StandardProperty<T> {
    CouchbaseDocumentLink<TDOC> _parentLink;

    public SynchronizedLinkProperty(CouchbaseDocumentLink<TDOC> parentLink){
        super(parentLink);
        parentLink.addChildSynchronizedProperty(this);
        _parentLink=parentLink;
    }
    
    protected abstract T getRealValue(TDOC doc);

    @Override
    public final T get(){ 
        if(_parentLink.getLinkedObjectFromCache()!=null){
            set(getRealValue(_parentLink.getLinkedObjectFromCache()));
        }
        return super.get();
    }

    public void sync(){
        get();
    }
}
