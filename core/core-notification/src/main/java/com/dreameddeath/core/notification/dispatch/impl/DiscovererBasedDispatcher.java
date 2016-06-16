package com.dreameddeath.core.notification.dispatch.impl;

import com.dreameddeath.core.couchbase.exception.DocumentNotFoundException;
import com.dreameddeath.core.curator.discovery.ICuratorDiscoveryListener;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.notification.discoverer.ListenerDiscoverer;
import com.dreameddeath.core.notification.dispatch.DispatchResult;
import com.dreameddeath.core.notification.dispatch.IDispatcher;
import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.IEventListenerFactory;
import com.dreameddeath.core.notification.listener.SubmissionResult;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.notification.model.v1.listener.ListenedEvent;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Observable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class DiscovererBasedDispatcher implements IDispatcher,ICuratorDiscoveryListener<ListenerDescription> {
    private IEventListenerFactory eventListenerFactory;
    private ListenerDiscoverer discoverer;
    private Map<String,IEventListener> listenerMap=new HashMap<>();
    private Multimap<EntityModelId,IEventListener> listenerMultimap = Multimaps.synchronizedSetMultimap(HashMultimap.create());

    @Autowired
    public void setDiscoverer(ListenerDiscoverer discoverer) {
        this.discoverer = discoverer;
        discoverer.addListener(this);
    }

    @Autowired
    public void setEventListenerFactory(IEventListenerFactory eventListenerFactory) {
        this.eventListenerFactory = eventListenerFactory;
    }

    @Override
    public void onRegister(String uid, ListenerDescription obj) {
        IEventListener listener = eventListenerFactory.getListener(obj.getType(),obj.getGroupName());
        listenerMap.put(uid,listener);
        for (ListenedEvent listenedEvent : obj.getListenedNotification()) {
            listenerMultimap.put(listenedEvent.getType(),listener);
        }
    }

    @Override
    public void onUnregister(String uid, ListenerDescription oldObj) {
        final IEventListener listener = listenerMap.remove(uid);
        oldObj.getListenedNotification().forEach(listenedEvent -> listenerMultimap.remove(listenedEvent.getType(),listener));
    }

    @Override
    public void onUpdate(String uid, ListenerDescription oldObj, ListenerDescription newObj) {
        onUnregister(uid,oldObj);
        onRegister(uid,newObj);
    }

    private <T extends Event> Observable<SubmissionResult> manageSubmission(final T event, final ICouchbaseSession session,boolean isRetry){
        Observable<T> eventObs;
        if(!isRetry){
            listenerMultimap.get(event.getModelId()).forEach(listener->event.addListeners(listener.getName()));
                eventObs =
                        session.asyncSave(event)
                                .map(CouchbaseDocument.Utils::freeze)
                                //.flatMap(evt-> {


                                //})
                ;

        }
        else{

        }




        return

                Observable.from(listenerMultimap.get(event.getModelId()))
                .reduce(new OverallSubmissionRequest<>(event,session,isRetry), OverallSubmissionRequest::addListener)
                .map(OverallSubmissionRequest::freeze)
                .map(this::enqueue);
    }

    private <T extends Event> Observable<SubmissionResult> postAll(final T event, final ICouchbaseSession session){
        return manageSubmission(event,session,false);
    }

    private <T extends Event> Observable<SubmissionResult> rePostAll(final T event, final ICouchbaseSession session){
        return manageSubmission(event,session,false);
    }

    private <T extends Event> SubmissionResult enqueue(OverallSubmissionRequest<T> request){
        return null;
    }

    @Override
    public <T extends Event> Observable<DispatchResult> asyncDispatch(final Observable<T> event, final ICouchbaseSession session) {
        return event
                .flatMap(evt-> postAll(evt,session))
                .reduce(new DispatchResult(), DispatchResult::addResult);
    }

    private static class OverallSubmissionRequest<T extends Event>{
        private final T event;
        private final ICouchbaseSession session;
        private final boolean isRecovery;
        private final List<IEventListener> listeners=new ArrayList<>();
        private boolean isFrozen = false;

        public OverallSubmissionRequest(T event,ICouchbaseSession session,boolean isRecovery){
            this.event = event;
            this.session = session;
            this.isRecovery=isRecovery;
        }

        public synchronized OverallSubmissionRequest<T> addListener(IEventListener listener){
            if(isFrozen){
                throw new IllegalStateException("Cannot add listener "+listener.getName()+listener.getType());
            }
            if(!event.getListeners().contains(listener.getName())){
                listeners.add(listener);
            }
            return this;
        }

        public Observable<SubmissionRequest<T>> getSubmissionRequests(){
            return Observable.from(listeners)
                    .map(listener -> new SubmissionRequest<>(event,listener,session,isRecovery))
                    .flatMap(SubmissionRequest::getRequestIfApplicable);
        }

        public synchronized OverallSubmissionRequest<T> freeze() {
            if(isFrozen){
                throw new IllegalStateException("Cannot freeze again");
            }
            session.asyncSave(event).map(CouchbaseDocument.Utils::freeze);

            this.event.getBaseMeta().freeze();
            isFrozen=true;
            return this;
        }

    }

    private static class SubmissionRequest<T extends Event>{
        private final T event;
        private final IEventListener listener;
        private final ICouchbaseSession session;
        private final boolean isRecovery;
        private Notification notification;

        public SubmissionRequest(T event, IEventListener listener,ICouchbaseSession session,boolean isRecovery){
            this.event = event;
            this.listener = listener;
            this.session = session;
            this.isRecovery = isRecovery;
        }


        public T getEvent() {
            return event;
        }

        public IEventListener getListener() {
            return listener;
        }

        private Observable<Notification> newNotification(){
            Notification newNotification = session.newEntity(Notification.class);
            newNotification.setListenerName(listener.getName());
            newNotification.setEventId(event.getId().toString());
            return session.asyncSave(newNotification);

        }


        private Observable<Notification> getApplicableNotification(){
            if(isRecovery){
                return session.asyncGetFromKeyParams(Notification.class, event.getId(), listener.getName())
                            .onErrorResumeNext(e-> {
                                if (e instanceof DocumentNotFoundException) {
                                    return newNotification();
                                }
                                else{
                                    throw new RuntimeException(e);
                                }
                            })
                            .filter(notification -> !notification.getStatus().equals(Notification.Status.SUBMITTED));
            }
            else{
                return newNotification();
            }
        }

        private SubmissionRequest<T> attachNotification(Notification notification){
            this.notification=notification;
            return this;
        }

        public Observable<SubmissionRequest<T>> getRequestIfApplicable(){
            return getApplicableNotification().map(this::attachNotification);
        }

        /*public Observable<SubmissionRequest<T>> markAsSuccess(){

        }

        public Observable<SubmissionRequest<T>> markAsFailure(String errorCode){

        }*/
    }
}
