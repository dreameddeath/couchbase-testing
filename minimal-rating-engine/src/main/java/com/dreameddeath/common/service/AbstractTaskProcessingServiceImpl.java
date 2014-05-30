package com.dreameddeath.common.service;

import com.dreameddeath.common.model.process.AbstractTask;
import com.dreameddeath.common.model.process.AbstractTask.State;
/**
 * Created by ceaj8230 on 21/05/2014.
 */
public abstract class AbstractTaskProcessingServiceImpl<T extends AbstractTask> implements TaskProcessingService<T> {
    public abstract void init(T task);
    public void preprocess(T task){}
    public void postprocess(T task){}
    public abstract void cleanup(T task);
    public abstract void process(T task);

    public void execute(T task) {
        if(!task.isInitialized()){
            init(task);
            task.setState(State.INITIALIZED);
            task.getParentJob().save();
        }

        if(!task.isPrepared()){
            preprocess(task);
            task.setState(State.PREPROCESSED);
            task.getParentJob().save();
        }

        if(!task.isProcessed()){
            process(task);
            task.setState(State.PROCESSED);
            task.getParentJob().save();
        }

        if(!task.isFinalized()){
            postprocess(task);
            task.setState(State.POSTPROCESSED);
            task.getParentJob().save();
        }

        if(!task.isDone()){
            cleanup(task);
            task.setState(State.DONE);
            task.getParentJob().save();
        }
    }
}
