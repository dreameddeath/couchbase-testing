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
import com.dreameddeath.core.notification.listener.impl.AbstractDiscoverableLocalListener;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 02/08/2016.
 */
public class ListenerAutoSubscribe implements ICuratorDiscoveryListener<ListenerDescription> {
    private IEventBus bus;
    private Map<String,AbstractDiscoverableLocalListener> listenerMap = new ConcurrentHashMap<>();

    public ListenerAutoSubscribe() {
    }

    public ListenerAutoSubscribe(IEventBus bus) {
        setBus(bus);
    }

    public IEventBus getBus() {
        return bus;
    }

    public void setBus(IEventBus bus) {
        this.bus = bus;
        resyncBus();
    }

    private void resyncBus(){
        for(AbstractDiscoverableLocalListener listener:listenerMap.values()){
            bus.addListener(listener);
        }
    }

    @Override
    public void onRegister(String uid, ListenerDescription obj) {
        AbstractDiscoverableLocalListener listener=null;

        if(listener!=null && bus!=null){
            bus.addListener(listener);
        }
    }

    @Override
    public void onUnregister(String uid, ListenerDescription oldObj) {

    }

    @Override
    public void onUpdate(String uid, ListenerDescription oldObj, ListenerDescription newObj) {

    }
}
