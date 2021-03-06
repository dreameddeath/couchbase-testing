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

package com.dreameddeath.core.process.service;

import com.dreameddeath.core.exception.process.JobExecutionException;
import com.dreameddeath.core.process.common.AbstractJob;
import com.dreameddeath.core.process.common.AbstractJob.State;
import com.dreameddeath.core.process.common.AbstractTask;


/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class BasicJobProcessingServiceImpl implements JobProcessingService<AbstractJob> {
    private ProcessingServiceFactory factory;

    public BasicJobProcessingServiceImpl(ProcessingServiceFactory factory){
        this.factory = factory;
    }

    public ProcessingServiceFactory getFactory(){
        return factory;
    }

    public void onSave(AbstractJob job,State state){}
    public void onEndProcessing(AbstractJob job,State state){}

    public void execute(AbstractJob job) throws JobExecutionException{
        //job.setProcessingService(this);
        job.setLastRunError(null);
        try {
            if (!job.isInitialized()) {
                try {
                    boolean saveAsked;
                    saveAsked=job.init();
                    job.setJobState(State.INITIALIZED);
                    if(saveAsked){
                        onSave(job, State.INITIALIZED);
                        job.getBaseMeta().getSession().save(job);
                    }
                    onEndProcessing(job, State.INITIALIZED);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.INITIALIZED, e);
                }
            }

            if (!job.isPrepared()) {
                try {
                    boolean saveAsked;
                    saveAsked=job.preprocess();
                    job.setJobState(State.PREPROCESSED);
                    if(saveAsked){
                        onSave(job, State.PREPROCESSED);
                        job.getBaseMeta().getSession().save(job);
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
                        getFactory().getTaskServiceForClass(AbstractTask.class).execute(task);
                    }
                    if(job.getPendingTasks().size()>0){
                        //TODO throw an error
                    }
                    onSave(job, State.PROCESSED);
                    job.setJobState(State.PROCESSED);
                    job.getBaseMeta().getSession().save(job);
                    onEndProcessing(job, State.PROCESSED);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.PROCESSED, e);
                }
            }

            if (!job.isFinalized()) {
                try {
                    boolean saveAsked;
                    saveAsked=job.postprocess();
                    job.setJobState(State.POSTPROCESSED);
                    if(saveAsked){
                        onSave(job, State.POSTPROCESSED);
                        job.getBaseMeta().getSession().save(job);
                    }
                    onEndProcessing(job, State.POSTPROCESSED);
                } catch (Throwable e) {
                    throw new JobExecutionException(job, State.POSTPROCESSED, e);
                }
            }

            if (!job.isDone()) {
                try {
                    job.cleanup();
                    job.setJobState(State.DONE);
                    onSave(job,State.DONE);
                    job.getBaseMeta().getSession().save(job);
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
        //job.setProcessingService(null);
    }
}
