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

package com.dreameddeath.installedbase.process.model.v1;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.tasks.DocumentCreateTask;
import com.dreameddeath.core.process.model.v1.tasks.DocumentUpdateTask;
import com.dreameddeath.core.process.model.v1.tasks.NoOpTask;
import com.dreameddeath.installedbase.model.v1.InstalledBase;

/**
 * Created by Christophe Jeunesse on 04/09/2014.
 */
@DocumentEntity
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


    @DocumentEntity
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

    @DocumentEntity
    public static class UpdateInstalledBaseTask extends DocumentUpdateTask<InstalledBase>  {
        /**
         *  contractUid : UID of the contract
         */
        @DocumentProperty("contractUid")
        private Property<String> contractUid = new StandardProperty<>(UpdateInstalledBaseTask.this);
        /**
         *  tempContractId : temporary contract id for creation
         */
        @DocumentProperty("tempContractId")
        private Property<String> tempContractId = new StandardProperty<>(UpdateInstalledBaseTask.this);
        /**
         *  updateResult : result of the update
         */
        @DocumentProperty("updateResult")
        private Property<InstalledBaseUpdateResult> updateResult = new StandardProperty<>(UpdateInstalledBaseTask.this);

        // contractUid accessors
        public String getContractUid() { return contractUid.get(); }
        public void setContractUid(String val) { contractUid.set(val); }

        /**
         * Getter of tempContractId
         * @return the value of tempContractId
         */
        public String getTempContractId() { return tempContractId.get(); }
        /**
         * Setter of tempContractId
         * @param val the new value of tempContractId
         */
        public void setTempContractId(String val) { tempContractId.set(val); }

        /**
         * Getter of updateResult
         * @return the value of updateResult
         */
        public InstalledBaseUpdateResult getUpdateResult() { return updateResult.get(); }
        /**
         * Setter of updateResult
         * @param val the new value of updateResult
         */
        public void setUpdateResult(InstalledBaseUpdateResult val) { updateResult.set(val); }

    }
}
