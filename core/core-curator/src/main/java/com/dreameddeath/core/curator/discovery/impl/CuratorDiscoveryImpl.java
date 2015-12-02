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

package com.dreameddeath.core.curator.discovery.impl;

import com.dreameddeath.core.curator.discovery.ICuratorDiscovery;
import com.dreameddeath.core.curator.discovery.ICuratorDiscoveryLifeCycleListener;
import com.dreameddeath.core.curator.discovery.ICuratorDiscoveryListener;
import com.dreameddeath.core.curator.model.IRegisterable;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Christophe Jeunesse on 26/10/2015.
 */
public abstract class CuratorDiscoveryImpl<T extends IRegisterable> implements ICuratorDiscovery<T> {
    private final static Logger LOG = LoggerFactory.getLogger(CuratorDiscoveryImpl.class);
    private final CuratorFramework curatorFramework;
    private final String basePath;
    private PathChildrenCache pathCache=null;
    private List<ICuratorDiscoveryListener<T>> listeners =new ArrayList<>();
    private List<ICuratorDiscoveryLifeCycleListener> lifeCycleListeners =new ArrayList<>();
    private Map<String,T> instanceCache =  Maps.newConcurrentMap();

    public CuratorDiscoveryImpl(CuratorFramework curatorFramework, String basePath) {
        this.curatorFramework = curatorFramework;
        if(!basePath.startsWith("/")){
            basePath="/"+basePath;
        }
        this.basePath = basePath;
    }

    protected abstract T deserialize(String uid,byte[] element);

    @PostConstruct
    public final void start() throws Exception{
        for(ICuratorDiscoveryLifeCycleListener listener:lifeCycleListeners){
            listener.onStart(this,true);
        }
        pathCache = new PathChildrenCache(curatorFramework,basePath,true);
        pathCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        CountDownLatch started=new CountDownLatch(1);
        pathCache.getListenable().addListener((client, event) -> {
            synchronized (CuratorDiscoveryImpl.this) {
                String uid;
                switch (event.getType()) {
                    case CHILD_UPDATED:
                    case CHILD_ADDED:
                        LOG.debug("event {} / {}", event.getType(), event.getData().getPath());
                         uid = event.getData().getPath().substring(event.getData().getPath().lastIndexOf("/")+1);
                        T obj = deserialize(uid,event.getData().getData());
                        resync(uid,obj);
                        break;
                    case CHILD_REMOVED:
                        LOG.debug("event {} / {}", event.getType(), event.getData().getPath());
                        uid = event.getData().getPath().substring(event.getData().getPath().lastIndexOf("/")+1);
                        remove(uid);
                        break;
                    case CONNECTION_RECONNECTED:
                        LOG.debug("event {} / {}", event.getType(), event.getInitialData().size());
                        Set<String> existingUid = instanceCache.keySet();
                        for(ChildData child :event.getInitialData()){
                            uid = child.getPath().substring(child.getPath().lastIndexOf("/")+1);
                            T resyncObj =deserialize(uid,child.getData());
                            resync(uid,resyncObj);
                            existingUid.remove(resyncObj.getUid());
                        }
                        existingUid.forEach(this::remove);
                        break;
                    case INITIALIZED:
                        started.countDown();
                }
            }
        });
        started.await(10, TimeUnit.SECONDS);//TODO be parametizable
        for(ICuratorDiscoveryLifeCycleListener listener:lifeCycleListeners){
            listener.onStart(this,false);
        }
        LOG.info("Discoverer started on path "+basePath);
    }

    @PreDestroy
    public final void stop() throws Exception{
        for(ICuratorDiscoveryLifeCycleListener listener:lifeCycleListeners){
            listener.onStop(this, true);
        }
        if(pathCache!=null) {
            pathCache.close();
        }
        instanceCache.clear();
        for(ICuratorDiscoveryLifeCycleListener listener:lifeCycleListeners){
            listener.onStop(this,false);
        }
    }

    protected void resync(String uid,final T newObj){
        final T oldObj = instanceCache.put(newObj.getUid(), newObj);
        if(oldObj==null) {
            listeners.forEach(l -> {
                try{
                    l.onRegister(uid,newObj);
                }
                catch(Throwable e){
                    ///TODO log error
                }
            });
        }
        else{
            listeners.forEach(l -> {
                try {
                    l.onUpdate(uid,oldObj, newObj);
                }
                catch (Throwable e){
                    ///TODO log error
                }
            });
        }
    }

    protected void remove(String uid){
        T oldObj = instanceCache.get(uid);
        if(oldObj!=null) {
            instanceCache.remove(uid);
            listeners.forEach(l -> {
                try {
                    l.onUnregister(uid,oldObj);
                }
                catch (Throwable e){
                    ///Todo log error
                }
            });
        }
    }



    @Override
    public List<T> getList(){
        Preconditions.checkNotNull(pathCache);
        return Lists.newArrayList(instanceCache.values());
    }

    @Override
    public T get(String uid) {
        Preconditions.checkNotNull(pathCache);
        return  instanceCache.get(uid);
    }

    @Override
    public void refresh() throws Exception{
        Preconditions.checkNotNull(pathCache);
        pathCache.clearAndRefresh();
    }

    @Override
    public void addListener(ICuratorDiscoveryListener<T> listener) {
        listeners.add(listener);
        for(Map.Entry<String,T> entry:instanceCache.entrySet()){
            listener.onRegister(entry.getKey(),entry.getValue());
        }
    }

    @Override
    public void addLifeCycleListener(ICuratorDiscoveryLifeCycleListener listener){
        lifeCycleListeners.add(listener);
    }


    @Override
    final public CuratorFramework getClient(){
        return curatorFramework;
    }

}
