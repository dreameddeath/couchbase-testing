package com.dreameddeath.common.service;

import com.dreameddeath.common.model.process.AbstractJob;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ceaj8230 on 26/05/2014.
 */
public class JobProcessingServiceFactory {
    private Map<Class<? extends AbstractJob>, JobProcessingService<?>> _jobServiceMap
            = new HashMap<Class<? extends AbstractJob>, JobProcessingService<?>>();

    public <T extends AbstractJob> void addService(Class<T> entityClass,JobProcessingService<T> service){
        _jobServiceMap.put(entityClass,service);
    }

    protected <T extends AbstractJob> JobProcessingService<T> getService(Class<T> clazz){
        JobProcessingService<T> result = (JobProcessingService<T>)_jobServiceMap.get(clazz);
        if(result==null){
            Class parentClass=clazz.getSuperclass();
            if(AbstractJob.class.isAssignableFrom(parentClass)){
                result = getService(parentClass.asSubclass(AbstractJob.class));
                if(result!=null){
                    _jobServiceMap.put(clazz,result);
                }
            }
        }
        ///TODO throw an error if null
        return result;
    }

    public <T extends AbstractJob> void execute(T job, Class<T> clazz){
        getService(clazz).execute(job);
    }

}
