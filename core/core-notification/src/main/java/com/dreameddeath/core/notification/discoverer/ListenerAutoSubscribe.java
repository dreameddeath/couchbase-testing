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

package com.dreameddeath.core.notification.discoverer;

import com.dreameddeath.core.curator.discovery.path.ICuratorPathDiscoveryListener;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.listener.HasListenerDescription;
import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.IEventListenerFactory;
import com.dreameddeath.core.notification.listener.impl.AbstractLocalListener;
import com.dreameddeath.core.notification.listener.impl.DefaultDiscoverableDeferringListener;
import com.dreameddeath.core.notification.listener.impl.DiscoverableDefaultBlockingListener;
import com.dreameddeath.core.notification.model.v1.listener.ListenedEvent;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 02/08/2016.
 */
public class ListenerAutoSubscribe implements ICuratorPathDiscoveryListener<ListenerDescription> {
    private static final Logger LOG = LoggerFactory.getLogger(ListenerAutoSubscribe.class);
    private IEventBus bus;
    private Map<String,IEventListener> listenerMap = new ConcurrentHashMap<>();
    private ListMultimap<String,IEventListener> crossDomainListeners = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private IEventListenerFactory listenerFactory;
    private ICouchbaseSessionFactory sessionFactory;

    public ListenerAutoSubscribe() {
    }

    public ListenerAutoSubscribe(IEventBus bus) {
        setBus(bus);
    }

    public ListenerAutoSubscribe(IEventBus bus,IEventListenerFactory factory) {
        setBus(bus);
        setListenerFactory(factory);
    }

    @Autowired
    public ListenerAutoSubscribe setSessionFactory(ICouchbaseSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        return this;
    }

    public IEventBus getBus() {
        return bus;
    }

    @Autowired
    public void setBus(IEventBus bus) {
        this.bus = bus;
        resyncBus();
    }

    @Autowired
    public void setListenerFactory(IEventListenerFactory listenerFactory) {
        this.listenerFactory = listenerFactory;
    }

    private void resyncBus(){
        for(IEventListener listener:listenerMap.values()){
            bus.addListener(listener);
        }
    }

    @Override
    public void onRegister(String uid, ListenerDescription obj) {
        LOG.info("Registering listener {}/{}",obj.getName(),obj.getType());
        IEventListener listener = buildListener(obj);
        {
            if (listener != null) {
                listenerMap.put(uid, listener);
                if (bus != null) {
                    bus.addListener(listener);
                }
            }
        }

        for (IEventListener crossDomainListener : buildCrossDomainListeners(obj, listener)) {
                registerCrossDomainListener(uid, obj, crossDomainListener);
        }
    }

    private void registerCrossDomainListener(String uid, ListenerDescription obj, IEventListener crossDomainListener) {
        LOG.info("Registering cross domain listener {}/{}/{}",obj.getName(),obj.getType(),crossDomainListener.getDomain());
        crossDomainListeners.put(uid,crossDomainListener);
        if(bus!=null){
            bus.addListener(crossDomainListener);
        }
    }

    private  List<IEventListener> buildCrossDomainListeners(ListenerDescription obj,IEventListener parentListener) {
        Map<String,IEventListener> crossDomainListenersPerDomain = new TreeMap<>();
        for(ListenedEvent listenedEvent:obj.getListenedEvents()){
            if(!obj.getDomain().equals(listenedEvent.getType().getDomain())){
                Preconditions.checkArgument(parentListener instanceof HasListenerDescription,"Cannot have cross domain for listener %s of class %s",parentListener.getName(),parentListener.getClass());
                Preconditions.checkArgument(listenedEvent.getPublishedClassName()!=null,"The listened event %s in listener %s must have a publishedClassName for cross domain", listenedEvent,parentListener.getName());
                crossDomainListenersPerDomain.computeIfAbsent(listenedEvent.getType().getDomain(),(domain)->
                        listenerFactory.getCrossDomainListener(domain,(IEventListener & HasListenerDescription)parentListener)
                );
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void onUnregister(String uid, ListenerDescription oldObj) {
        IEventListener listener = listenerMap.get(uid);
        removeListenerFromBus(listener);
        listenerMap.remove(uid);

        for(IEventListener crossDomainListener:crossDomainListeners.get(uid)){
            removeListenerFromBus(crossDomainListener);
        }
        crossDomainListeners.removeAll(uid);
    }

    private void removeListenerFromBus(IEventListener listener) {
        if(bus!=null && listener!=null){
            bus.removeListener(listener);
        }
    }

    @Override
    public void onUpdate(String uid, ListenerDescription oldDescription, ListenerDescription newDescription) {
        IEventListener currListener = listenerMap.get(uid);
        {
            IEventListener newListener = buildListener(newDescription);
            checkAndUpdateListener(uid, newDescription, currListener, newListener);
        }
        {
            List<IEventListener> newCrossDomainListeners = buildCrossDomainListeners(newDescription,currListener);
            Iterator<IEventListener> currCrossDomainIt = crossDomainListeners.get(uid).iterator();
            while(currCrossDomainIt.hasNext()){
                final IEventListener currCrossDomain = currCrossDomainIt.next();
                Optional<IEventListener> updateExisting = newCrossDomainListeners.stream().filter(listener -> currCrossDomain.getDomain().equals(listener.getDomain())).findFirst();
                if(updateExisting.isPresent()){
                    checkAndUpdateListener(uid,newDescription,currCrossDomain,updateExisting.get());
                }
                else{
                    removeListenerFromBus(currCrossDomain);
                    currCrossDomainIt.remove();
                }
            }

            for (IEventListener newCrossDomainListener : newCrossDomainListeners) {
                registerCrossDomainListener(uid,newDescription,newCrossDomainListener);
            }
        }
    }

    private void checkAndUpdateListener(String uid, ListenerDescription newDescription, IEventListener currListener, IEventListener newListener) {
        if(!currListener.getDomain().equals(newListener.getDomain())){
            throw new IllegalStateException("Cannot change the listener domain "+currListener.getDomain()+" to " +newListener.getDomain()+ " for uid "+uid);
        }

        if(!currListener.getName().equals(newListener.getName())){
            throw new IllegalStateException("Cannot change the listener name "+currListener.getName()+" to " +newListener.getName()+ " for uid "+uid);
        }

        if(currListener.getType().equals(newListener.getType())){
            throw new IllegalStateException("Cannot change the listener type "+currListener.getType()+" to " +newListener.getType()+ " for uid "+uid +" and name "+currListener.getName());
        }

        if(currListener instanceof HasListenerDescription) {
            ((HasListenerDescription)currListener).setDescription(newDescription);
        }
    }

    public IEventListener buildListener(ListenerDescription description){
        IEventListener listener = listenerFactory.getListener(description);

        if(listener==null){
            LOG.info("No listener setup by the factory {} for listener {}/{}",listenerFactory,description.getType(),description.getName());
            if(description.getAllowDeferred()!=null && description.getAllowDeferred()){
                listener = new DefaultDiscoverableDeferringListener(description);
            }
            else{
                listener = new DiscoverableDefaultBlockingListener(description);
            }
            ((AbstractLocalListener) listener).setSessionFactory(sessionFactory);
        }
        return listener;
    }
}
