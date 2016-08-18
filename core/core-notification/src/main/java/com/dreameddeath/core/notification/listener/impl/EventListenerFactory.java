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

package com.dreameddeath.core.notification.listener.impl;

import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.IEventListenerBuilder;
import com.dreameddeath.core.notification.listener.IEventListenerFactory;
import com.dreameddeath.core.notification.listener.IEventListenerTypeMatcher;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.dreameddeath.core.notification.utils.ListenerInfoManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Christophe Jeunesse on 12/08/2016.
 */
public class EventListenerFactory implements IEventListenerFactory {
    private final List<MatcherPair> listenerBuilderMatcher =new CopyOnWriteArrayList<>();
    private final Map<EventListenerKey,IEventListener> listenerMap = new ConcurrentHashMap<>();


    public void registerFromManager(ListenerInfoManager manager){
        for(ListenerInfoManager.ListenerClassInfo classInfo:manager.getListenersClassInfo()){
            registerListener(manager.getTypeMatcher(classInfo),manager.getListenerBuilder(classInfo));
        }
    }

    public void registerListener(IEventListenerTypeMatcher matcher, IEventListener listener){
        listenerBuilderMatcher.add(new MatcherPair(matcher,listener));
    }


    public void registerListener(IEventListenerTypeMatcher matcher,IEventListenerBuilder listenerBuilder){
        listenerBuilderMatcher.add(new MatcherPair(matcher,listenerBuilder));
    }

    private IEventListener getListener(EventListenerKey keyRequested){
        return listenerMap.computeIfAbsent(keyRequested,key->
                listenerBuilderMatcher.stream()
                        .filter(pair->pair.matcher.isMatching(key.type,key.params))
                        .sorted((pair1,pair2)->pair2.matcher.getMatchingRank(key.type,key.params)-pair1.matcher.getMatchingRank(key.type,key.params))
                        .findFirst()
                        .map(pair -> pair.getListener(key))
                        .orElse(null)
        );
    }

    @Override
    public IEventListener getListener(final String type, final String name, final Map<String, String> params) {
        return getListener(new EventListenerKey(type,name,params));
    }

    @Override
    public IEventListener getListener(final ListenerDescription description) {
        return getListener(new EventListenerKey(description));
    }

    private static class MatcherPair{
        private final IEventListenerTypeMatcher matcher;
        private final IEventListener listener;
        private final IEventListenerBuilder constructor;

        public MatcherPair(IEventListenerTypeMatcher matcher, IEventListener targetListener) {
            this.matcher = matcher;
            this.listener = targetListener;
            this.constructor=null;
        }

        public MatcherPair(IEventListenerTypeMatcher matcher, IEventListenerBuilder constructor) {
            this.matcher = matcher;
            this.listener = null;
            this.constructor=constructor;
        }

        public IEventListener getListener(EventListenerKey key){
            if(listener!=null){
                return listener;
            }
            else if(key.description!=null){
                return constructor.build(key.description);
            }
            else{
                return constructor.build(key.type,key.name,key.params);
            }
        }
    }

    private class EventListenerKey {
        private final String type;
        private final String name;
        private final Map<String,String> params;
        private final ListenerDescription description;
        public EventListenerKey(String type,String name, Map<String, String> params) {
            this.type = type;
            this.name = name;
            this.params = params!=null?params: Collections.EMPTY_MAP;
            this.description = null;
        }

        public EventListenerKey(ListenerDescription description) {
            this.type = description.getType();
            this.name = description.getName();
            this.params = description.getParameters()!=null?description.getParameters(): Collections.EMPTY_MAP;
            this.description = description;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EventListenerKey that = (EventListenerKey) o;

            if (!type.equals(that.type)) return false;
            if (!name.equals(that.name)) return false;
            return params.equals(that.params);

        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + params.hashCode();
            return result;
        }
    }
}
