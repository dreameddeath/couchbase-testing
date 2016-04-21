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

package com.dreameddeath.installedbase.process.model;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.tasks.DocumentCreateTask;
import com.dreameddeath.core.process.model.v1.tasks.DocumentUpdateTask;
import com.dreameddeath.core.process.model.v1.tasks.NoOpTask;
import com.dreameddeath.installedbase.model.InstalledBase;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseResponse;

/**
 * Created by Christophe Jeunesse on 04/09/2014.
 */
public class CreateUpdateInstalledBaseJob extends AbstractJob{
    /**
     *  request : The create update installedbase request
     */
    @DocumentProperty("request")
    private Property<CreateUpdateInstalledBaseRequest> request = new ImmutableProperty<>(CreateUpdateInstalledBaseJob.this,CreateUpdateInstalledBaseRequest.class);
    /**
     *  result : The CreateUpdate result
     */
    @DocumentProperty("result")
    private Property<CreateUpdateInstalledBaseResponse> result = new StandardProperty<>(CreateUpdateInstalledBaseJob.this,CreateUpdateInstalledBaseResponse.class);


    // request accessors
    public CreateUpdateInstalledBaseRequest getRequest() { return request.get(); }
    public void setRequest(CreateUpdateInstalledBaseRequest val) { request.set(val); }
    // result accessors
    public CreateUpdateInstalledBaseResponse getResult() { return result.get(); }
    public void setResult(CreateUpdateInstalledBaseResponse val) { result.set(val); }


    public static class InitEmptyInstalledBase extends DocumentCreateTask<InstalledBase> {
        /**
         *  contractTempId : The contract temporary Id to be created
         */
        @DocumentProperty("contractTempId")
        private Property<String> contractTempId = new StandardProperty<>(InitEmptyInstalledBase.this);

        // contractTempId accessors
        public String getContractTempId() { return contractTempId.get(); }
        public void setContractTempId(String val) { contractTempId.set(val); }

    }

    public static class AfterPreCreateSyncTask extends NoOpTask {}

    public static class UpdateInstalledBase extends DocumentUpdateTask<InstalledBase> {
        /**
         *  contractUid : UID of the contract
         */
        @DocumentProperty("contractUid")
        private Property<String> contractUid = new StandardProperty<>(UpdateInstalledBase.this);

        // contractUid accessors
        public String getContractUid() { return contractUid.get(); }
        public void setContractUid(String val) { contractUid.set(val); }
    }
}
