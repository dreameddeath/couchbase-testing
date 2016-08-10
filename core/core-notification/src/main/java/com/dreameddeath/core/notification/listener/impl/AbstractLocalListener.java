package com.dreameddeath.core.notification.listener.impl;

import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.SubmissionResult;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 30/05/2016.
 */
public abstract class AbstractLocalListener  extends AbstractNotificationProcessor implements IEventListener{
    @Override
    public <T extends Event> Observable<SubmissionResult> submit(final Notification sourceNotif, final T event) {
        return process(sourceNotif,event);
    }
}
