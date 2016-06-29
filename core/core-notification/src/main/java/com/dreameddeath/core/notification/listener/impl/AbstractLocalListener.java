package com.dreameddeath.core.notification.listener.impl;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.SubmissionResult;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.user.AnonymousUser;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 30/05/2016.
 */
public abstract class AbstractLocalListener implements IEventListener{
    private ICouchbaseSessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(ICouchbaseSessionFactory factory){
        this.sessionFactory = factory;
    }

    @Override
    public <T extends Event> Observable<SubmissionResult> submit(final Notification sourceNotif, final T event) {
        final ICouchbaseSession session = sessionFactory.newSession(ICouchbaseSession.SessionType.READ_WRITE, AnonymousUser.INSTANCE);
        if(sourceNotif.getStatus().equals(Notification.Status.SUBMITTED)|| sourceNotif.getStatus().equals(Notification.Status.CANCELLED)){
            return Observable.just(new SubmissionResult(sourceNotif,false));
        }

        return process(event,session)
                .map(boolRes->{
                    sourceNotif.setStatus(Notification.Status.SUBMITTED);
                    return sourceNotif;
                })
                .flatMap(session::asyncSave)
                .map(notif-> new SubmissionResult(notif,true))
                .onErrorResumeNext(throwable ->
                        Observable.just(new SubmissionResult(sourceNotif,throwable))
                );
    }

    protected  abstract <T extends Event> Observable<Boolean> process(T event,ICouchbaseSession session);

}
