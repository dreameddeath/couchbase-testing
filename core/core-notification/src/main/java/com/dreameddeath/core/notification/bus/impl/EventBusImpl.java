package com.dreameddeath.core.notification.bus.impl;

import com.dreameddeath.core.dao.exception.DuplicateUniqueKeyDaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationFailure;
import com.dreameddeath.core.dao.exception.validation.ValidationObservableException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.bus.IEventBusLifeCycleListener;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class EventBusImpl implements IEventBus {
    private final ThreadFactory threadFactory = new DefaultThreadFactory();
    private final List<IEventBusLifeCycleListener> lifeCycleListeners = new CopyOnWriteArrayList<>();
    private final Map<String,IEventListener> listenersMap = new ConcurrentHashMap<>();
    private final Disruptor<InternalEvent>[] disruptors;
    private final EventTranslatorTwoArg<InternalEvent,Event, Notification> translator;
    private final RingBuffer<InternalEvent>[] ringBuffers;

    public EventBusImpl(){
        int size = NotificationConfigProperties.EVENTBUS_THREAD_POOL_SIZE.getValue(1);
        disruptors = new Disruptor[size];
        ringBuffers = new RingBuffer[size];
        translator=(internalEvent, sequence, event,notification) -> internalEvent.setProcessingElement(event,notification,getListenerByName(notification.getListenerName()));
    }

    @Override
    public void addLifeCycleListener(IEventBusLifeCycleListener listener) {
        lifeCycleListeners.add(listener);
    }

    public IEventListener getListenerByName(String listenerName){
        return listenersMap.get(listenerName);
    }

    @Override
    public synchronized void addListener(IEventListener listener){
        if(listenersMap.containsKey(listener.getName())){
            Preconditions.checkArgument(listenersMap.get(listener.getName()).equals(listener),"The listener %s is already existing",listener.getName());
        }
        else {
            listenersMap.put(listener.getName(), listener);
            for(IEventBusLifeCycleListener lifeCycleListener:lifeCycleListeners){
                lifeCycleListener.onAddListener(listener);
            }
        }
    }

    public Collection<IEventListener> getListeners(){
        return listenersMap.values();
    }

    @Override
    public void start() {
        int bufferSize = Integer.highestOneBit(NotificationConfigProperties.EVENTBUS_BUFFER_SIZE.get());
        for(int index=0;index<disruptors.length;index++) {
            disruptors[index] = new Disruptor<>(InternalEvent::new,
                    bufferSize,
                    threadFactory,
                    ProducerType.MULTI,
                    new BlockingWaitStrategy()
            );
            disruptors[index].setDefaultExceptionHandler(new InternalExceptionHandler());
            //ringBuffers[index] =disruptors[index].getRingBuffer();
        }

        for(int index=0;index<disruptors.length;index++){
            disruptors[index].handleEventsWith(new InternalEventHandler());
            ringBuffers[index]=disruptors[index].start();
        }
        for(IEventBusLifeCycleListener lifeCycleListener:lifeCycleListeners){
            lifeCycleListener.onStart();
        }
    }

    @Override
    public void removeListener(IEventListener listener) {
        IEventListener removedListener=listenersMap.remove(listener.getName());
        if(removedListener!=null){
            for(IEventBusLifeCycleListener lifeCycleListener:lifeCycleListeners){
                lifeCycleListener.onRemoveListener(listener);
            }
        }
    }

    @Override
    public void stop() {
        for(IEventBusLifeCycleListener lifeCycleListener:lifeCycleListeners){
            lifeCycleListener.onStop();
        }
        for(int index=0;index<disruptors.length;index++) {
            //if(disruptors[index].s)
            if(disruptors[index]!=null) {
                disruptors[index].shutdown();
            }
        }

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
                        listenersMap.values().stream()
                                .filter(listener -> listener.isApplicable(event))
                                .forEach(listener -> event.addListeners(listener.getName()));

                    }
                    event.setStatus(Event.Status.NOTIFICATIONS_LIST_NAME_GENERATED);
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
                        String correlationId  = event.getCorrelationId();
                        if(correlationId==null){
                            correlationId = event.getId().toString();
                        }
                        int index = Math.abs((correlationId+notification.getListenerName()).hashCode())%ringBuffers.length;
                        ringBuffers[index].publishEvent(translator,event,notification);
                        return new PublishedResult(notification);
                    })
                    .onErrorResumeNext(
                            throwable -> Observable.just(new PublishedResult(inputListnerName,throwable))
                    );
                listPublishedResult.add(notificationObservable);
        }

        return Observable.merge(listPublishedResult)
                .reduce(EventFireResult.builder(event),
                        EventFireResult.Builder::withDispatchResult
                        )
                .map(EventFireResult.Builder::build)
                .flatMap(eventResult->{
                    if(eventResult.areAllNotificationsInDb()){
                        eventResult.getEvent().setStatus(Event.Status.NOTIFICATIONS_IN_DB);
                    }
                    return session.asyncSave(eventResult.getEvent()).map(savedEvent->eventResult);
                });
    }



    private class InternalEventHandler implements EventHandler<InternalEvent>{
        private final Logger LOG = LoggerFactory.getLogger(InternalEventHandler.class);

        @Override
        public void onEvent(InternalEvent event, long sequence, boolean endOfBatch) throws Exception {
            //if(listener.getName().equals(event.getNotification().getListenerName()) && (!isMultithreaded || ((sequence % nbThreads)==rank))) {
            LOG.trace("Submitting {} with seq {} for listener {}",event.getNotification().getBaseMeta().getKey(),sequence,this);
            SubmissionResult result = event.getListener().submit(event.getNotification(), event.getEvent()).toBlocking().single();
            if(result.isFailure()){
                throw new RuntimeException(result.getError());
            }
            //}
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


    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-eventbus-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
