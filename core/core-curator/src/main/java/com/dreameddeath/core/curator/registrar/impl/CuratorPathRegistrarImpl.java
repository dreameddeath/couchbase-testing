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

package com.dreameddeath.core.curator.registrar.impl;

import com.dreameddeath.core.curator.model.IRegistrablePathData;
import com.dreameddeath.core.curator.registrar.ICuratorPathRegistrar;
import com.dreameddeath.core.curator.utils.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 26/10/2015.
 */
public abstract class CuratorPathRegistrarImpl<T extends IRegistrablePathData> implements ICuratorPathRegistrar<T>,Closeable {
    private final static Logger LOG = LoggerFactory.getLogger(CuratorPathRegistrarImpl.class);
    private final CuratorFramework curatorFramework;
    private final String basePath;
    private final Map<String,NodeCache> registered = new HashMap<>();
    //private final Map<String,T> registeredValues = new HashMap<>();

    public CuratorPathRegistrarImpl(CuratorFramework curatorFramework, String basePath) {
        this.curatorFramework = curatorFramework;
        this.basePath = basePath;
    }

    protected abstract byte[] serialize(T obj) throws Exception;
    protected abstract T deserialize(String uid,byte[] currentData);
    protected abstract int compare(T currObj,T newObj);


    final public CuratorFramework getCuratorFramework() {
        return curatorFramework;
    }

    final public String getBasePath() {
        return basePath;
    }

    protected void preparePath(T obj) throws Exception{
        CuratorUtils.createPathIfNeeded(curatorFramework, CuratorUtils.buildPath(basePath,obj.uid()),serialize(obj));
    }


    @Override @PreDestroy
    public synchronized final void close() {
        registered.entrySet().forEach(entry -> {
            try {
                entry.getValue().close();
            } catch (IOException e) {
                LOG.error("Cannot close node "+entry.getValue().getCurrentData().getPath());
            }
        });
    }

    @Override
    public synchronized final Result register(T obj) throws Exception{
        preparePath(obj);
        final NodeCache node = new NodeCache(curatorFramework,CuratorUtils.buildPath(basePath,obj.uid()));
        final CountDownLatch initDone = new CountDownLatch(1);
        final NodeCacheListener listener=initDone::countDown;
        node.getListenable().addListener(listener);
        node.start(false);
        if(!initDone.await(30, TimeUnit.SECONDS)){
            throw new TimeoutException("Init failed of path data "+obj.uid());
        }
        node.getListenable().removeListener(listener);
        LOG.info("Registered path {}",node.getCurrentData().getPath());
        if(updateIfCacheMiss(node,obj)!=Result.IGNORED) {
            registered.put(obj.uid(), node);
            return Result.DONE;
        }
        return Result.IGNORED;
    }

    private final int versionCompare(T currentObj,T newObj) {
        if (currentObj != null) {
            return compare(currentObj, newObj);
        }
        else {
            return -1;
        }
    }
    private final Result updateIfCacheMiss(NodeCache cache, T newObj){
        ChildData childData=cache.getCurrentData();
        T currentObj=deserialize(newObj.uid(),childData.getData());
        int compareRes=versionCompare(deserialize(newObj.uid(),childData.getData()),newObj);
        //The current object has a lower version than the new one, perform update
        if(compareRes<0){
            CountDownLatch updateDone=new CountDownLatch(1);
            NodeCacheListener listener=updateDone::countDown;
            cache.getListenable().addListener(listener);
            try {
                Stat newStat = curatorFramework.setData().withVersion(childData.getStat().getVersion()).forPath(childData.getPath(), serialize(newObj));
                if(!updateDone.await(30,TimeUnit.SECONDS)){
                    LOG.warn("Update cached failed in time of obj {}/{} to version {}", newObj.uid(), newObj.getClass().getSimpleName(), newObj.version());
                }
                else {
                    LOG.info("Update successfull of obj {}/{} to version {}", newObj.uid(), newObj.getClass().getSimpleName(), newObj.version());
                }
                return Result.DONE;
            }
            catch(Exception e){
                LOG.warn("Data updates from object version "+currentObj.uid()+"/"+currentObj.version()+" to "+newObj.uid()+"/"+newObj.version()+". The exception is : ",e);
            }
            cache.getListenable().removeListener(listener);
        }
        return compareRes==0?Result.UNCHANGE:Result.IGNORED;
    }

    @Override
    public synchronized final Result update(T obj) throws Exception {
        NodeCache currNode=registered.get(obj.uid());
        if(currNode==null){
            return register(obj);
        }
        else{
            return updateIfCacheMiss(currNode, obj);
        }
    }

    @Override
    public synchronized final Result deregister(T obj) throws Exception{
        NodeCache currNode=registered.get(obj.uid());
        if(currNode!=null){
            ChildData childData=currNode.getCurrentData();
            T currentObj=deserialize(obj.uid(),childData.getData());
            int compareRes=versionCompare(currentObj,obj);
            if(compareRes==0){
                try {
                    curatorFramework.delete().withVersion(childData.getStat().getVersion()).forPath(childData.getPath());
                    return Result.DONE;
                }
                catch(Exception e){
                    LOG.error("Deletion failed of "+childData.getPath(),e);
                    return Result.IGNORED;
                }
            }

        }
        return Result.IGNORED;
    }

    @Override
    public synchronized final List<T> registeredList(){
        return registered.entrySet().stream().map(entry->deserialize(entry.getKey(),entry.getValue().getCurrentData().getData())).collect(Collectors.toList());
    }

}
