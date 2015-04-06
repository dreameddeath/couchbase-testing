/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.process.service;

import com.dreameddeath.core.session.ICouchbaseSession;

/**
 * Created by ceaj8230 on 23/11/2014.
 */
public class TaskContext {
    private JobContext _jobContext;
    public JobContext getJobContext(){return _jobContext;}
    public void setJobContext(JobContext context){_jobContext=context;}

    public ICouchbaseSession getSession(){return _jobContext.getSession();}

    public ExecutorServiceFactory getExecutorFactory(){return _jobContext.getExecutorFactory();}
    public ProcessingServiceFactory getProcessingFactory(){return _jobContext.getProcessingFactory();}

    public static TaskContext newContext(JobContext ctxt){
        TaskContext res = new TaskContext();
        res.setJobContext(ctxt);
        return res;
    }

}
