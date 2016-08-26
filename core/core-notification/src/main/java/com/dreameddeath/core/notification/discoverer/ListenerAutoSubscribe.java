/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.notification.discoverer;

import com.dreameddeath.core.curator.discovery.ICuratorDiscoveryListener;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.IEventListenerFactory;
import com.dreameddeath.core.notification.listener.impl.AbstractDiscoverableListener;
import com.dreameddeath.core.notification.listener.impl.AbstractLocalListener;
import com.dreameddeath.core.notification.listener.impl.DefaultDiscoverableDeferringNotification;
import com.dreameddeath.core.notification.listener.impl.DiscoverableDefaultBlockingListener;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 02/08/2016.
 */
public class ListenerAutoSubscribe implements ICuratorDiscoveryListener<ListenerDescription> {
    private static final Logger LOG = LoggerFactory.getLogger(ListenerAutoSubscribe.class);
    private IEventBus bus;
    private Map<String,IEventListener> listenerMap = new ConcurrentHashMap<>();
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
        IEventListener listener=buildListener(obj);
        if(listener!=null){
            listenerMap.put(uid,listener);
            if(bus!=null) {
                bus.addListener(listener);
            }
        }
    }

    @Override
    public void onUnregister(String uid, ListenerDescription oldObj) {
        IEventListener listener = listenerMap.get(uid);
        if(bus!=null && listener!=null){
            bus.removeListener(listener);
        }
        listenerMap.remove(uid);
    }

    @Override
    public void onUpdate(String uid, ListenerDescription oldDescription, ListenerDescription newDescription) {
        IEventListener currListener = listenerMap.get(uid);
        IEventListener newListener = buildListener(newDescription);
        if(!currListener.getName().equals(newListener.getName())){
            throw new IllegalStateException("Cannot change the listener name "+currListener.getName()+" to " +newListener.getName()+ " for uid "+uid);
        }

        if(currListener.getType().equals(newListener.getType())){
            throw new IllegalStateException("Cannot change the listener type "+currListener.getType()+" to " +newListener.getType()+ " for uid "+uid +" and name "+currListener.getName());
        }

        if(currListener instanceof AbstractDiscoverableListener) {
            ((AbstractDiscoverableListener)currListener).setDescription(newDescription);
        }
    }

    public IEventListener buildListener(ListenerDescription description){
        IEventListener listener = listenerFactory.getListener(description);

        if(listener==null){
            LOG.info("No listener setup by the factory {} for listener {}/{}",listenerFactory,description.getType(),description.getName());
            if(description.getAllowDeferred()!=null && description.getAllowDeferred()){
                listener = new DefaultDiscoverableDeferringNotification(description);
            }
            else{
                listener = new DiscoverableDefaultBlockingListener(description);
            }
            ((AbstractLocalListener)listener).setSessionFactory(sessionFactory);
        }
        return listener;
    }
}
