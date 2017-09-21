/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.notification.bus;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.notification.common.IEvent;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.EventListenerLink;
import com.dreameddeath.core.notification.model.v1.INotificationsHolder;
import com.dreameddeath.core.notification.model.v1.NotificationLink;
import io.reactivex.Single;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class EventFireResult<T extends IEvent,THOLDER extends CouchbaseDocument & INotificationsHolder> {
    private final T event;
    private final THOLDER notificationHolder;
    private final List<PublishedResult> results;
    private final boolean hasFailures;
    private final boolean allNotificationsInDb;
    private Throwable finalSaveError;

    private EventFireResult(Builder<T,THOLDER> builder){
        event=builder.event;
        notificationHolder = builder.notificationHolder;
        results=builder.dispatchResults;
        hasFailures=results.stream().filter(PublishedResult::hasFailure).count()>0;
        allNotificationsInDb = results.stream().filter(PublishedResult::isNotificationInDb).count()==builder.getListeners().size();
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

    public boolean areAllNotificationsInDb() {
        return allNotificationsInDb;
    }

    public Single<EventFireResult<T,THOLDER>> withSaveError(Throwable e){
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


    public static <TEVT extends IEvent,THOLDER extends CouchbaseDocument & INotificationsHolder> Builder<TEVT,THOLDER> builder(TEVT event,THOLDER notificationHolder){
        return new Builder<>(event,notificationHolder) ;
    }

    public static <TEVT extends IEvent,THOLDER extends CouchbaseDocument & INotificationsHolder> Builder<TEVT,THOLDER> builder(EventFireResult<TEVT,THOLDER> eventFireResult,THOLDER event){
        return new Builder<>(eventFireResult,event);
    }

    public THOLDER getNotificationHolder() {
        return notificationHolder;
    }


    public static class Builder<T extends IEvent,THOLDER extends CouchbaseDocument & INotificationsHolder>{
        private final T event;
        private final THOLDER notificationHolder;
        private List<PublishedResult> dispatchResults = new ArrayList<>();


        private Builder(T event,THOLDER notificationHolder){
            this.event=event;
            this.notificationHolder = notificationHolder;
        }

        private Builder(EventFireResult<T,THOLDER> res,THOLDER notificationHolder){
            if(notificationHolder instanceof Event) {
                this.event = (T)notificationHolder;
            }
            else{
                this.event = res.event;
            }
            this.notificationHolder = notificationHolder;
            this.dispatchResults.addAll(res.results);
        }

        public Builder<T,THOLDER> withDispatchResult(PublishedResult dispatchResult){
            dispatchResults.add(dispatchResult);
            return this;
        }

        public EventFireResult<T,THOLDER> build(){
            return new EventFireResult<>(this);
        }

        public Collection<EventListenerLink> getListeners(){
            return notificationHolder.getListeners();
        }
    }

}
