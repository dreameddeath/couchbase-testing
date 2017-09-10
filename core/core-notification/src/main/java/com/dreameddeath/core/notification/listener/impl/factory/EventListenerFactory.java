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

package com.dreameddeath.core.notification.listener.impl.factory;

import com.dreameddeath.core.depinjection.IDependencyInjector;
import com.dreameddeath.core.notification.listener.*;
import com.dreameddeath.core.notification.listener.impl.crossdomain.LocalCrossDomainListener;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.dreameddeath.core.notification.utils.ListenerInfoManager;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
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
    private final Map<EventListenerKey<? extends IEventListener>,IEventListener> listenerMap = new ConcurrentHashMap<>();
    private IDependencyInjector dependencyInjector;
    private ListenerInfoManager listenerInfoManager;

    @Autowired
    public void setDependencyInjector(IDependencyInjector dependencyInjector){
        this.dependencyInjector = dependencyInjector;
    }

    public void setListenerInfoManager(ListenerInfoManager listenerInfoManager) {
        this.listenerInfoManager = listenerInfoManager;
    }

    @PostConstruct
    public void registerFromManager(){
        for(ListenerInfoManager.ListenerClassInfo classInfo:listenerInfoManager.getListenersClassInfo()){
            registerListener(listenerInfoManager.getTypeMatcher(classInfo),listenerInfoManager.getListenerBuilder(classInfo,dependencyInjector));
        }

        registerCrossDomainListener(new IEventListenerTypeMatcher() {
            @Override
            public boolean isMatching(boolean isCrossDomain, String type, Map<String, String> params) {
                return isCrossDomain;
            }

            @Override
            public int getMatchingRank(boolean isCrossDomain, String type, Map<String, String> params) {
                return 0;
            }
        }, (domain,listener)->new LocalCrossDomainListener<>(domain,(IEventListener & HasListenerDescription)listener));
    }

    public void registerListener(IEventListenerTypeMatcher matcher, IEventListener listener){
        listenerBuilderMatcher.add(new MatcherPair(matcher,listener));
    }


    public void registerCrossDomainListener(IEventListenerTypeMatcher matcher, CrossDomainListenerBuilder crossDomainBuilder){
        listenerBuilderMatcher.add(new MatcherPair(matcher,(domain,parentListener)->{
            IEventListener newListener = crossDomainBuilder.build(domain,parentListener);
            dependencyInjector.autowireBean(newListener,"listener"+newListener+"!CrossDomain!"+domain);
            return newListener;
        }));
    }

    public void registerListener(IEventListenerTypeMatcher matcher,IEventListenerBuilder listenerBuilder){
        listenerBuilderMatcher.add(new MatcherPair(matcher,listenerBuilder));
    }

    private <T extends IEventListener & HasListenerDescription> IEventListener getListener(EventListenerKey<T> keyRequested){
        return listenerMap.computeIfAbsent(keyRequested,key->
                listenerBuilderMatcher.stream()
                        .filter(pair->pair.matcher.isMatching(key.isCrossDomain(),key.type,key.params))
                        .sorted((pair1,pair2)->pair2.matcher.getMatchingRank(key.isCrossDomain(),key.type,key.params)-pair1.matcher.getMatchingRank(key.isCrossDomain(),key.type,key.params))
                        .findFirst()
                        .map(pair -> pair.getListener(key))
                        .orElse(null)
        );
    }

    @Override
    public <T extends IEventListener & HasListenerDescription> IEventListener getCrossDomainListener(final String domain, final T parentListener) {
        return getListener(new EventListenerKey<>(domain,parentListener));
    }

    @Override
    public IEventListener getListener(final ListenerDescription description) {
        return getListener(new EventListenerKey<>(description));
    }

    private static class MatcherPair{
        private final IEventListenerTypeMatcher matcher;
        private final CrossDomainListenerBuilder crossDomainListenerBuilder;
        private final IEventListener listener;
        private final IEventListenerBuilder constructor;

        public MatcherPair(IEventListenerTypeMatcher matcher, CrossDomainListenerBuilder crossDomainListenerBuilder) {
            this.matcher = matcher;
            this.crossDomainListenerBuilder = crossDomainListenerBuilder;
            this.listener = null;
            this.constructor=null;
        }


        public MatcherPair(IEventListenerTypeMatcher matcher, IEventListener targetListener) {
            this.matcher = matcher;
            this.crossDomainListenerBuilder =null;
            this.listener = targetListener;
            this.constructor=null;
        }

        public MatcherPair(IEventListenerTypeMatcher matcher, IEventListenerBuilder constructor) {
            this.matcher = matcher;
            this.crossDomainListenerBuilder = null;
            this.listener = null;
            this.constructor=constructor;
        }

        public IEventListener getListener(EventListenerKey<?> key){
            IEventListener newListener;
            if(listener!=null){
                return listener;
            }
            else if(crossDomainListenerBuilder !=null){
                Preconditions.checkState(key.isCrossDomain(),"Error {}",key);
                newListener = crossDomainListenerBuilder.build(key.domain,key.parentListener);
            }
            else if(key.description!=null){
                newListener = constructor.build(key.description);
            }
            else{
                newListener = constructor.build(key.domain,key.type,key.name,key.params);
            }

            return newListener;
        }
    }

    private static class EventListenerKey<T extends IEventListener & HasListenerDescription> {
        private final T parentListener;
        private final String type;
        private final String domain;
        private final String name;
        private final Map<String,String> params;
        private final ListenerDescription description;

        public  EventListenerKey(String domain,T parentListener) {
            this.parentListener=parentListener;
            this.domain=domain;
            this.type = parentListener.getType();
            this.name = parentListener.getName();
            this.params = parentListener.getDescription().getParameters()!=null?parentListener.getDescription().getParameters(): Collections.EMPTY_MAP;
            this.description = parentListener.getDescription();
        }

        public EventListenerKey(ListenerDescription description) {
            this.parentListener = null;
            this.domain = description.getDomain();
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

            if (!domain.equals(that.domain)) return false;
            if (!type.equals(that.type)) return false;
            if (!name.equals(that.name)) return false;
            return params.equals(that.params);
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + domain.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + params.hashCode();
            return result;
        }

        public boolean isCrossDomain() {
            return parentListener != null;
        }
    }

    public interface CrossDomainListenerBuilder<T extends HasListenerDescription & IEventListener> {
        IEventListener build(String domain, T parentListener);
    }
}
