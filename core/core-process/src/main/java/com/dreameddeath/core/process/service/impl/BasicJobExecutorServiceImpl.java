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
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.model.ProcessState;
import com.dreameddeath.core.process.model.ProcessState.State;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.context.JobContext;
import com.dreameddeath.core.process.service.context.TaskContext;


/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class BasicJobExecutorServiceImpl<T extends AbstractJob> implements IJobExecutorService<T> {

    public void onSave(JobContext<T> ctxt,ProcessState.State state){}
    public void onEndProcessing(JobContext<T> ctxt,ProcessState.State state){}

    public void manageStateExecutionEnd(JobContext<T> ctxt, ProcessState.State newState, boolean needSave) throws DaoException,ValidationException,StorageException{
        ctxt.getJobState().setState(newState);
        if(needSave) {
            onSave(ctxt, newState);
            ctxt.save();
        }
        onEndProcessing(ctxt, newState);
    }

    @Override
    public void execute(JobContext<T> ctxt) throws JobExecutionException{
        final ProcessState jobState=ctxt.getJobState();
        final AbstractJob job = ctxt.getJob();
        jobState.setLastRunError(null);
        try {
            if (!jobState.isInitialized()) {
                try {
                    boolean saveAsked;
                    saveAsked=ctxt.getProcessingService().init(ctxt);
                    manageStateExecutionEnd(ctxt,State.INITIALIZED,saveAsked);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.INITIALIZED, e);
                }
            }

            if (!jobState.isPrepared()) {
                try {
                    boolean saveAsked;
                    saveAsked=ctxt.getProcessingService().preprocess(ctxt);
                    manageStateExecutionEnd(ctxt,State.PREPROCESSED,saveAsked);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.PREPROCESSED, e);
                }
            }

            if (!jobState.isProcessed()) {
                try {
                    TaskContext<T,?> taskCtxt;
                    while ((taskCtxt = ctxt.getNextExecutableTask()) != null) {
                        if(job.getBaseMeta().getState()== CouchbaseDocument.DocumentState.NEW){
                            ctxt.save();
                        }
                        taskCtxt.execute();
                    }
                    if(ctxt.getPendingTasks(true).size()>0){
                        throw new JobExecutionException(ctxt,"Remaning not executable tasks");
                    }
                    manageStateExecutionEnd(ctxt,State.PROCESSED,true);
                }
                catch(JobExecutionException e){
                    throw e;
                }
                catch (Throwable e) {
                    throw new JobExecutionException(job, State.PROCESSED, e);
                }
            }

            if (!jobState.isFinalized()) {
                try {
                    boolean saveAsked;
                    saveAsked=ctxt.getProcessingService().postprocess(ctxt);
                    manageStateExecutionEnd(ctxt,State.POSTPROCESSED,saveAsked);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.POSTPROCESSED, e);
                }
            }

            if (!jobState.isDone()) {
                try {
                    ctxt.getProcessingService().cleanup(ctxt);
                    manageStateExecutionEnd(ctxt,State.DONE,true);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.DONE, e);
                }
            }
        }
        catch(JobExecutionException e){
            jobState.setLastRunError("["+e.getClass().getSimpleName()+"] "+e.getMessage());
            throw e;
        }
        catch(Throwable e){
            jobState.setLastRunError("["+e.getClass().getSimpleName()+"] "+e.getMessage());
            throw new JobExecutionException(job,State.UNKNOWN,e);
        }
    }
}
