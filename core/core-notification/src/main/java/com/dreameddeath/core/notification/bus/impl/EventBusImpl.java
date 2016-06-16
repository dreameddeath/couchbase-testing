package com.dreameddeath.core.notification.bus.impl;

import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.dispatch.IDispatcher;
import com.dreameddeath.core.notification.model.v1.Event;
import rx.Observable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class EventBusImpl implements IEventBus {
    private List<IDispatcher> dispatchers = new CopyOnWriteArrayList<>();

    public void addDispatcher(IDispatcher dispatcher){
        dispatchers.add(dispatcher);
    }

    @Override
    public <T extends Event> Observable<EventFireResult<T>> asyncFireEvent(T event) {
        return this.asyncFireEvent(Observable.just(event));
    }

    @Override
    public <T extends Event> Observable<EventFireResult<T>> asyncFireEvent(final Observable<T> event) {
        return Observable.from(dispatchers)
                .map(dispatcher -> dispatcher.asyncDispatch(event,null/*TODO provide session*/))
                .reduce(EventFireResult.builder(event), EventFireResult.Builder::withDispatchResult)
                .map(EventFireResult.Builder::build);
    }

    @Override
    public <T extends Event> EventFireResult<T> fireEvent(T event) {
        return asyncFireEvent(event).toBlocking().first();
    }
}
