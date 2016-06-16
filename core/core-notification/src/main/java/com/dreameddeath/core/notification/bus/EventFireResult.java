package com.dreameddeath.core.notification.bus;

import com.dreameddeath.core.notification.dispatch.DispatchResult;
import com.dreameddeath.core.notification.model.v1.Event;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class EventFireResult<T extends Event> {
    private final Observable<T> event;
    private final List<Observable<DispatchResult>> results;

    private EventFireResult(Builder<T> builder){
        event=builder.event;
        results=builder.dispatchResults;
    }

    public T getEvent() {
        return event.toBlocking().first();
    }

    public Observable<T> getAsyncEvent(){
        return event;
    }

    public Observable<Boolean> isSuccess(){
        return Observable.merge(results).filter(DispatchResult::hasFailures).count().map(res->res==0);
    }

    public static <TEVT extends Event> Builder<TEVT> builder(Observable<TEVT> event){
        return new Builder<>(event);
    }

    public static class Builder<T extends Event>{
        private final Observable<T> event;
        private List<Observable<DispatchResult>> dispatchResults = new ArrayList<>();

        public Builder(Observable<T> event){
            this.event=event;
        }

        public Builder<T> withDispatchResult(Observable<DispatchResult> dispatchResult){
            dispatchResults.add(dispatchResult);
            return this;
        }

        public EventFireResult<T> build(){
            return new EventFireResult<>(this);
        }
    }
}
