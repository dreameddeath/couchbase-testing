package com.dreameddeath.core.process.business;

import com.dreameddeath.core.event.TaskProcessEvent;
import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.model.process.DocumentDeleteRequest;
import com.dreameddeath.core.model.process.DocumentDeleteResponse;
import com.dreameddeath.core.process.common.AbstractJob;

/**
 * Created by CEAJ8230 on 22/09/2014.
 */
public class DocumentDeleteJob extends AbstractJob<DocumentDeleteRequest,DocumentDeleteResponse> {
    @Override
    public DocumentDeleteRequest newRequest() {
        return new DocumentDeleteRequest();
    }
    @Override
    public DocumentDeleteResponse newResult() {
        return new DocumentDeleteResponse();
    }
    @Override
    public boolean when(TaskProcessEvent evt) throws JobExecutionException {
        return false;
    }

}
