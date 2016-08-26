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

package com.dreameddeath.core.notification.listener.impl;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.notification.listener.SubmissionResult;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.core.user.IUser;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 20/07/2016.
 */
public abstract class AbstractNotificationProcessor {
    private ICouchbaseSessionFactory sessionFactory;
    private IUser defaultSessionUser = AnonymousUser.INSTANCE;

    @Autowired
    public void setSessionFactory(ICouchbaseSessionFactory factory){
        this.sessionFactory = factory;
    }

    @Autowired(required = false)
    public void setDefaultSessionUser(IUser user){
        defaultSessionUser = user;
    }


    public Observable<SubmissionResult> process(final String sourceNotifId) {
        final ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE, defaultSessionUser);
        return process(sourceNotifId,session);
    }

    private boolean needProcessing(Notification notification){
        return notification.getStatus()!= Notification.Status.PROCESSED && notification.getStatus()!= Notification.Status.CANCELLED;
    }

    private Observable<SubmissionResult> buildNotificationResult(Notification notification){
        return Observable.just(new SubmissionResult(notification,notification.getStatus()== Notification.Status.PROCESSED));
    }

    public  Observable<SubmissionResult> process(final String sourceNotifId,final ICouchbaseSession session) {
        return session.asyncGet(sourceNotifId,Notification.class)
                .flatMap(notification -> {
                    if(!needProcessing(notification)){
                        return buildNotificationResult(notification);
                    }

                    return session.asyncGetFromUID(notification.getEventId().toString(),Event.class)
                                .flatMap(event-> process(notification,event,session));

                });
    }

    public <T extends Event> Observable<SubmissionResult> process(final Notification sourceNotif, final T event){
        if(!needProcessing(sourceNotif)){
            return buildNotificationResult(sourceNotif);
        }
        final ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE, defaultSessionUser);

        return this.process(sourceNotif,event,session);
    }

    protected  <T extends Event> Observable<SubmissionResult> process(final Notification sourceNotif, final T event,final ICouchbaseSession session) {
        Preconditions.checkState(!sourceNotif.getStatus().equals(Notification.Status.PROCESSED) && !sourceNotif.getStatus().equals(Notification.Status.CANCELLED),
                "Bad Status %s  for notif %s/%s. The listener name is[%s]",
                sourceNotif.getStatus(),
                sourceNotif.getEventId(),
                sourceNotif.getId(),
                sourceNotif.getListenerName()
        );

        return doProcess(event,sourceNotif,session)
                .map(processingResult->{
                    if(processingResult==ProcessingResult.SUBMITTED) {
                        sourceNotif.setStatus(Notification.Status.SUBMITTED);
                    }
                    else if(processingResult==ProcessingResult.DEFERRED){
                        sourceNotif.setStatus(Notification.Status.DEFERRED);
                    }
                    else if(processingResult==ProcessingResult.PROCESSED) {
                        sourceNotif.setStatus(Notification.Status.PROCESSED);
                    }
                    sourceNotif.incNbAttempts();
                    return sourceNotif;
                })
                .flatMap(session::asyncSave)
                .map(notif-> new SubmissionResult(notif,true))
                .onErrorResumeNext(throwable ->
                        Observable.just(new SubmissionResult(sourceNotif,throwable))
                );
    }

    protected  abstract <T extends Event> Observable<ProcessingResult> doProcess(T event,Notification notification,ICouchbaseSession session);

    public enum ProcessingResult{
        PROCESSED,
        SUBMITTED,
        DEFERRED
    }

}
