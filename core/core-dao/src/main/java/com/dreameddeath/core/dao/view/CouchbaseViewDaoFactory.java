/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.dao.view;

import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.exception.DaoNotFoundException;
import com.dreameddeath.core.dao.exception.DuplicateDaoException;
import com.dreameddeath.core.model.document.CouchbaseDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ceaj8230 on 18/12/2014.
 */
public class CouchbaseViewDaoFactory {
    private Map<Class<? extends CouchbaseDocument>, List<CouchbaseViewDao<?,? extends CouchbaseDocument,?>>> _daosMap
            = new ConcurrentHashMap<>();
    private Map<String,CouchbaseViewDao> _perClassNameAndNameCacheMap=new ConcurrentHashMap<>();


    public <T extends CouchbaseDocument> void addDao(CouchbaseViewDao dao){
        DaoForClass annotation = dao.getParentDao().getClass().getAnnotation(DaoForClass.class);
        if(annotation==null){
            throw new NullPointerException("Annotation DaoForClass not defined for dao <"+dao.getParentDao().getClass().getName()+">");
        }
        addDaoFor(annotation.value(), dao);
    }

    public <T extends CouchbaseDocument> void addDaoFor(Class<T> entityClazz,CouchbaseViewDao dao){
        if(!_daosMap.containsKey(entityClazz)){
            _daosMap.put(entityClazz,new ArrayList<>());
        }

        for(CouchbaseViewDao existingDao:_daosMap.get(entityClazz)){
            if(existingDao.getViewName().equals(dao.getViewName())){
                throw new DuplicateDaoException("The dao view <"+dao.getViewName()+"> is already existing for class "+entityClazz.getName());
            }
        }
        _daosMap.get(entityClazz).add(dao);
    }

    public <T extends CouchbaseDocument> List<CouchbaseViewDao> getViewListDaoFor(Class<T> entityClass){
        List<CouchbaseViewDao> result = (List)_daosMap.get(entityClass);
        if(result==null){
            Class parentClass=entityClass.getSuperclass();
            if(CouchbaseDocument.class.isAssignableFrom(parentClass)){
                result = getViewListDaoFor(parentClass.asSubclass(CouchbaseDocument.class));
                if(result!=null){
                    _daosMap.put(entityClass,(List)result);
                }
            }
        }
        return (List)result;
    }

    public <T extends CouchbaseDocument> CouchbaseViewDao getViewDaoFor(Class<T> entityClass,String viewName) throws DaoNotFoundException{
        CouchbaseViewDao result = _perClassNameAndNameCacheMap.get(entityClass.getName()+viewName);
        if(result==null) {
            List<CouchbaseViewDao> list = (List) getViewListDaoFor(entityClass);
            if(list != null){
                for(CouchbaseViewDao view:list){
                    if(view.getViewName().equals(viewName)){
                        result=view;
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

        for(List list:_daosMap.values()){
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
}
