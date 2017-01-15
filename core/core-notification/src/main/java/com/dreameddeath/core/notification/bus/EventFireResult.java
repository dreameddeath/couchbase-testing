/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.notification.bus;

import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.NotificationLink;
import io.reactivex.Single;

import java.util.*;

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

    public List<PublishedResult> getResults() {
        return Collections.unmodifiableList(results);
    }

    public static <TEVT extends Event> Builder<TEVT> builder(TEVT event){
        return new Builder<>(event);
    }

    public static <TEVT extends Event> Builder<TEVT> builder(EventFireResult<TEVT> eventFireResult,TEVT event){
        return new Builder<>(eventFireResult,event);
    }


    public boolean areAllNotificationsInDb() {
        return allNotificationsInDb;
    }

    public Single<EventFireResult<T>> withSaveError(Throwable e){
        this.finalSaveError = e;
        return Single.just(this);
    }

    public boolean hasSaveError() {
        return finalSaveError!=null;
    }

    public Throwable getSaveError() {
        return finalSaveError;
    }

    public Map<String, NotificationLink> getNoficationsLinksMap() {
        Map<String,NotificationLink> resultingMap=new TreeMap<>();
        for(PublishedResult result : results){
            resultingMap.put(result.getListnenerName(),new NotificationLink(result.getNotification()));
        }
        return resultingMap;
    }

    public static class Builder<T extends Event>{
        private final T event;
        private List<PublishedResult> dispatchResults = new ArrayList<>();

        public Builder(T event){
            this.event=event;
        }

        public Builder(EventFireResult<T> res,T event){
            this.event=event;
            this.dispatchResults.addAll(res.results);
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
