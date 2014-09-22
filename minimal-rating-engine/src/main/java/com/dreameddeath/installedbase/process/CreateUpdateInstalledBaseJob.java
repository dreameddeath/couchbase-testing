package com.dreameddeath.installedbase.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.event.TaskProcessEvent;
import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.process.common.AbstractJob;
import com.dreameddeath.core.process.document.DocumentCreateTask;
import com.dreameddeath.installedbase.model.common.InstalledBase;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseRequest;
import com.dreameddeath.installedbase.model.process.CreateUpdateInstalledBaseResponse;

/**
 * Created by ceaj8230 on 04/09/2014.
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
            if(contract.comOp.equals(CreateUpdateInstalledBaseRequest.CommercialOperation.ADD)){
                //TODO add create installedBaseTask
            }
        }
        return true;
    }

    public static class InitEmptyInstalledBase extends DocumentCreateTask<InstalledBase>{
        /**
         *  contractTempId : The contract temporay Id to be created
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
    }
}
