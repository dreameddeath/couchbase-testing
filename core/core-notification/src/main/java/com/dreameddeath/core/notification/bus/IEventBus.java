package com.dreameddeath.core.notification.bus;

import com.dreameddeath.core.notification.model.v1.Event;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public interface IEventBus {
    <T extends Event> Observable<EventFireResult<T>> asyncFireEvent(T event);
    <T extends Event> Observable<EventFireResult<T>> asyncFireEvent(Observable<T> event);
    <T extends Event> EventFireResult<T> fireEvent(T event);
}
