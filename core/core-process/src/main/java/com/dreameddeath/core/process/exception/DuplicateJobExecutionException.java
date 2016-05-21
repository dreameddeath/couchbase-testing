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

package com.dreameddeath.core.process.exception;

import com.dreameddeath.core.model.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.service.context.JobContext;

/**
 * Created by Christophe Jeunesse on 14/01/2016.
 */
public class DuplicateJobExecutionException extends JobExecutionException {
    public DuplicateJobExecutionException(JobContext<?> ctxt, String message) {
        super(ctxt, message);
    }

    public DuplicateJobExecutionException(JobContext<?> ctxt, String message, DuplicateUniqueKeyException e) {
        super(ctxt, message, e);
    }

    public DuplicateJobExecutionException(JobContext<?> ctxt, DuplicateUniqueKeyException e) {
        super(ctxt, e);
    }

    public DuplicateJobExecutionException(AbstractJob job, ProcessState.State state, String message) {
        super(job, state, message);
    }

    public DuplicateJobExecutionException(AbstractJob job, ProcessState.State state, String message, DuplicateUniqueKeyException e) {
        super(job, state, message, e);
    }

    public DuplicateJobExecutionException(AbstractJob job, ProcessState.State state, DuplicateUniqueKeyException e) {
        super(job, state, e);
    }

    @Override
    public DuplicateUniqueKeyException getCause(){
        return (DuplicateUniqueKeyException) super.getCause();
    }


    public String getKey() {
        return (getCause()!=null)?getCause().getKey():null;
    }

    public CouchbaseUniqueKey getUniqueKeyDoc() {
        return (getCause()!=null)?getCause().getUniqueKeyDoc():null;
    }

    public String getOwnerDocumentKey() {
        return (getCause()!=null)?getCause().getOwnerDocumentKey():null;
    }
}
