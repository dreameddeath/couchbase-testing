package com.dreameddeath.core.notification.listener.impl;

import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.SubmissionResult;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 30/05/2016.
 */
public abstract class AbstractLocalListener implements IEventListener{
    @Override
    public <T extends Event> Observable<SubmissionResult> submit(T event) {
        Notification result = new Notification();
        result.setListenerName(this.getName());
        return null;
    }

    public <T extends Event> void process(T event) {
        Notification result = new Notification();
        result.setListenerName(this.getName());
    }
}
