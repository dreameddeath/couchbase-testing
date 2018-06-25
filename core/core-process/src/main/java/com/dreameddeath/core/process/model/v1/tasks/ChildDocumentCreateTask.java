/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.process.model.v1.tasks;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 23/02/2016.
 */
@DocumentEntity
public abstract class ChildDocumentCreateTask<TCHILD extends CouchbaseDocument,TPARENT extends CouchbaseDocument> extends DocumentCreateTask<TCHILD> {
    public ChildDocumentCreateTask(String parentKey){
        setParentDocKey(parentKey);
    }

    public ChildDocumentCreateTask(){
    }


    /**
     *  parentDocKey : Parent document key
     */
    @DocumentProperty("parent")
    private Property<String> parentDocKey = new ImmutableProperty<>(ChildDocumentCreateTask.this);

    /**
     * Getter of parentDocKey
     * @return the content
     */
    public String getParentDocKey() { return parentDocKey.get(); }
    /**
     * Setter of parentDocKey
     * @param parentKey the key of the parement
     */
    public void setParentDocKey(String parentKey) { parentDocKey.set(parentKey); }

    public Single<TPARENT> getParentDocument(ICouchbaseSession session){return session.asyncGet(getParentDocKey());}
}
