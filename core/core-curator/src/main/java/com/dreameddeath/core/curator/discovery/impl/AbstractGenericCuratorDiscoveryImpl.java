/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *  
 */

package com.dreameddeath.core.curator.discovery.impl;

import com.dreameddeath.core.curator.discovery.generic.IGenericCuratorDiscovery;
import com.dreameddeath.core.curator.discovery.generic.IGenericCuratorDiscoveryLifeCycleListener;
import com.dreameddeath.core.curator.discovery.generic.IGenericCuratorDiscoveryListener;
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
 * Created by Christophe Jeunesse on 06/10/2016.
 */
public abstract class AbstractGenericCuratorDiscoveryImpl<TDATA,TLISTENER extends IGenericCuratorDiscoveryListener<TDATA>,TDISCOVERY extends IGenericCuratorDiscoveryLifeCycleListener> implements IGenericCuratorDiscovery<TDATA,TLISTENER,TDISCOVERY> {
    private final static Logger LOG = LoggerFactory.getLogger(StandardCuratorDiscoveryImpl.class);
    private final CuratorFramework curatorFramework;
    private final String basePath;
    private PathChildrenCache pathCache=null;
    private List<TLISTENER> listeners =new ArrayList<>();
    private List<TDISCOVERY> lifeCycleListeners =new ArrayList<>();
    private Map<String,TDATA> instanceCache =  Maps.newConcurrentMap();
    private CountDownLatch startedCountDown;

    public AbstractGenericCuratorDiscoveryImpl(CuratorFramework curatorFramework, String basePath) {
        this.curatorFramework = curatorFramework;
        if(!basePath.startsWith("/")){
            basePath="/"+basePath;
        }
        this.basePath = basePath;
    }

    protected abstract TDATA deserialize(String uid,byte[] element);

    protected abstract String getUid(TDATA data);

    protected void preparePath(){
        CuratorUtils.createPathIfNeeded(curatorFramework, basePath);
    }

    @PostConstruct
    public final void start() throws Exception{
        for(TDISCOVERY listener:lifeCycleListeners){
            listener.onStart(this,true);
        }
        preparePath();
        pathCache = new PathChildrenCache(curatorFramework,basePath,true);
        startedCountDown =new CountDownLatch(1);
        pathCache.getListenable().addListener((client, event) -> {
            switch (event.getType()) {
                case CHILD_UPDATED:
                case CHILD_ADDED:
                    LOG.debug("event {} / {}", event.getType(), (event.getData()!=null)?event.getData().getPath():null);
                    if(event.getData()!=null){
                        String uid = event.getData().getPath().substring(event.getData().getPath().lastIndexOf("/") + 1);
                        TDATA obj = deserialize(uid, event.getData().getData());
                        resync(uid,obj);
                    }
                    else{
                        LOG.warn("Received empty data for event of type {}",event.getType());
                    }
                    break;
                case CHILD_REMOVED:
                    LOG.debug("event {} / {}", event.getType(), (event.getData()!=null)?event.getData().getPath():null);
                    if(event.getData()!=null){
                        String uid = event.getData().getPath().substring(event.getData().getPath().lastIndexOf("/") + 1);
                        remove(uid);
                    }
                    else{
                        LOG.warn("Received empty data for event of type {}",event.getType());
                    }
                    break;
                case CONNECTION_RECONNECTED:
                    LOG.debug("event {} / {}", event.getType(), (event.getInitialData()!=null)?event.getInitialData().size():0);
                {
                    Set<String> existingUid = new HashSet<>(instanceCache.keySet());
                    if(event.getInitialData()!=null) {
                        for (ChildData child : event.getInitialData()) {
                            String uid = child.getPath().substring(child.getPath().lastIndexOf("/") + 1);
                            TDATA resyncObj = deserialize(uid, child.getData());
                            resync(uid, resyncObj);
                            existingUid.remove(getUid(resyncObj));
                        }
                        existingUid.forEach(this::remove);
                    }
                }
                break;
                case INITIALIZED:
                    startedCountDown.countDown();
                    break;
            }
        });

        pathCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        waitStarted();
        for(TDISCOVERY listener:lifeCycleListeners){
            listener.onStart(this,false);
        }
        LOG.info("Discoverer started on path "+basePath);
    }

    @PreDestroy
    public final void stop() throws Exception{
        waitStarted();
        LOG.info("Stopping discovery on path {}",basePath);
        for (TDISCOVERY listener : lifeCycleListeners) {
            listener.onStop(this, true);
        }
        if (pathCache != null) {
            pathCache.close();
        }
        //Simulate removal of entries as if they weren't there
        List<String> listUidToRemove = new ArrayList<>(instanceCache.keySet());
        listUidToRemove.forEach(this::remove);
        instanceCache.clear();
        for (TDISCOVERY listener : lifeCycleListeners) {
            listener.onStop(this, false);
        }
        startedCountDown = null;
    }

    protected void resync(String uid,final TDATA newObj){
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
        TDATA oldObj = instanceCache.remove(uid);
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
    public List<TDATA> getList(){
        Preconditions.checkNotNull(pathCache);
        return Lists.newArrayList(instanceCache.values());
    }

    @Override
    public TDATA get(String uid) {
        Preconditions.checkNotNull(pathCache);
        return  instanceCache.get(uid);
    }

    @Override
    public void refresh() throws Exception{
        Preconditions.checkNotNull(pathCache);
        pathCache.clearAndRefresh();
    }

    @Override
    public void addListener(TLISTENER listener) {
        listeners.add(listener);
        for(Map.Entry<String,TDATA> entry:instanceCache.entrySet()){
            listener.onRegister(entry.getKey(),entry.getValue());
        }
    }


    @Override
    public void removeListener(TLISTENER listener) {
        if(listeners.remove(listener)){
            for(Map.Entry<String,TDATA> entry:instanceCache.entrySet()){
                listener.onUnregister(entry.getKey(),entry.getValue());
            }
        }
    }

    public List<? extends TLISTENER> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    @Override
    public void addLifeCycleListener(TDISCOVERY listener){
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
        waitStarted(5, TimeUnit.MINUTES);
    }

    public void waitStarted(long timeout,TimeUnit unit) throws InterruptedException{
        if(startedCountDown !=null){
            if(startedCountDown.getCount()>0) {
                Preconditions.checkArgument(startedCountDown.await(timeout, unit), "The start phase hasn't finished on time");
            }
        }
        else{
            throw new IllegalStateException("The module hasn't reached start phase");
        }
    }
}
