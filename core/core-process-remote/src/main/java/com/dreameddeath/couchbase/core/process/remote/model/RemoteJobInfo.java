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

package com.dreameddeath.couchbase.core.process.remote.model;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 14/01/2016.
 */
public class RemoteJobInfo extends CouchbaseDocumentElement {
    @DocumentProperty("remoteJobId")
    private Property<UUID> remoteJobId =new ImmutableProperty<>(RemoteJobInfo.this);
    /**
     *  isDone : Tell that the remote job is finished
     */
    @DocumentProperty("isDone")
    private Property<Boolean> isDone = new StandardProperty<>(RemoteJobInfo.this,false);

    // target job uuid
    public UUID getRemoteJobId(){ return remoteJobId.get(); }
    public void setRemoteJobId(UUID remoteJobId){this.remoteJobId.set(remoteJobId);}

    // isDone accessors
    public Boolean getIsDone() { return isDone.get(); }
    public void setIsDone(Boolean val) { isDone.set(val); }

}
