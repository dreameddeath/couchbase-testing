/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.process.exception;


import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.ProcessState.State;
import com.dreameddeath.core.process.service.context.JobContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class JobExecutionException extends Exception {
    private final State state;
    private final AbstractJob job;
    private final List<EventFireResult<?>> failedNotifications=new ArrayList<>();

    public JobExecutionException(JobContext<?> ctxt, String message) {
        this(ctxt.getInternalJob(),ctxt.getJobState().getState(),message);
    }

    public JobExecutionException(JobContext<?> ctxt, String message,Throwable e) {
        this(ctxt.getInternalJob(),ctxt.getJobState().getState(),message,e);
    }

    public JobExecutionException(JobContext<?> ctxt, Throwable e) {
        this(ctxt.getInternalJob(),ctxt.getJobState().getState(),e);
    }


    public JobExecutionException(AbstractJob job, State state, String message) {
        super(message);
        this.job = job;
        this.state = state;
    }

    public JobExecutionException(AbstractJob job, State state, String message, Throwable e) {
        super(message, e);
        this.job = job;
        this.state = state;
    }

    public JobExecutionException(AbstractJob job, State state, Throwable e) {
        super(e);
        this.job = job;
        this.state = state;
    }

    public <T extends AbstractJob> JobExecutionException(JobContext<T> context, String message, List<EventFireResult<?>> failedNotifs) {
        super(message);
        this.job = context.getInternalJob();
        this.state = context.getJobState().getState();
        this.failedNotifications.addAll(failedNotifs);
    }

    public AbstractJob getJob(){ return job;}
    public State getState(){ return state;}

    public List<EventFireResult<?>> getFailedNotifications() {
        return Collections.unmodifiableList(failedNotifications);
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("Job[").append(job.getClass()).append("/").append(job.getUid()).append("] ");
        sb.append(super.toString());
        return sb.toString();
    }
}
