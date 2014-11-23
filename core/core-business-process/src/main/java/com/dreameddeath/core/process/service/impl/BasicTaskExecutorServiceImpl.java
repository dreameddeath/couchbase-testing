package com.dreameddeath.core.process.service.impl;

import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.model.process.AbstractJob;
import com.dreameddeath.core.model.process.AbstractTask;
import com.dreameddeath.core.model.process.AbstractTask.State;
import com.dreameddeath.core.model.process.SubJobProcessTask;
import com.dreameddeath.core.process.service.*;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public class BasicTaskExecutorServiceImpl implements ITaskExecutorService<AbstractTask> {
    public void onSave(AbstractTask task, State state){}
    public void onEndProcessing(AbstractTask task, State state){}

    @Override
    public void execute(TaskContext ctxt,AbstractTask task) throws TaskExecutionException {
        task.setLastRunError(null);
        try {
            if (!task.isInitialized()) {
                try {
                    boolean saveAsked;
                    saveAsked = ctxt.getProcessingFactory().init(ctxt, task);
                    task.setState(State.INITIALIZED);

                    if (saveAsked) {
                        onSave(task, State.INITIALIZED);
                        ctxt.getSession().save(task.getParentJob());
                    }
                    onEndProcessing(task, State.INITIALIZED);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.INITIALIZED, e);
                }
            }

            if (!task.isPrepared()) {
                try {
                    boolean saveAsked;
                    saveAsked =ctxt.getProcessingFactory().preprocess(ctxt,task);
                    task.setState(State.PREPROCESSED);

                    if(saveAsked){
                        onSave(task, State.PREPROCESSED);
                        ctxt.getSession().save(task.getParentJob());
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
                        AbstractJob job = ctxt.getSession().getFromUID(subJobTask.getUid(), AbstractJob.class);
                        if (!job.isDone()) {
                            ctxt.getExecutorFactory().execute(JobContext.newContext(ctxt.getJobContext()), job);
                        }
                        saveAsked=true;
                    }
                    else {
                        saveAsked = ctxt.getProcessingFactory().process(ctxt,task);
                    }
                    task.setState(State.PROCESSED);
                    if(saveAsked){
                        onSave(task, State.PROCESSED);
                        ctxt.getSession().save(task.getParentJob());
                    }
                    onEndProcessing(task, State.PROCESSED);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.PROCESSED, e);
                }
            }

            if (!task.isFinalized()) {
                try {
                    boolean saveAsked;
                    saveAsked=ctxt.getProcessingFactory().postprocess(ctxt,task);
                    task.setState(State.POSTPROCESSED);
                    if(saveAsked){
                        onSave(task, State.POSTPROCESSED);
                        ctxt.getSession().save(task.getParentJob());
                    }
                    onEndProcessing(task, State.POSTPROCESSED);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.POSTPROCESSED, e);
                }
            }

            if (!task.isDone()) {
                try {
                    ctxt.getProcessingFactory().cleanup(ctxt,task);
                    task.setState(State.DONE);
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
