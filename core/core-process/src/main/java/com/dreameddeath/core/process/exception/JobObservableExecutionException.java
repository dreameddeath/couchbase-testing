/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.process.exception;

import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.service.context.JobContext;

/**
 * Created by Christophe Jeunesse on 27/10/2016.
 */
public class JobObservableExecutionException extends RuntimeException {
    public JobObservableExecutionException(JobExecutionException e){
        super(e);
    }

    public JobObservableExecutionException(JobContext<?> ctxt, String message) {
        this(new JobExecutionException(ctxt.getInternalJob(),ctxt.getJobState().getState(),message));
    }

    public JobObservableExecutionException(JobContext<?> ctxt, String message,Throwable e) {
        this(new JobExecutionException(ctxt.getInternalJob(),ctxt.getJobState().getState(),message,e));
    }


    public JobObservableExecutionException(JobContext<?> ctxt, Throwable e) {
        this(new JobExecutionException(ctxt.getInternalJob(),ctxt.getJobState().getState(),e));
    }


    public JobObservableExecutionException(AbstractJob job, ProcessState.State state, String message) {
        this(new JobExecutionException(job,state,message));
    }

    public JobObservableExecutionException(AbstractJob job, ProcessState.State state, String message, Throwable e) {
        this(new JobExecutionException(job,state,message, e));
    }

    public JobObservableExecutionException(AbstractJob job, ProcessState.State state, Throwable e) {
        this(new JobExecutionException(job,state,e));
    }

    public String getMessage(){
        return getCause().getMessage();
    }

    @Override
    public JobExecutionException getCause(){
        return (JobExecutionException)super.getCause();
    }


}
