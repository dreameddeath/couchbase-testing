package com.dreameddeath.common.service;

import com.dreameddeath.common.model.process.AbstractTask;
import com.dreameddeath.common.model.process.AbstractTask.State;
/**
 * Created by ceaj8230 on 21/05/2014.
 */
public class TaskProcessingServiceImpl<T extends AbstractTask> implements TaskProcessingService<T> {

    public void execute(T task) {
        if(!task.isInitialized()){
            task.init();
            task.setState(State.INITIALIZED);
            task.getParentJob().save();
        }

        if(!task.isPrepared()){
            task.preprocess();
            task.setState(State.PREPROCESSED);
            task.getParentJob().save();
        }

        if(!task.isProcessed()){
            task.process();
            task.setState(State.PROCESSED);
            task.getParentJob().save();
        }

        if(!task.isFinalized()){
            task.postprocess();
            task.setState(State.POSTPROCESSED);
            task.getParentJob().save();
        }

        if(!task.isDone()){
            task.cleanup();
            task.setState(State.DONE);
            task.getParentJob().save();
        }
    }

}
