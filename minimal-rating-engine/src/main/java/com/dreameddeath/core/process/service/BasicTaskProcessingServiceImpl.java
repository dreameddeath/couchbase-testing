package com.dreameddeath.core.process.service;

import com.dreameddeath.core.event.TaskProcessEvent;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.process.common.AbstractTask;
import com.dreameddeath.core.process.common.AbstractTask.State;
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
        task.setProcessingService(this);
        task.setLastRunError(null);
        try {
            if (!task.isInitialized()) {
                try {
                    if (task.init()) {
                        task.setState(State.INITIALIZED);
                        onSave(task, State.INITIALIZED);
                        task.getParentJob().save();
                    }
                    onEndProcessing(task, State.INITIALIZED);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.INITIALIZED, e);
                }
            }

            if (!task.isPrepared()) {
                try {
                    if (task.preprocess()) {
                        task.setState(State.PREPROCESSED);
                        onSave(task, State.PREPROCESSED);
                        task.getParentJob().save();
                    }
                    onEndProcessing(task, State.PREPROCESSED);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.PREPROCESSED, e);
                }
            }

            if (!task.isProcessed()) {
                try {
                    if (task.process()) {
                        task.setState(State.PROCESSED);
                        onSave(task, State.PROCESSED);
                        task.getParentJob().save();
                    }
                    onEndProcessing(task, State.PROCESSED);
                } catch (Throwable e) {
                    throw new TaskExecutionException(task, State.PROCESSED, e);
                }
            }

            if (!task.isFinalized()) {
                try {
                    if (task.postprocess()) {
                        task.setState(State.POSTPROCESSED);
                        onSave(task, State.POSTPROCESSED);
                        task.getParentJob().save();
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
                        task.getParentJob().save();//Save has it has been requested by the "when"
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

        task.setProcessingService(null);
    }

}
