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

import com.dreameddeath.core.process.exception.JobExecutionException;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.model.AbstractJob.State;
import com.dreameddeath.core.process.model.AbstractTask;
import com.dreameddeath.core.process.service.IJobExecutorService;
import com.dreameddeath.core.process.service.JobContext;
import com.dreameddeath.core.process.service.TaskContext;


/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class BasicJobExecutorServiceImpl implements IJobExecutorService<AbstractJob> {

    public void onSave(AbstractJob job,AbstractJob.State state){}
    public void onEndProcessing(AbstractJob job,State state){}

    @Override
    public void execute(JobContext ctxt,AbstractJob job) throws JobExecutionException{
        job.setLastRunError(null);
        try {
            if (!job.isInitialized()) {
                try {
                    boolean saveAsked;
                    saveAsked=ctxt.getProcessingFactory().init(ctxt,job);
                    job.setJobState(State.INITIALIZED);
                    if(saveAsked){
                        onSave(job, State.INITIALIZED);
                        ctxt.getSession().save(job);
                    }
                    onEndProcessing(job, State.INITIALIZED);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.INITIALIZED, e);
                }
            }

            if (!job.isPrepared()) {
                try {
                    boolean saveAsked;
                    saveAsked=ctxt.getProcessingFactory().preprocess(ctxt,job);
                    job.setJobState(State.PREPROCESSED);
                    if(saveAsked){
                        onSave(job, State.PREPROCESSED);
                        ctxt.getSession().save(job);
                    }
                    onEndProcessing(job, State.PREPROCESSED);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.PREPROCESSED, e);
                }
            }

            if (!job.isProcessed()) {
                try {
                    AbstractTask task;
                    while ((task = job.getNextExecutableTask()) != null) {
                        task.setLastRunError(null);
                        ctxt.getExecutorFactory().execute(TaskContext.newContext(ctxt), task);
                    }
                    if(job.getPendingTasks().size()>0){
                        //TODO throw an error
                    }
                    onSave(job, State.PROCESSED);
                    job.setJobState(State.PROCESSED);
                    ctxt.getSession().save(job);
                    onEndProcessing(job, State.PROCESSED);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.PROCESSED, e);
                }
            }

            if (!job.isFinalized()) {
                try {
                    boolean saveAsked;
                    saveAsked=ctxt.getProcessingFactory().postprocess(ctxt,job);
                    job.setJobState(State.POSTPROCESSED);
                    if(saveAsked){
                        onSave(job, State.POSTPROCESSED);
                        ctxt.getSession().save(job);
                    }
                    onEndProcessing(job, State.POSTPROCESSED);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.POSTPROCESSED, e);
                }
            }

            if (!job.isDone()) {
                try {
                    ctxt.getProcessingFactory().cleanup(ctxt, job);
                    job.setJobState(State.DONE);
                    onSave(job,State.DONE);
                    ctxt.getSession().save(job);
                    onEndProcessing(job,State.DONE);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.DONE, e);
                }
            }
        }
        catch(JobExecutionException e){
            job.setLastRunError("["+e.getClass().getSimpleName()+"] "+e.getMessage());
            throw e;
        }
        catch(Throwable e){
            job.setLastRunError("["+e.getClass().getSimpleName()+"] "+e.getMessage());
            throw new JobExecutionException(job,State.UNKNOWN,e);
        }
    }
}
