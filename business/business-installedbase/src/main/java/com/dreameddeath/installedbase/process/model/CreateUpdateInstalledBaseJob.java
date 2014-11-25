package com.dreameddeath.installedbase.process.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.model.process.NoOpTask;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.process.business.model.DocumentCreateTask;
import com.dreameddeath.core.process.business.model.DocumentUpdateTask;
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


    public static class InitEmptyInstalledBase extends DocumentCreateTask<InstalledBase> {
        /**
         *  contractTempId : The contract temporary Id to be created
         */
        @DocumentProperty("contractTempId")
        private Property<String> _contractTempId = new StandardProperty<String>(InitEmptyInstalledBase.this);

        // contractTempId accessors
        public String getContractTempId() { return _contractTempId.get(); }
        public void setContractTempId(String val) { _contractTempId.set(val); }

    }

    public static class AfterPreCreateSyncTask extends NoOpTask {}

    public static class UpdateInstalledBase extends DocumentUpdateTask<InstalledBase> {
        /**
         *  contractUid : UID of the contract
         */
        @DocumentProperty("contractUid")
        private Property<String> _contractUid = new StandardProperty<String>(UpdateInstalledBase.this);

        // contractUid accessors
        public String getContractUid() { return _contractUid.get(); }
        public void setContractUid(String val) { _contractUid.set(val); }
    }
}
