package com.dreameddeath.core.notification.bus.impl;

import com.dreameddeath.core.dao.exception.DuplicateUniqueKeyDaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationFailure;
import com.dreameddeath.core.dao.exception.validation.ValidationObservableException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.bus.PublishedResult;
import com.dreameddeath.core.notification.config.NotificationConfigProperties;
import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.validation.exception.ValidationCompositeFailure;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import rx.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class EventBusImpl implements IEventBus {
    private final Map<String,IEventListener> listenersMap = new ConcurrentHashMap<>();
    private final Disruptor<InternalEvent> disruptor;
    private final RingBuffer<InternalEvent> ringBuffer;
    private final EventTranslatorTwoArg<InternalEvent,Event, Notification> translator;

    public EventBusImpl(){
        int bufferSize = NotificationConfigProperties.EVENTBUS_BUFFER_SIZE.get();
        disruptor = new Disruptor<>(InternalEvent::new,
                                    Integer.highestOneBit(bufferSize),
                                    Executors.defaultThreadFactory());

        int size = NotificationConfigProperties.EVENTBUS_THREAD_POOL_SIZE.getValue(1);
        EventHandler<InternalEvent>[] arrayHandler=new InternalEventHandler[size];
        Arrays.fill(arrayHandler,new InternalEventHandler());
        disruptor.handleEventsWith(arrayHandler);
        disruptor.setDefaultExceptionHandler(new InternalExceptionHandler());
        translator=(internalEvent, sequence, event,notification) -> internalEvent.setProcessingElement(event,notification,getListenerByName(notification.getListenerName()));
        ringBuffer=disruptor.start();
    }

    public IEventListener getListenerByName(String listenerName){
        return listenersMap.get(listenerName);
    }

    public void addListener(IEventListener listener){
        listenersMap.put(listener.getName(),listener);
    }

    @Override
    public <T extends Event> Observable<EventFireResult<T>> asyncFireEvent(T event, final ICouchbaseSession session) {
        return this.asyncFireEvent(Observable.just(event),session);
    }

    @Override
    public <T extends Event> Observable<EventFireResult<T>> asyncFireEvent(final Observable<T> eventObservable,final ICouchbaseSession session) {
        return eventObservable.map(
                event-> {
                    listenersMap.values().stream()
                            .filter(listener->listener.isApplicable(event))
                            .forEach(listener -> event.addListeners(listener.getName()));
                    event.incrSubmissionAttempt();
                    return event;
                }
        )
        .flatMap(session::asyncSave)
        .flatMap(event->this.submitEvent(event,session));
    }

    @Override
    public <T extends Event> EventFireResult<T> fireEvent(T event,ICouchbaseSession session) {
        return asyncFireEvent(event,session).toBlocking().single();
    }

    protected <T extends Event> Observable<EventFireResult<T>> submitEvent(final T event,ICouchbaseSession session){
        List<Observable<PublishedResult>> listPublishedResult = new ArrayList<>();
        for(String inputListnerName:event.getListeners()){
            Observable<PublishedResult> notificationObservable = Observable.just(inputListnerName)
                    .map(listenerName->{
                        Notification result = new Notification();
                        result.setEventId(event.getId());
                        result.setListenerName(listenerName);
                        return result;
                    })
                    .flatMap(notification -> {
                        if(event.getSubmissionAttempt()>1L){
                            return session.asyncSave(notification);
                        }
                        else{
                            return Observable.just(notification);
                        }
                    })
                    .onErrorResumeNext(throwable -> {
                        if(throwable instanceof ValidationObservableException){
                            ValidationFailure validationFailure = ((ValidationObservableException)throwable).getCause().getFailure();
                            if(validationFailure instanceof ValidationCompositeFailure) {
                                DuplicateUniqueKeyDaoException duplicateUniqueKeyDaoException = ((ValidationCompositeFailure) validationFailure).findException(DuplicateUniqueKeyDaoException.class);
                                if(duplicateUniqueKeyDaoException!=null) {
                                    return session.asyncGet(duplicateUniqueKeyDaoException.getOwnerDocumentKey(),Notification.class)
                                            .map(notif->{
                                                notif.incNbAttempts();
                                                return notif;
                                            });
                                }
                            }
                        }
                        if(throwable instanceof RuntimeException){
                            throw (RuntimeException)throwable;
                        }
                        else{
                            throw new RuntimeException(throwable);
                        }
                    })
                    .map(notification -> {
                        ringBuffer.publishEvent(translator,event,notification);
                        return new PublishedResult(notification);
                    })
                    .onErrorResumeNext(
                            throwable -> {
                                return Observable.just(new PublishedResult(inputListnerName,throwable));
                            }
                    );
                listPublishedResult.add(notificationObservable);
        }

        return Observable.merge(listPublishedResult)
                .reduce(EventFireResult.builder(event),
                        EventFireResult.Builder::withDispatchResult
                        )
                .map(EventFireResult.Builder::build);
    }



    private class InternalEventHandler implements EventHandler<InternalEvent>{
        @Override
        public void onEvent(InternalEvent event, long sequence, boolean endOfBatch) throws Exception {
            event.getListener().submit(event.getNotification(),event.getEvent()).toBlocking().single();
        }

    }

    private class InternalExceptionHandler implements ExceptionHandler<InternalEvent>{
        @Override
        public void handleEventException(Throwable ex, long sequence, InternalEvent event) {

        }

        @Override
        public void handleOnStartException(Throwable ex) {

        }

        @Override
        public void handleOnShutdownException(Throwable ex) {

        }
    }

}
