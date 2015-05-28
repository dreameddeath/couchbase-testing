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

package com.dreameddeath.installedbase.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.event.TaskProcessEvent;
import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.process.business.DocumentCreateTask;
import com.dreameddeath.core.process.business.DocumentUpdateTask;
import com.dreameddeath.core.process.common.AbstractJob;
import com.dreameddeath.core.process.common.NoOpTask;
import com.dreameddeath.installedbase.model.common.InstalledBase;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseResponse;

/**
 * Created by Christophe Jeunesse on 04/09/2014.
 */
public class CreateUpdateInstalledBaseJob extends AbstractJob<CreateUpdateInstalledBaseRequest,CreateUpdateInstalledBaseResponse> {

    @Override
    public CreateUpdateInstalledBaseRequest newRequest() {
        return new CreateUpdateInstalledBaseRequest();
    }

    @Override
    public CreateUpdateInstalledBaseResponse newResult() {
        return new CreateUpdateInstalledBaseResponse();
    }

    @Override
    public boolean when(TaskProcessEvent evt) throws JobExecutionException {
        return false;
    }

    @Override
    public boolean init() throws JobExecutionException{
        for(CreateUpdateInstalledBaseRequest.Contract contract: this.getRequest().contracts){
            UpdateInstalledBase updateTask=new UpdateInstalledBase();

            if(contract.comOp.equals(CreateUpdateInstalledBaseRequest.CommercialOperation.ADD)){
                InitEmptyInstalledBase emptyCreateTask=addTask(new InitEmptyInstalledBase());
                emptyCreateTask.setContractTempId(contract.tempId);
                emptyCreateTask.chainWith(new UpdateInstalledBase());
            }
            else{
                updateTask.setContractUid(contract.id);
            }
        }

        return true;
    }

    public static class InitEmptyInstalledBase extends DocumentCreateTask<InstalledBase>{
        /**
         *  contractTempId : The contract temporary Id to be created
         */
        @DocumentProperty("contractTempId")
        private Property<String> _contractTempId = new StandardProperty<String>(InitEmptyInstalledBase.this);

        // contractTempId accessors
        public String getContractTempId() { return _contractTempId.get(); }
        public void setContractTempId(String val) { _contractTempId.set(val); }

        @Override
        public InstalledBase buildDocument(){
            InstalledBase result = newEntity(InstalledBase.class);
            return result;
        }

        @Override
        public boolean postprocess()throws TaskExecutionException {
            CreateUpdateInstalledBaseResponse.Contract result = new CreateUpdateInstalledBaseResponse.Contract();
            result.tempId = getContractTempId();
            try {
                result.id = this.getDocument().getUid();
            }
            catch(DaoException e){
                throw new TaskExecutionException(this,this.getState(),"Dao error during retrieval of doc "+this.getDocKey(),e);
            }
            catch(StorageException e){
                throw new TaskExecutionException(this,this.getState(),"Storage error during retrieval of doc "+this.getDocKey(),e);
            }
            getJobResult(CreateUpdateInstalledBaseResponse.class).contracts.add(result);
            return true;
        }
    }

    public static class AfterPreCreateSyncTask extends NoOpTask{}

    public static class UpdateInstalledBase extends DocumentUpdateTask<InstalledBase> {
        /**
         *  contractUid : UID of the contract
         */
        @DocumentProperty("contractUid")
        private Property<String> _contractUid = new StandardProperty<String>(UpdateInstalledBase.this);

        // contractUid accessors
        public String getContractUid() { return _contractUid.get(); }
        public void setContractUid(String val) { _contractUid.set(val); }

        @Override
        public boolean preprocess() throws TaskExecutionException{
            if(getContractUid()!=null){
                try {
                    setDocKey(getParentJob().getBaseMeta().getSession().getKeyFromUID(getContractUid(), InstalledBase.class));
                }
                catch(DaoException e){
                    throw new TaskExecutionException(this,this.getState(),"Cannot build key from uid <"+getContractUid()+">");
                }
            }
            else {
                InitEmptyInstalledBase creationInstalledBase = getDependentTask(InitEmptyInstalledBase.class);
                if (creationInstalledBase != null) {
                    setDocKey(creationInstalledBase.getDocKey());
                }
                else{
                    throw new TaskExecutionException(this,this.getState(),"Inconsistent State : neither existing InstalledBase nor InitTask found");
                }
            }
            return false;
        }

        @Override
        public void processDocument(){

        }
    }
}
