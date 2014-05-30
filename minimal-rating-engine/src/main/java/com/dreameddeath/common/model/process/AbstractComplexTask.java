package com.dreameddeath.common.model.process;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.property.ArrayListProperty;


import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by ceaj8230 on 21/05/2014.
 */
public class AbstractComplexTask extends AbstractDocumentUpdateTask {
    @DocumentProperty("tasks")
    private List<String> _taskIdList = new ArrayListProperty<String>(AbstractComplexTask.this);

    public List<String> getTasks() { return Collections.unmodifiableList(_taskIdList);}
    public void setTasks(Collection<String> taskIds) {
        _taskIdList.clear();
        _taskIdList.addAll(taskIds);
    }

    public void addTask(String taskId){
        if(_taskIdList.contains(taskId)){
            ///TODO throw an error
        }
        if(getParentJob().getTask(taskId)!=null){
            ///TODO throw
        }
        _taskIdList.add(taskId);

    }


}
