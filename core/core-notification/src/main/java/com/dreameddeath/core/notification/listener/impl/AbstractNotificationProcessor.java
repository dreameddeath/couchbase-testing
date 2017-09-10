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

package com.dreameddeath.core.notification.listener.impl;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.listener.SubmissionResult;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.user.AnonymousUser;
import com.dreameddeath.core.user.IUser;
import com.google.common.base.Preconditions;
import io.reactivex.Single;
import org.springframework.beans.factory.annotation.Autowired;

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

    public Single<SubmissionResult> processIfNeeded(final String domain, final String sourceNotifKey) {
        final ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE,domain,defaultSessionUser);
        return session.asyncGet(sourceNotifKey,Notification.class)
                .flatMap(notification -> session.asyncGetFromUID(notification.getEventId().toString(),Event.class)
                        .map(event -> new NotificationAndEvent(event, notification)))
                .flatMap(notificationAndEvent -> processIfNeeded(notificationAndEvent.notification,notificationAndEvent.event,session));
    }

    private boolean needProcessing(Notification notification){
        return notification.getStatus()!= Notification.Status.PROCESSED && notification.getStatus()!= Notification.Status.CANCELLED;
    }

    private Single<SubmissionResult> buildNotificationResult(Notification notification){
        return Single.just(new SubmissionResult(notification,notification.getStatus()== Notification.Status.PROCESSED));
    }

    public <T extends IEvent> Single<SubmissionResult> processIfNeeded(final String domain,final String sourceNotifKey,final T providedEvent) {
        final ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE,domain, defaultSessionUser);

        return session.asyncGet(sourceNotifKey,Notification.class)
                .flatMap(notification -> processIfNeeded(notification,providedEvent,session));
    }

    public <T extends IEvent> Single<SubmissionResult> processIfNeeded(final Notification sourceNotif, final T event){
        final ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE,sourceNotif.getDomain(), defaultSessionUser);
        return this.processIfNeeded(sourceNotif,event,session);
    }

    protected  <T extends IEvent> Single<SubmissionResult> processIfNeeded(final Notification sourceNotif,final T providedEvent,final ICouchbaseSession session) {
        if(!needProcessing(sourceNotif)){
            return buildNotificationResult(sourceNotif);
        }
        return process(sourceNotif,providedEvent,session);
    }

    public <T extends IEvent> Single<SubmissionResult> processIfNeeded(ICouchbaseSession session, final Notification sourceNotif, final T event){
        if(!needProcessing(sourceNotif)){
            return buildNotificationResult(sourceNotif);
        }

        return this.process(sourceNotif,event,session);
    }


    protected void incrementAttemptsManagement(final Notification sourceNotif) {
        sourceNotif.incNbAttempts();
    }

    protected  <T extends IEvent> Single<SubmissionResult> process(final Notification sourceNotif, final T event,final ICouchbaseSession session) {
        Preconditions.checkState(!sourceNotif.getStatus().equals(Notification.Status.PROCESSED) && !sourceNotif.getStatus().equals(Notification.Status.CANCELLED),
                "Bad Status %s  for notif %s/%s. The listener name is[%s]",
                sourceNotif.getStatus(),
                sourceNotif.getEventId(),
                sourceNotif.getId(),
                sourceNotif.getListenerLink().getName()
        );
        incrementAttemptsManagement(sourceNotif);
        return doProcess(event,sourceNotif,session)
                .map(processingResultInfo->{
                    if(!processingResultInfo.isRemote()) {
                        if (processingResultInfo.getResult()== ProcessingResult.SUBMITTED) {
                            processingResultInfo.getNotification().setStatus(Notification.Status.SUBMITTED);
                        } else if (processingResultInfo.getResult() == ProcessingResult.DEFERRED) {
                            processingResultInfo.getNotification().setStatus(Notification.Status.DEFERRED);
                        } else if (processingResultInfo.getResult() == ProcessingResult.PROCESSED) {
                            processingResultInfo.getNotification().setStatus(Notification.Status.PROCESSED);
                        }
                    }
                    return processingResultInfo.getNotification();
                })
                .flatMap(session::asyncSave)
                .map(notif -> new SubmissionResult(notif,notif.getStatus()== Notification.Status.PROCESSED))
                .onErrorResumeNext(throwable -> Single.just(new SubmissionResult(sourceNotif, throwable)));
    }

    protected  abstract <T extends IEvent> Single<ProcessingResultInfo> doProcess(T event,Notification notification,ICouchbaseSession session);


    public static class ProcessingResultInfo{
        private final Notification notification;
        private final boolean isRemote;
        private final ProcessingResult result;

        public ProcessingResultInfo(Notification notification, boolean isRemote, ProcessingResult result) {
            this.notification = notification;
            this.isRemote = isRemote;
            this.result = result;
        }

        public Notification getNotification() {
            return notification;
        }

        public boolean isRemote() {
            return isRemote;
        }

        public ProcessingResult getResult() {
            return result;
        }

        public static Single<ProcessingResultInfo> buildSingle(Notification notification, boolean isRemote, ProcessingResult result){
            return Single.just(new ProcessingResultInfo(notification,isRemote,result));
        }

        public static ProcessingResultInfo build(Notification notification, boolean isRemote,ProcessingResult result){
            return new ProcessingResultInfo(notification,isRemote,result);
        }
    }

    public enum ProcessingResult{
        PROCESSED,
        SUBMITTED,
        DEFERRED
    }


    private final class NotificationAndEvent{
        private final IEvent event;
        private final Notification notification;


        public NotificationAndEvent(IEvent event, Notification notification) {
            this.event = event;
            this.notification = notification;
        }
    }
}
