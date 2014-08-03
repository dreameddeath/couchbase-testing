package com.dreameddeath.common.process;

import com.dreameddeath.common.event.TaskProcessEvent;
import com.dreameddeath.common.model.process.AbstractTask;
import com.dreameddeath.common.model.process.AbstractTask.State;
/**
 * Created by ceaj8230 on 21/05/2014.
 */
public class BasicTaskProcessingServiceImpl implements TaskProcessingService<AbstractTask> {
    private ProcessingServiceFactory _factory;

    public BasicTaskProcessingServiceImpl(ProcessingServiceFactory factory){
        _factory = factory;
    }

    public ProcessingServiceFactory getFactory(){
        return _factory;
    }

    public void execute(AbstractTask task) {
        task.setProcessingService(this);
        if(!task.isInitialized()){
            if(task.init()) {
                task.setState(State.INITIALIZED);
                task.getParentJob().save();
            }
        }

        if(!task.isPrepared()){
            if(task.preprocess()) {
                task.setState(State.PREPROCESSED);
                task.getParentJob().save();
            }
        }

        if(!task.isProcessed()){
            if(task.process()) {
                task.setState(State.PROCESSED);
                task.getParentJob().save();
            }
        }

        if(!task.isFinalized()){
            if(task.postprocess()) {
                task.setState(State.POSTPROCESSED);
                task.getParentJob().save();
            }
        }

        if(!task.isDone()){
            task.cleanup();
            task.setState(State.DONE);
            if(task.getParentJob().when(new TaskProcessEvent(task))){
                task.getParentJob().save();//Save has it has been requested by the "when"
            }
        }
        task.setProcessingService(null);
    }

}
