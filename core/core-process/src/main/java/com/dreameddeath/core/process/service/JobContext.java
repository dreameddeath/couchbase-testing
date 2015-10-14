/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.process.service;

import com.dreameddeath.core.dao.session.ICouchbaseSession;

/**
 * Created by Christophe Jeunesse on 23/11/2014.
 */
public class JobContext {
    private ICouchbaseSession session;
    private ExecutorServiceFactory executorFactory;
    private ProcessingServiceFactory processingFactory;

    public ICouchbaseSession getSession(){return session;}
    public void setSession(ICouchbaseSession session){ this.session=session;}

    public void setExecutorFactory(ExecutorServiceFactory factory){executorFactory = factory;}
    public ExecutorServiceFactory getExecutorFactory(){return executorFactory;}

    public void setProcessingFactory(ProcessingServiceFactory factory){processingFactory = factory;}
    public ProcessingServiceFactory getProcessingFactory(){return processingFactory;}

    public static JobContext newContext(ICouchbaseSession session, ExecutorServiceFactory execFactory,ProcessingServiceFactory processFactory){
        JobContext res = new JobContext();
        res.setSession(session);
        res.setExecutorFactory(execFactory);
        res.setProcessingFactory(processFactory);
        return res;
    }

    public static JobContext newContext(JobContext ctxt){
        JobContext res = new JobContext();
        res.setSession(ctxt.getSession());
        res.setExecutorFactory(ctxt.getExecutorFactory());
        res.setProcessingFactory(ctxt.getProcessingFactory());
        return res;
    }

}
