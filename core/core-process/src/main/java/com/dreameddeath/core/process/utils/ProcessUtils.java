/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.process.utils;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.v2.DocumentState;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.listener.impl.AbstractNotificationProcessor;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.service.context.JobContext;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 03/01/2016.
 */
public class ProcessUtils {

    public static <TJOB extends AbstractJob> Single<TJOB> asyncLoadJob(ICouchbaseSession session, String uid, Class<TJOB> jobClass){
        return session.asyncGetFromKeyParams(jobClass,uid);
    }

    public static <TTASK extends AbstractTask> Single<TTASK> asyncLoadTask(ICouchbaseSession session, AbstractJob job, int taskId,Class<TTASK> taskClass){
        return session.asyncGetFromKeyParams(taskClass,job.getUid(),taskId);
    }

    public static <TJOB extends AbstractJob>  TJOB loadJob(ICouchbaseSession session,String uid, Class<TJOB> jobClass) throws DaoException,StorageException{
        return session.toBlocking().blockingGetFromKeyParams(jobClass,uid);
    }

    public static <TTASK extends AbstractTask>  TTASK loadTask(ICouchbaseSession session, AbstractJob job, int taskId,Class<TTASK> taskClass) throws DaoException,StorageException{
        return session.toBlocking().blockingGetFromKeyParams(taskClass,job.getUid(),taskId);
    }

    public static <TJOB extends AbstractJob> Single<AbstractNotificationProcessor.ProcessingResult> mapJobResultToNotificationProcessingResult(JobContext<TJOB> context){
        ProcessState.State state = context.getJobState().getState();
        if(state==null || state== ProcessState.State.UNKNOWN || context.getInternalJob().getMeta().getState().equals(DocumentState.NEW)){
            Single.error(new IllegalStateException("The id "+context.getJobId()+" of type "+context.getInternalJob().getClass()+" in an inconsistent state"));
        }
        switch (context.getJobState().getState()){
            case DONE:
                return Single.just(AbstractNotificationProcessor.ProcessingResult.PROCESSED);
            case NEW:
            case ASYNC_NEW:
                return Single.just(AbstractNotificationProcessor.ProcessingResult.DEFERRED);
            case CANCELLED:
                return Single.just(AbstractNotificationProcessor.ProcessingResult.ABORTED);
            default:
                return Single.just(AbstractNotificationProcessor.ProcessingResult.SUBMITTED);
        }
    }

    public static <TJOB extends AbstractJob, TEVT extends IEvent> void setJobRequestId(TJOB job,TEVT event, String listenerName){
        job.setRequestUid(String.join("#","Notification",listenerName,event.getId().toString()));
    }
}
