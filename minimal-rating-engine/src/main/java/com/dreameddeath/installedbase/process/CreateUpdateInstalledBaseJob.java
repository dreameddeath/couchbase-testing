package com.dreameddeath.installedbase.process;

import com.dreameddeath.core.event.TaskProcessEvent;
import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.model.process.AbstractJob;
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


}
