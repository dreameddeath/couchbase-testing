package com.dreameddeath.common.service;

import com.dreameddeath.common.model.process.AbstractTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ceaj8230 on 26/05/2014.
 */
public class TaskProcessingServiceFactory {
    private Map<Class<? extends AbstractTask>, TaskProcessingService<?>> _taskServiceMap
            = new HashMap<Class<? extends AbstractTask>, TaskProcessingService<?>>();

    public <T extends AbstractTask> void addService(Class<T> entityClass,TaskProcessingService<T> service){
        _taskServiceMap.put(entityClass,service);
    }

    protected <T extends AbstractTask> TaskProcessingService<T> getService(Class<T> clazz){
        TaskProcessingService<T> result = (TaskProcessingService<T>)_taskServiceMap.get(clazz);
        if(result==null){
            Class parentClass=clazz.getSuperclass();
            if(AbstractTask.class.isAssignableFrom(parentClass)){
                result = getService(parentClass.asSubclass(AbstractTask.class));
                if(result!=null){
                    _taskServiceMap.put(clazz,result);
                }
            }
        }
        ///TODO throw an error if null
        return result;
    }

    public <T extends AbstractTask> void execute(T task, Class<T> clazz){
        getService(clazz).execute(task);
    }
}
