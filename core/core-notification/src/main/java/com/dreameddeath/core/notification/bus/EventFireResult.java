package com.dreameddeath.core.notification.bus;

import com.dreameddeath.core.notification.model.v1.Event;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class EventFireResult<T extends Event> {
    private final T event;
    private final List<PublishedResult> results;
    private final boolean hasFailures;
    private final boolean allNotificationsInDb;
    private Throwable finalSaveError;

    private EventFireResult(Builder<T> builder){
        event=builder.event;
        results=builder.dispatchResults;
        hasFailures=results.stream().filter(PublishedResult::hasFailure).count()>0;
        allNotificationsInDb = results.stream().filter(PublishedResult::isNotificationInDb).count()==event.getListeners().size();
        finalSaveError=null;
    }

    public T getEvent() {
        return event;
    }

    public boolean isSuccess(){
        return !hasFailures();
    }

    public boolean hasFailures() {
        return hasFailures|| hasSaveError();
    }

    public static <TEVT extends Event> Builder<TEVT> builder(TEVT event){
        return new Builder<>(event);
    }

    public boolean areAllNotificationsInDb() {
        return allNotificationsInDb;
    }

    public Observable<EventFireResult<T>> withSaveError(Throwable e){
        this.finalSaveError = e;
        return Observable.just(this);
    }

    public boolean hasSaveError() {
        return finalSaveError!=null;
    }

    public Throwable getSaveError() {
        return finalSaveError;
    }

    public static class Builder<T extends Event>{
        private final T event;
        private List<PublishedResult> dispatchResults = new ArrayList<>();

        public Builder(T event){
            this.event=event;
        }

        public Builder<T> withDispatchResult(PublishedResult dispatchResult){
            dispatchResults.add(dispatchResult);
            return this;
        }

        public EventFireResult<T> build(){
            return new EventFireResult<>(this);
        }
    }
}
