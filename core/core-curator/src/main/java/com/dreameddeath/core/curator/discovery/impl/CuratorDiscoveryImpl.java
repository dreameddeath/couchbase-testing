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
import com.dreameddeath.core.curator.utils.CuratorUtils;
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
import java.util.*;
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
    private CountDownLatch startedCountDown;

    public CuratorDiscoveryImpl(CuratorFramework curatorFramework, String basePath) {
        this.curatorFramework = curatorFramework;
        if(!basePath.startsWith("/")){
            basePath="/"+basePath;
        }
        this.basePath = basePath;
    }

    protected abstract T deserialize(String uid,byte[] element);

    protected void preparePath(){
        CuratorUtils.createPathIfNeeded(curatorFramework, basePath);
    }

    @PostConstruct
    public final void start() throws Exception{
        for(ICuratorDiscoveryLifeCycleListener listener:lifeCycleListeners){
            listener.onStart(this,true);
        }
        preparePath();
        pathCache = new PathChildrenCache(curatorFramework,basePath,true);
        startedCountDown =new CountDownLatch(1);
        pathCache.getListenable().addListener((client, event) -> {
                switch (event.getType()) {
                    case CHILD_UPDATED:
                    case CHILD_ADDED:
                        //started.countDown();
                        LOG.debug("event {} / {}", event.getType(), event.getData().getPath());
                        {
                            String uid = event.getData().getPath().substring(event.getData().getPath().lastIndexOf("/") + 1);
                            T obj = deserialize(uid, event.getData().getData());
                            resync(uid,obj);
                        }
                        break;
                    case CHILD_REMOVED:
                        //started.countDown();
                        LOG.debug("event {} / {}", event.getType(), event.getData().getPath());
                        {
                            String uid = event.getData().getPath().substring(event.getData().getPath().lastIndexOf("/") + 1);
                            remove(uid);
                        }
                        break;
                    case CONNECTION_RECONNECTED:
                        LOG.debug("event {} / {}", event.getType(), event.getInitialData().size());
                        {
                            Set<String> existingUid = new HashSet<>(instanceCache.keySet());
                            for (ChildData child : event.getInitialData()) {
                                String uid = child.getPath().substring(child.getPath().lastIndexOf("/") + 1);
                                T resyncObj = deserialize(uid, child.getData());
                                resync(uid, resyncObj);
                                existingUid.remove(resyncObj.getUid());
                            }
                            existingUid.forEach(this::remove);
                        }
                        break;
                    case INITIALIZED:
                        /*for(ChildData childData:event.getInitialData()){
                            String uid = childData.getPath().substring(childData.getPath().lastIndexOf("/")+1);
                            T obj = deserialize(uid,childData.getData());
                            resync(uid,obj);
                        }*/
                        startedCountDown.countDown();
                        break;
                }
        });

        pathCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        waitStarted();
        for(ICuratorDiscoveryLifeCycleListener listener:lifeCycleListeners){
            listener.onStart(this,false);
        }
        LOG.info("Discoverer started on path "+basePath);
    }

    @PreDestroy
    public final void stop() throws Exception{
        waitStarted();
        LOG.info("Stopping discovery on path {}",basePath);
        for (ICuratorDiscoveryLifeCycleListener listener : lifeCycleListeners) {
            listener.onStop(this, true);
        }
        if (pathCache != null) {
            pathCache.close();
        }
        instanceCache.clear();
        for (ICuratorDiscoveryLifeCycleListener listener : lifeCycleListeners) {
            listener.onStop(this, false);
        }
        startedCountDown = null;
    }

    protected void resync(String uid,final T newObj){
        instanceCache.compute(uid,(key,oldObj)->{
            if(oldObj==null) {
                listeners.forEach(l -> {
                    try{
                        l.onRegister(uid,newObj);
                    }
                    catch(Throwable e){
                        LOG.error("Error during registering {}/{}",newObj.getClass(),uid);
                        LOG.error("Error the error was:",e);
                    }
                });
            }
            else{
                listeners.forEach(l -> {
                    try {
                        l.onUpdate(uid,oldObj, newObj);
                    }
                    catch (Throwable e){
                        LOG.error("Error during uid {}/{}",newObj.getClass(),uid);
                        LOG.error("Error the error was:",e);
                    }
                });
            }
            return newObj;
        }
        );
    }

    protected void remove(String uid){
        T oldObj = instanceCache.remove(uid);
        if(oldObj!=null) {
            listeners.forEach(l -> {
                try {
                    l.onUnregister(uid,oldObj);
                }
                catch (Throwable e){
                    LOG.error("Error during unregistering {}/{}",oldObj.getClass(),uid);
                    LOG.error("Error the error was:",e);
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
    public void removeListener(ICuratorDiscoveryListener<T> listener) {
        if(listeners.remove(listener)){
            for(Map.Entry<String,T> entry:instanceCache.entrySet()){
                listener.onUnregister(entry.getKey(),entry.getValue());
            }
        }
    }

    public List<ICuratorDiscoveryListener<T>> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    @Override
    public void addLifeCycleListener(ICuratorDiscoveryLifeCycleListener listener){
        lifeCycleListeners.add(listener);
    }


    @Override
    final public CuratorFramework getClient(){
        return curatorFramework;
    }

    final public String getBasePath(){
        return basePath;
    }

    public void waitStarted() throws InterruptedException{
        if(startedCountDown !=null){
            if(startedCountDown.getCount()>0) {
                Preconditions.checkArgument(startedCountDown.await(5, TimeUnit.MINUTES), "The start phase hasn't finished on time");
            }
        }
        else{
            throw new IllegalStateException("The module hasn't reached start phase");
        }
    }
}
