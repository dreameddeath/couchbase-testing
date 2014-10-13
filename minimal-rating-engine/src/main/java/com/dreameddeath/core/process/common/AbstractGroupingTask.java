package com.dreameddeath.core.process.common;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.Validate;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by CEAJ8230 on 25/09/2014.
 */
public class AbstractGroupingTask extends AbstractTask {
    /**
     *  subTasks : List of children tasks
     */
    @DocumentProperty("subTasks") @Validate
    private ListProperty<AbstractTask> _subTasks = new ArrayListProperty<AbstractTask>(AbstractGroupingTask.this);

    // SubTasks Accessors
    public List<AbstractTask> getSubTasks() { return _subTasks.get(); }
    public void setSubTasks(Collection<AbstractTask> vals) { _subTasks.set(vals); }
    public boolean addSubTasks(AbstractTask val){ return _subTasks.add(val); }
    public boolean removeSubTasks(AbstractTask val){ return _subTasks.remove(val); }
    public AbstractTask getSubTask(String id){
        for(AbstractTask task :_subTasks){
            if(id.equals(task.getUid())){ return task; }
        }
        return null;
    }
    public AbstractTask getSubTask(Integer pos) {
        return _subTasks.get(pos);
    }
    public <T extends AbstractTask> T getSubTask(Integer pos,Class<T> clazz) {return (T)getSubTask(pos);}
    public <T extends AbstractTask> T getSubTask(String id, Class<T>clazz) {
        return (T)getSubTask(id);
    }

    public String buildTaskId(AbstractTask task){
        return String.format("%010d", _subTasks.size());
    }


    public List<AbstractTask> getPendingTasks() {
        List<AbstractTask> pendingTasks=new ArrayList<AbstractTask>();
        for(AbstractTask task:_subTasks){
            if(! task.isDone()){
                pendingTasks.add(task);
            }
        }
        return pendingTasks;
    }

    public AbstractTask getNextExecutableTask(){
        for(AbstractTask task:_subTasks){
            if(! task.isDone()){
                List<String> preRequisiteList = new ArrayList<String>();
                for(String uid:task.getDependency()){
                    if(!getSubTask(uid).isDone()){
                        preRequisiteList.add(uid);
                    }
                }
                if(preRequisiteList.size()==0){
                    return task;
                }
            }
        }
        return null;
    }

    @Override
    final public boolean process(){return false;}
}
