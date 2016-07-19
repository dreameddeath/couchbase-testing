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
import com.dreameddeath.core.notification.listener.SubmissionResult;
import com.dreameddeath.core.notification.model.v1.Event;
import com.dreameddeath.core.notification.model.v1.Notification;
import com.dreameddeath.core.validation.exception.ValidationCompositeFailure;
import com.google.common.base.Preconditions;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class EventBusImpl implements IEventBus {
    private final Map<String,IEventListener> listenersMap = new ConcurrentHashMap<>();
    private final Map<String,IEventListener> listenersMultithreadedMap = new ConcurrentHashMap<>();
    private final Map<String,IEventListener> fullListenerMap = new ConcurrentHashMap<>();
    private final Disruptor<InternalEvent> disruptor;
    private final EventTranslatorTwoArg<InternalEvent,Event, Notification> translator;
    private final RingBuffer<InternalEvent> ringBuffer;

    public EventBusImpl(){
        int bufferSize = Integer.highestOneBit(NotificationConfigProperties.EVENTBUS_BUFFER_SIZE.get());

        disruptor = new Disruptor<>(InternalEvent::new,
                                    bufferSize,
                                    Executors.defaultThreadFactory(),
                                    ProducerType.MULTI,
                                    new BlockingWaitStrategy()
                    );
        //disruptor.
        disruptor.setDefaultExceptionHandler(new InternalExceptionHandler());
        translator=(internalEvent, sequence, event,notification) -> internalEvent.setProcessingElement(event,notification,getListenerByName(notification.getListenerName()));
        ringBuffer=disruptor.getRingBuffer();
    }

    public IEventListener getListenerByName(String listenerName){
        return fullListenerMap.get(listenerName);
    }

    @Override
    public void addListener(IEventListener listener){
        IEventListener putRes;
        putRes = listenersMap.put(listener.getName(),listener);
        Preconditions.checkArgument(putRes==null,"The listener %s is already existing",listener.getName());
        putRes = fullListenerMap.put(listener.getName(),listener);
        Preconditions.checkArgument(putRes==null,"The listener %s is already existing",listener.getName());
        //disruptor.handleEventsWith(new InternalEventHandler(listener));
    }

    @Override
    public void addMultiThreadedListener(IEventListener listener){
        IEventListener putRes;
        putRes = listenersMultithreadedMap.put(listener.getName(),listener);
        Preconditions.checkArgument(putRes==null,"The listener %s is already existing",listener.getName());
        putRes = fullListenerMap.put(listener.getName(),listener);
        Preconditions.checkArgument(putRes==null,"The listener %s is already existing",listener.getName());
    }


    @Override
    public void start() {
        final ArrayList<EventHandler<InternalEvent>> handlersList = new ArrayList<>();
        int size = NotificationConfigProperties.EVENTBUS_THREAD_POOL_SIZE.getValue(1);
        listenersMap.forEach((name,listener)->handlersList.add(new InternalEventHandler(listener)));
        listenersMultithreadedMap.forEach((name,listener)->{
            for(int pos=0;pos<size;++pos) {
                handlersList.add(new InternalEventHandler(listener,pos,size));
            }
        });

        disruptor.handleEventsWith((EventHandler<InternalEvent>[])handlersList.toArray(new EventHandler[handlersList.size()]));

        disruptor.start();
    }

    @Override
    public void stop() {
        disruptor.shutdown();
    }

    @Override
    public <T extends Event> Observable<EventFireResult<T>> asyncFireEvent(T event, final ICouchbaseSession session) {
        return this.asyncFireEvent(Observable.just(event),session);
    }

    @Override
    public <T extends Event> Observable<EventFireResult<T>> asyncFireEvent(final Observable<T> eventObservable,final ICouchbaseSession session) {
        return eventObservable.map(
                event-> {
                    event.incrSubmissionAttempt();
                    if(event.getSubmissionAttempt()==1) {
                        fullListenerMap.values().stream()
                                .filter(listener -> listener.isApplicable(event))
                                .forEach(listener -> event.addListeners(listener.getName()));

                    }
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
                    .filter(notification -> notification.getStatus()!= Notification.Status.SUBMITTED && notification.getStatus()!= Notification.Status.CANCELLED )
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
        private final Logger LOG = LoggerFactory.getLogger(InternalEventHandler.class);
        private final IEventListener listener;
        private final boolean isMultithreaded;
        private final int nbThreads;
        private final int rank;

        public InternalEventHandler(IEventListener listener) {
            this(listener,0,0);
        }

        public InternalEventHandler(IEventListener listener,int rank,int nbThreads) {
            this.listener = listener;
            this.rank = rank;
            this.nbThreads = nbThreads;
            this.isMultithreaded = nbThreads>0;
        }


        @Override
        public void onEvent(InternalEvent event, long sequence, boolean endOfBatch) throws Exception {
            if(listener.getName().equals(event.getNotification().getListenerName()) && (!isMultithreaded || ((sequence % nbThreads)==rank))) {
                LOG.trace("Submitting {} with seq {} for listener {}",event.getNotification().getBaseMeta().getKey(),sequence,this);
                SubmissionResult result = listener.submit(event.getNotification(), event.getEvent()).toBlocking().single();
                if(result.isFailure()){
                    throw new RuntimeException(result.getError());
                }
            }
        }


        @Override
        public String toString() {
            return "InternalEventHandler{"+listener.getName()+(isMultithreaded?"["+rank+"/"+nbThreads+"]":"")+"}";
        }
    }

    private class InternalExceptionHandler implements ExceptionHandler<InternalEvent>{
        private Logger LOG = LoggerFactory.getLogger(InternalExceptionHandler.class);
        @Override
        public void handleEventException(Throwable ex, long sequence, InternalEvent event) {
            LOG.error("Error for event {}/{}",event.getListener().getName(),event.getNotification().getEventId());
            LOG.error("The exception was :",ex);
        }

        @Override
        public void handleOnStartException(Throwable ex) {
            LOG.error("The start exception was :",ex);
        }

        @Override
        public void handleOnShutdownException(Throwable ex) {
            LOG.error("The stop exception was :",ex);
        }
    }

}
