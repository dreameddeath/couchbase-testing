/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.dao.view;

import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.exception.DaoNotFoundException;
import com.dreameddeath.core.dao.exception.DuplicateDaoException;
import com.dreameddeath.core.dao.factory.IDaoFactory;
import com.dreameddeath.core.model.document.CouchbaseDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 18/12/2014.
 */
public class CouchbaseViewDaoFactory implements IDaoFactory {
    private Map<ViewKeyClass, List<CouchbaseViewDao<?,?,? extends CouchbaseDocument>>> daosMap
            = new ConcurrentHashMap<>();
    private Map<ViewKey,CouchbaseViewDao> perClassNameAndNameCacheMap=new ConcurrentHashMap<>();


    public CouchbaseViewDaoFactory(Builder builder){

    }


    public void addDao(String domain,CouchbaseViewDao<?,?,? extends CouchbaseDocument> dao){
        DaoForClass annotation = dao.getParentDao().getClass().getAnnotation(DaoForClass.class);
        if(annotation==null){
            throw new NullPointerException("Annotation DaoForClass not defined for dao <"+dao.getParentDao().getClass().getName()+">");
        }
        addDaoFor(domain,(Class<CouchbaseDocument>) annotation.value(),(CouchbaseViewDao<?,?,CouchbaseDocument>) dao);
    }

    public synchronized <T extends CouchbaseDocument> void addDaoFor(String domain,Class<T> entityClazz,CouchbaseViewDao<?,?,T> dao){
        ViewKeyClass<T> key = new ViewKeyClass<>(domain,entityClazz);
        if(!daosMap.containsKey(key)){
            daosMap.put(key,new ArrayList<>());
        }

        for(CouchbaseViewDao<?,?,? extends CouchbaseDocument> existingDao:daosMap.get(key)){
            if(existingDao.getViewName().equals(dao.getViewName())){
                throw new DuplicateDaoException("The dao view <"+dao.getViewName()+"> is already existing for class "+entityClazz.getName());
            }
        }
        daosMap.get(key).add(dao);
    }

    public <T extends CouchbaseDocument> List<CouchbaseViewDao<?,?,? extends T>> getViewListDaoFor(String domain,Class<T> entityClass){
        ViewKeyClass<T> key = new ViewKeyClass<>(domain,entityClass);
        List<CouchbaseViewDao<?,?,? extends T>> result = (List)daosMap.get(key);
        if(result==null){
            Class<?> parentClass=entityClass.getSuperclass();
            if(CouchbaseDocument.class.isAssignableFrom(parentClass)){
                result = getViewListDaoFor(domain,parentClass.asSubclass(CouchbaseDocument.class));
                if(result!=null){
                    daosMap.put(key,(List)result);
                }
            }
        }
        return result;
    }

    public <T extends CouchbaseDocument> CouchbaseViewDao<?,?,T> getViewDaoFor(String domain,Class<T> entityClass,String viewName) throws DaoNotFoundException{
        ViewKey<T> key = new ViewKey<>(domain,entityClass,viewName);
        CouchbaseViewDao result = perClassNameAndNameCacheMap.get(key);
        if(result==null) {
            List<CouchbaseViewDao<?,?,T>> list = getViewListDaoFor(domain,(Class)entityClass);
            if(list != null){
                for(CouchbaseViewDao viewDao:list){
                    if(viewDao.getViewName().equals(viewName)){
                        perClassNameAndNameCacheMap.putIfAbsent(key,viewDao);
                        result=viewDao;
                        break;
                    }
                }
            }
        }

        if(result==null){
            throw new DaoNotFoundException(entityClass,viewName);
        }
        return result;
    }

    public void initAllViews() throws StorageException{
        Map<ICouchbaseBucket,Map<String,Map<String,String>>> bucketDesignDocMap=new HashMap<>();

        for(List list:daosMap.values()){
            List<CouchbaseViewDao> views=list;

            for(CouchbaseViewDao view:views){
                if(!bucketDesignDocMap.containsKey(view.getClient())){
                    bucketDesignDocMap.put(view.getClient(), new HashMap<>());
                }
                Map<String,Map<String,String>> designDocMap = bucketDesignDocMap.get(view.getClient());
                if(!designDocMap.containsKey(view.getDesignDoc())){
                    designDocMap.put(view.getDesignDoc(), new HashMap<>());
                }
                Map<String,String> daoNamedMap=designDocMap.get(view.getDesignDoc());
                daoNamedMap.put(view.getViewName(),view.buildMapString());
            }
        }

        for(Map.Entry<ICouchbaseBucket,Map<String,Map<String,String>>> bucketDesignDocEntry:bucketDesignDocMap.entrySet()) {
            for(Map.Entry<String,Map<String,String>> designDoc:bucketDesignDocEntry.getValue().entrySet()){
                bucketDesignDocEntry.getKey().createOrUpdateView(designDoc.getKey(),designDoc.getValue());
            }

        }
    }

    @Override
    public void init() {

    }

    @Override
    public synchronized void cleanup() {
        daosMap.clear();
        perClassNameAndNameCacheMap.clear();
    }


    public static Builder builder(){
        return new Builder();
    }


    public static class Builder{
        public CouchbaseViewDaoFactory build(){
            return new CouchbaseViewDaoFactory(this);
        }
    }

    private class ViewKey<T extends CouchbaseDocument>{
        private final String domain;
        private final Class<T> className;
        private final String viewName;

        public ViewKey(String domain, Class<T> className,String viewName) {
            this.domain = domain;
            this.className = className;
            this.viewName = viewName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ViewKey<?> viewKey = (ViewKey<?>) o;

            if (!domain.equals(viewKey.domain)) return false;
            if (!className.equals(viewKey.className)) return false;
            return viewName.equals(viewKey.viewName);
        }

        @Override
        public int hashCode() {
            int result = domain.hashCode();
            result = 31 * result + className.hashCode();
            result = 31 * result + viewName.hashCode();
            return result;
        }
    }

    private class ViewKeyClass<T extends CouchbaseDocument>{
        private final String domain;
        private final Class<T> aClass;

        public ViewKeyClass(String domain, Class<T> aClass) {
            this.domain = domain;
            this.aClass = aClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ViewKeyClass that = (ViewKeyClass) o;

            if (!domain.equals(that.domain)) return false;
            return aClass.equals(that.aClass);
        }

        @Override
        public int hashCode() {
            int result = domain.hashCode();
            result = 31 * result + aClass.hashCode();
            return result;
        }
    }
}
