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

import com.dreameddeath.core.event.TaskProcessEvent;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.process.common.AbstractJob;
import com.dreameddeath.core.process.common.AbstractTask;
import com.dreameddeath.core.process.common.AbstractTask.State;
import com.dreameddeath.core.process.common.SubJobProcessTask;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class BasicTaskProcessingServiceImpl implements TaskProcessingService<AbstractTask> {
    private ProcessingServiceFactory _factory;

    public BasicTaskProcessingServiceImpl(ProcessingServiceFactory factory){
        _factory = factory;
    }

    public ProcessingServiceFactory getFactory(){
        return _factory;
    }

    public void onSave(AbstractTask task, State state){}
    public void onEndProcessing(AbstractTask task, State state){}


    public void execute(AbstractTask task) throws TaskExecutionException {
        task.setLastRunError(null);
        try {
            if (!task.isInitialized()) {
                try {
                    boolean saveAsked;
                    saveAsked = task.init();
                    task.setState(State.INITIALIZED);

                    if (saveAsked) {
                        onSave(task, State.INITIALIZED);
                        task.getParentJob().getBaseMeta().getSession().save(task.getParentJob());
                    }
                    onEndProcessing(task, State.INITIALIZED);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.INITIALIZED, e);
                }
            }

            if (!task.isPrepared()) {
                try {
                    boolean saveAsked;
                    saveAsked =task.preprocess();
                    task.setState(State.PREPROCESSED);

                    if(saveAsked){
                        onSave(task, State.PREPROCESSED);
                        task.getParentJob().getBaseMeta().getSession().save(task.getParentJob());
                    }
                    onEndProcessing(task, State.PREPROCESSED);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.PREPROCESSED, e);
                }
            }

            if (!task.isProcessed()) {
                try {
                    boolean saveAsked;
                    if(task instanceof SubJobProcessTask){
                        SubJobProcessTask subJobTask = (SubJobProcessTask)task;
                        AbstractJob job = subJobTask.getJob();
                        if (!job.isDone()) {
                            JobProcessingService processingService = getFactory().getJobServiceForClass(job.getClass());
                            processingService.execute(job);
                        }
                        saveAsked=true;
                    }
                    else {
                        saveAsked = task.process();
                    }
                    task.setState(State.PROCESSED);
                    if(saveAsked){
                        onSave(task, State.PROCESSED);
                        task.getParentJob().getBaseMeta().getSession().save(task.getParentJob());
                    }
                    onEndProcessing(task, State.PROCESSED);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.PROCESSED, e);
                }
            }

            if (!task.isFinalized()) {
                try {
                    boolean saveAsked;
                    saveAsked=task.postprocess();
                    task.setState(State.POSTPROCESSED);
                    if(saveAsked){
                        onSave(task, State.POSTPROCESSED);
                        task.getParentJob().getBaseMeta().getSession().save(task.getParentJob());
                    }
                    onEndProcessing(task, State.POSTPROCESSED);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.POSTPROCESSED, e);
                }
            }

            if (!task.isDone()) {
                try {
                    task.cleanup();
                    task.setState(State.DONE);
                    if (task.getParentJob().when(new TaskProcessEvent(task))) {
                        onSave(task, State.DONE);
                        task.getParentJob().getBaseMeta().getSession().save(task.getParentJob());//Save has it has been requested by the "when"
                    }
                    onEndProcessing(task, State.DONE);
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
