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
import com.dreameddeath.core.notification.bus.IEventBus;
import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.IEventListenerFactory;
import com.dreameddeath.core.notification.listener.impl.AbstractDiscoverableListener;
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
    private Map<String,AbstractDiscoverableListener> listenerMap = new ConcurrentHashMap<>();
    private IEventListenerFactory listenerFactory;

    public ListenerAutoSubscribe() {
    }

    public ListenerAutoSubscribe(IEventBus bus) {
        setBus(bus);
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
        for(AbstractDiscoverableListener listener:listenerMap.values()){
            bus.addListener(listener);
        }
    }

    @Override
    public void onRegister(String uid, ListenerDescription obj) {
        AbstractDiscoverableListener listener=buildListener(obj);
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
        AbstractDiscoverableListener currListener = listenerMap.get(uid);
        AbstractDiscoverableListener newListener = buildListener(newDescription);
        if(!currListener.getName().equals(newListener.getName())){
            throw new IllegalStateException("Cannot change the listener name "+currListener.getName()+" to " +newListener.getName()+ " for uid "+uid);
        }

        if(currListener.getDescription().getType().equals(newListener.getDescription().getType())){
            throw new IllegalStateException("Cannot change the listener type "+currListener.getDescription()+" to " +newListener.getDescription()+ " for uid "+uid +" and name "+currListener.getName());
        }

        currListener.setDescription(newDescription);
    }

    public AbstractDiscoverableListener buildListener(ListenerDescription description){
        IEventListener listener = listenerFactory.getListener(description);
        if(listener==null){
            LOG.info("No listener setup by the factory {} for listener {}/{}",listenerFactory,description.getType(),description.getName(),listener.getClass());
        }

        if(! (listener instanceof AbstractDiscoverableListener)){
            LOG.error("The listener setup by the factory {} for {}/{} isn't of the right type ({})",listenerFactory,description.getType(),description.getName(),listener.getClass());
            listener=null;
        }

        if(listener==null){
            if(description.getAllowDeferred()){
                listener = new DefaultDiscoverableDeferringNotification(description);
            }
            else{
                listener = new DiscoverableDefaultBlockingListener(description);
            }
        }
        return (AbstractDiscoverableListener)listener;
    }
}
