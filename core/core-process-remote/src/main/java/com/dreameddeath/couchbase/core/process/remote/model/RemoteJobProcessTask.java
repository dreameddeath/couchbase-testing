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
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.process.model.base.AbstractTask;

/**
 * Created by Christophe Jeunesse on 13/01/2016.
 */
public abstract class RemoteJobProcessTask<TREQ,TRESP> extends AbstractTask {
    /**
     *  jobInfo : The data around remote job processing info
     */
    @DocumentProperty("remoteJobInfo")
    private Property<RemoteJobInfo> remoteJobInfo = new StandardProperty<>(RemoteJobProcessTask.this,new RemoteJobInfo());
    /**
     * Getter of jobInfo
     * @return the content
     */
    public RemoteJobInfo getRemoteJobInfo() { return remoteJobInfo.get(); }
    /**
     * Setter of jobInfo
     * @param val new content
     */
    public void setRemoteJobInfo(RemoteJobInfo val) { remoteJobInfo.set(val); }
}
