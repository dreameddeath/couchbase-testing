package com.dreameddeath.core.process.document.model;

import com.dreameddeath.core.model.process.DocumentDeleteRequest;
import com.dreameddeath.core.model.process.DocumentDeleteResponse;
import com.dreameddeath.core.model.process.AbstractJob;

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

}
