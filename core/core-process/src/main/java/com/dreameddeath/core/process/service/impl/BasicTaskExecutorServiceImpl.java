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

package com.dreameddeath.core.process.service.impl;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.process.exception.TaskExecutionException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.model.AbstractTask;
import com.dreameddeath.core.process.model.ProcessState;
import com.dreameddeath.core.process.model.ProcessState.State;
import com.dreameddeath.core.process.model.SubJobProcessTask;
import com.dreameddeath.core.process.service.IJobExecutorClient;
import com.dreameddeath.core.process.service.ITaskExecutorService;
import com.dreameddeath.core.process.service.context.TaskContext;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class BasicTaskExecutorServiceImpl<TJOB extends AbstractJob,T extends AbstractTask> implements ITaskExecutorService<TJOB,T> {
    public void onSave(TaskContext<TJOB,T> ctxt, State state){}
    public void onEndProcessing(TaskContext<TJOB,T> ctxt, State state){}

    public void manageStateExecutionEnd(TaskContext<TJOB,T> ctxt, State newState, boolean needSave) throws DaoException,ValidationException,StorageException{
        ctxt.getTaskState().setState(newState);
        if(needSave) {
            onSave(ctxt, newState);
            ctxt.save();
        }
        onEndProcessing(ctxt,newState);
    }

    @Override
    public void execute(TaskContext<TJOB,T> ctxt) throws TaskExecutionException {
        T task = ctxt.getTask();
        ProcessState taskState = ctxt.getTaskState();
        taskState.setLastRunError(null);
        try {
            if (!taskState.isInitialized()) {
                try {
                    boolean saveAsked = ctxt.getProcessingService().init(ctxt);
                    manageStateExecutionEnd(ctxt,State.INITIALIZED,saveAsked);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.INITIALIZED, e);
                }
            }

            if (!taskState.isPrepared()) {
                try {
                    boolean saveAsked=ctxt.getProcessingService().preprocess(ctxt);
                    manageStateExecutionEnd(ctxt,State.PREPROCESSED,saveAsked);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.PREPROCESSED, e);
                }
            }

            if (!taskState.isProcessed()) {
                try {
                    boolean saveAsked;
                    if(task instanceof SubJobProcessTask){
                        SubJobProcessTask subJobTask = (SubJobProcessTask)task;
                        AbstractJob subJob = subJobTask.getJob(ctxt.getSession());
                        if(!subJob.getStateInfo().isDone()){
                            IJobExecutorClient subJobClient = ctxt.getJobContext().getClientFactory().buildJobClient(subJob.getClass());
                            subJobClient.executeJob(subJob,ctxt.getUser());
                        }
                        saveAsked=true;
                    }
                    else {
                        saveAsked = ctxt.getProcessingService().process(ctxt);
                    }

                    manageStateExecutionEnd(ctxt,State.PROCESSED,saveAsked);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.PROCESSED, e);
                }
            }

            if (!taskState.isFinalized()) {
                try {
                    boolean saveAsked=ctxt.getProcessingService().postprocess(ctxt);
                    manageStateExecutionEnd(ctxt,State.POSTPROCESSED,saveAsked);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.POSTPROCESSED, e);
                }
            }

            if (!taskState.isJobUpdated()) {
                try {
                    boolean saveAsked=ctxt.getProcessingService().updatejob(ctxt);
                    if(saveAsked){
                        ctxt.getJobContext().save();
                    }
                    manageStateExecutionEnd(ctxt,State.JOBUPDATED,saveAsked);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.JOBUPDATED, e);
                }
            }



            if (!taskState.isDone()) {
                try{
                    boolean needSave=ctxt.getProcessingService().cleanup(ctxt);
                    manageStateExecutionEnd(ctxt,State.DONE,needSave);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.DONE, e);
                }
            }
        } catch(TaskExecutionException e){
            throw e;
        } catch (Throwable e){
            throw new TaskExecutionException(task,State.UNKNOWN,e);
        }
    }
}
