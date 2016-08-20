package com.dreameddeath.core.notification.bus.impl;

import com.codahale.metrics.MetricRegistry;
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
import com.dreameddeath.core.notification.metrics.EventBusMetrics;
import com.dreameddeath.core.notification.metrics.NotificationMetrics;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class EventBusImpl implements IEventBus {
    private final static Logger LOG = LoggerFactory.getLogger(EventBusImpl.class);

    private final ThreadFactory threadFactory;
    private final List<IEventBusLifeCycleListener> lifeCycleListeners = new CopyOnWriteArrayList<>();
    private final Map<String,InternalEventHandler> eventHandlersMap = new ConcurrentHashMap<>();
    //private final Disruptor<InternalEvent>[] disruptors;
    private final EventTranslatorThreeArg<InternalEvent,Event, Notification,NotificationMetrics.Context> translator;
    //private final RingBuffer<InternalEvent>[] ringBuffers;
    private final EventBusMetrics eventBusMetrics;
    private volatile boolean isStarted=false;

    public EventBusImpl(){
        this("standard",null);
    }

    public EventBusImpl(MetricRegistry registry){
        this("standard",registry);
    }

    public EventBusImpl(String name){
        this(name,null);
    }

    public EventBusImpl(String name,MetricRegistry registry){
        translator=(internalEvent, sequence, event,notification,metricContext) -> internalEvent.setProcessingElement(event,notification,metricContext);
        eventBusMetrics = new EventBusMetrics("EventBus("+name+")",registry);
        threadFactory= new DefaultThreadFactory(name);
    }

    @Override
    public void addLifeCycleListener(IEventBusLifeCycleListener listener) {
        lifeCycleListeners.add(listener);
    }

    public synchronized List<IEventListener> getListeners(){
        return eventHandlersMap.values().stream().map(internalEventHandler -> internalEventHandler.listener).collect(Collectors.toList());
    }

    @Override
    public synchronized void start() {
        isStarted=true;
        for(IEventBusLifeCycleListener lifeCycleListener:lifeCycleListeners){
            try {
                lifeCycleListener.onStart();
            }
            catch(Throwable e){
                LOG.error("Listener <"+lifeCycleListener+"> error ",e);
            }
        }
        for(InternalEventHandler handler:eventHandlersMap.values()){
            handler.start();
        }
    }

    @Override
    public synchronized void addListener(IEventListener listener){
        if(eventHandlersMap.containsKey(listener.getName())){
            Preconditions.checkArgument(eventHandlersMap.get(listener.getName()).listener.equals(listener),"The listener %s is already existing",listener.getName());
        }
        else {
            InternalEventHandler handler=new InternalEventHandler(listener);
            eventHandlersMap.put(listener.getName(),handler);
            for(IEventBusLifeCycleListener lifeCycleListener:lifeCycleListeners){
                try {
                    lifeCycleListener.onAddListener(listener);
                }
                catch(Throwable e){
                    LOG.error("Listener <"+lifeCycleListener+"> error ",e);
                }
            }
            if(isStarted){
                handler.start();
            }
        }
    }

    @Override
    public synchronized void removeListener(IEventListener listener) {
        InternalEventHandler handler=eventHandlersMap.remove(listener.getName());
        if(handler!=null){
            handler.stop();
            for(IEventBusLifeCycleListener lifeCycleListener:lifeCycleListeners){
                try {
                    lifeCycleListener.onRemoveListener(listener);
                }
                catch(Throwable e){
                    LOG.error("Listener <"+lifeCycleListener+"> error ",e);
                }
            }
        }
    }

    @Override
    public synchronized void stop() {
        isStarted=false;
        for(InternalEventHandler handler:eventHandlersMap.values()){
            handler.stop();
        }
        for(IEventBusLifeCycleListener lifeCycleListener:lifeCycleListeners){
            try {
                lifeCycleListener.onStop();
            }
            catch(Throwable e){
                LOG.error("Listener <"+lifeCycleListener+"> error ",e);
            }
        }
        if(eventBusMetrics!=null){
            eventBusMetrics.close();
        }
    }

    @Override
    public <T extends Event> Observable<EventFireResult<T>> asyncFireEvent(T event, final ICouchbaseSession session) {
        return this.asyncFireEvent(Observable.just(event),session);
    }

    @Override
    public <T extends Event> Observable<EventFireResult<T>> asyncFireEvent(final Observable<T> eventObservable,final ICouchbaseSession session) {
        final EventBusMetrics.Context eventMetricContext = eventBusMetrics.start();
        return eventObservable.map(
                event-> {
                    event.incrSubmissionAttempt();
                    if(event.getSubmissionAttempt()==1) {
                        eventHandlersMap.values().stream()
                                .filter(handler -> handler.listener.isApplicable(event))
                                .forEach(handler-> event.addListeners(handler.listener.getName()));
                        event.setStatus(Event.Status.NOTIFICATIONS_LIST_NAME_GENERATED);
                    }
                    return event;
                }
        )
        .map(eventMetricContext::beforeInitialSave)
        .flatMap(session::asyncSave)
        .map(eventMetricContext::afterInitialSave)
        .flatMap(event->this.submitEvent(event,session,eventMetricContext))
        .doOnError(eventMetricContext::stop);
    }

    @Override
    public <T extends Event> EventFireResult<T> fireEvent(T event,ICouchbaseSession session) {
        return asyncFireEvent(event,session).toBlocking().single();
    }

    protected <T extends Event> Observable<EventFireResult<T>> submitEvent(final T event,ICouchbaseSession session,final EventBusMetrics.Context eventMetricContext){
        List<Observable<PublishedResult>> listPublishedResult = new ArrayList<>();
        for(String inputListnerName:event.getListeners()){
            final NotificationMetrics.Context notificationMetricContext = eventMetricContext.notificationStart(inputListnerName);
            Observable<PublishedResult> notificationObservable = Observable.just(inputListnerName)
                    .map(listenerName->{
                        Notification result = new Notification();
                        result.setEventId(event.getId());
                        result.setListenerName(listenerName);
                        return result;
                    })
                    .flatMap(notification -> {
                        Observable<Notification> notifObsRes = Observable.just(notification);
                        if(event.getSubmissionAttempt()>1L){
                            notifObsRes = notifObsRes
                                    .map(notificationMetricContext::beforeSave)
                                    .flatMap(session::asyncSave)
                                    .map(notificationMetricContext::afterSave);
                        }
                        return notifObsRes;
                    })
                    .onErrorResumeNext(throwable -> {
                        if(throwable instanceof ValidationObservableException){
                            ValidationFailure validationFailure = ((ValidationObservableException)throwable).getCause().getFailure();
                            if(validationFailure instanceof ValidationCompositeFailure) {
                                DuplicateUniqueKeyDaoException duplicateUniqueKeyDaoException = ((ValidationCompositeFailure) validationFailure).findException(DuplicateUniqueKeyDaoException.class);
                                if(duplicateUniqueKeyDaoException!=null) {
                                    return Observable.just(duplicateUniqueKeyDaoException.getOwnerDocumentKey())
                                            .map(notificationMetricContext::beforeReadDuplicate)
                                            .flatMap(notifId->session.asyncGet(notifId,Notification.class))
                                            .map(notificationMetricContext::afterReadDuplicate)
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
                        InternalEventHandler handler=eventHandlersMap.get(notification.getListenerName());
                        if(handler==null){
                            throw new IllegalStateException("Cannot find handler for listener "+notification.getListenerName());
                        }
                        handler.publish(event,notification,notificationMetricContext);
                        return new PublishedResult(notification);
                    })
                    .onErrorResumeNext(
                            throwable -> {
                                notificationMetricContext.stop(throwable);
                                return Observable.just(new PublishedResult(inputListnerName,throwable));
                            }
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
                    eventMetricContext.beforeFinalSave();
                    return session
                            .asyncSave(eventResult.getEvent())
                            .map(savedEvent->eventResult)
                            .map(eventMetricContext::afterFinalSave)
                            .onErrorResumeNext(eventResult::withSaveError)
                            .map(eventRes->eventMetricContext.stop(eventResult))
                            ;
                })
                ;
    }



    private class InternalEventHandler implements EventHandler<InternalEvent>{
        private final Logger LOG = LoggerFactory.getLogger(InternalEventHandler.class);
        private final Disruptor[] disruptors;
        private final RingBuffer[] ringBuffers;
        private final IEventListener listener;
        private final InternalExceptionHandler exceptionHandler=new InternalExceptionHandler();
        InternalEventHandler(IEventListener listener){
            int size = NotificationConfigProperties.EVENTBUS_THREAD_POOL_SIZE.getValue(1);
            disruptors = new Disruptor[size];
            ringBuffers = new RingBuffer[size];
            this.listener=listener;
        }
        
        public void start(){
            int bufferSize = Integer.highestOneBit(NotificationConfigProperties.EVENTBUS_BUFFER_SIZE.get());

            for(int index=0;index<disruptors.length;index++) {
                disruptors[index] = new Disruptor<>(InternalEvent::new,
                        bufferSize,
                        threadFactory,
                        ProducerType.MULTI,
                        new BlockingWaitStrategy()
                );
                disruptors[index].setDefaultExceptionHandler(exceptionHandler);
            }

            for(int index=0;index<disruptors.length;index++){
                disruptors[index].handleEventsWith(this);
                ringBuffers[index]=disruptors[index].start();
            }    
        }
        
        public <T extends Event> void publish(final T event,Notification notification,NotificationMetrics.Context notificationMetricContext){
            String correlationId  = event.getCorrelationId();
            if(correlationId==null){
                correlationId = event.getId().toString();
            }

            int index = Math.abs((correlationId+notification.getListenerName()).hashCode())%ringBuffers.length;
            notificationMetricContext.submitToQueue();
            ringBuffers[index].publishEvent(translator,event,notification,notificationMetricContext);
        }

        public void stop(){
            for(int index=0;index<disruptors.length;index++) {
                if(disruptors[index]!=null) {
                    disruptors[index].shutdown();
                }
            }
        }
        @Override
        public void onEvent(InternalEvent event, long sequence, boolean endOfBatch) throws Exception {
            LOG.trace("Submitting {} with seq {} for listener {}",event.getNotification().getBaseMeta().getKey(),sequence,this);
            event.getNotificationMetricsContext().submitToListener();
            SubmissionResult result = listener.submit(event.getNotification(), event.getEvent()).toBlocking().single();
            event.getNotificationMetricsContext().submitResult(result);
            if(result.isFailure()){
                throw new RuntimeException(result.getError());
            }
            event.cleanup();
        }

        private class InternalExceptionHandler implements ExceptionHandler<InternalEvent>{
            private Logger LOG = LoggerFactory.getLogger(InternalExceptionHandler.class);
            @Override
            public void handleEventException(Throwable ex, long sequence, InternalEvent event) {
                LOG.error("Error for event {}/{}",listener.getName(),event.getNotification().getEventId());
                LOG.error("The exception was :",ex);
                event.getNotificationMetricsContext().stop(ex);
                event.cleanup();
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



    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory(String busName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-eventbus-" + busName + "-"+
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
