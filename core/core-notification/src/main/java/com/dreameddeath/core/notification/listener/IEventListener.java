package com.dreameddeath.core.notification.listener;

import com.dreameddeath.core.notification.model.v1.Event;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public interface IEventListener {
    String getName();
    String getType();
    <T extends Event> Observable<SubmissionResult> submit(T event);
}
