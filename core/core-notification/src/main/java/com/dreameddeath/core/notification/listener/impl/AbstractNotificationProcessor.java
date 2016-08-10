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

    @Autowired
    public void setDefaultSessionUser(IUser user){
        defaultSessionUser = user;
    }


    public Observable<SubmissionResult> process(final String sourceNotifId) {
        final ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE, defaultSessionUser);
        return process(sourceNotifId,session);
    }


    public  Observable<SubmissionResult> process(final String sourceNotifId,final ICouchbaseSession session) {
        return session.asyncGet(sourceNotifId,Notification.class)
                .flatMap(notification -> {
                    if(notification.getStatus()== Notification.Status.SUBMITTED || notification.getStatus()== Notification.Status.CANCELLED){
                        return Observable.just(new SubmissionResult(notification,notification.getStatus()== Notification.Status.SUBMITTED));
                    }
                    else{
                        return session.asyncGetFromUID(notification.getEventId().toString(),Event.class)
                                .flatMap(event-> process(notification,event,session));
                    }
                });
    }

    public <T extends Event> Observable<SubmissionResult> process(final Notification sourceNotif, final T event){
        final ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE, defaultSessionUser);
        return this.process(sourceNotif,event,session);
    }

    public <T extends Event> Observable<SubmissionResult> process(final Notification sourceNotif, final T event,final ICouchbaseSession session) {
        Preconditions.checkArgument(!sourceNotif.getStatus().equals(Notification.Status.SUBMITTED) && !sourceNotif.getStatus().equals(Notification.Status.CANCELLED),
                "Bad Status %s  for notif %s/%s. The listener name is[%s]",
                sourceNotif.getStatus(),
                sourceNotif.getEventId(),
                sourceNotif.getId(),
                sourceNotif.getListenerName()
        );

        return doProcess(event,sourceNotif,session)
                .map(boolRes->{
                    sourceNotif.setStatus(Notification.Status.SUBMITTED);
                    sourceNotif.incNbAttempts();
                    return sourceNotif;
                })
                .flatMap(session::asyncSave)
                .map(notif-> new SubmissionResult(notif,true))
                .onErrorResumeNext(throwable ->
                        Observable.just(new SubmissionResult(sourceNotif,throwable))
                );
    }

    protected  abstract <T extends Event> Observable<Boolean> doProcess(T event,Notification notification,ICouchbaseSession session);

}
