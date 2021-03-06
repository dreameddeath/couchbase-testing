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

package com.dreameddeath.core;

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.business.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.business.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDao;
import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.dao.exception.dao.DaoNotFoundException;
import com.dreameddeath.core.dao.exception.dao.ReadOnlyException;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.date.DateTimeService;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.business.CouchbaseDocumentLink;
import com.dreameddeath.core.model.document.BaseCouchbaseDocument;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.user.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CouchbaseSession {
    final private CouchbaseSessionFactory sessionFactory;
    final private SessionType sessionType;
    final private DateTimeService dateTimeService;
    final private User user;

    private Map<String,BaseCouchbaseDocument> sessionCache = new HashMap<String,BaseCouchbaseDocument>();
    private Map<String,CouchbaseUniqueKey> keyCache = new HashMap<String,CouchbaseUniqueKey>();
    private Map<String,Set<CouchbaseDocumentLink>> links = new HashMap<String,Set<CouchbaseDocumentLink>>();
    private Map<String,Long> counters = new HashMap<String, Long>();

    public CouchbaseSession(CouchbaseSessionFactory factory,User user){
        this(factory,SessionType.READ_ONLY,user);
    }

    public CouchbaseSession(CouchbaseSessionFactory factory,SessionType type,User user){
        sessionFactory = factory;
        dateTimeService = factory.getDateTimeServiceFactory().getService();
        sessionType = type;
        this.user = user;
    }

    protected BaseCouchbaseDocumentDaoFactory getDocumentFactory(){
        return sessionFactory.getDocumentDaoFactory();
    }

    protected CouchbaseCounterDaoFactory getCounterDaoFactory(){
        return sessionFactory.getCounterDaoFactory();
    }

    protected void clean(){
        sessionCache.clear();
        links.clear();
        counters.clear();
    }

    public boolean isCalcOnly(){
        return sessionType==SessionType.CALC_ONLY;
    }
    public boolean isReadOnly(){
        return sessionType==SessionType.READ_ONLY;
    }
    protected void checkReadOnly(BaseCouchbaseDocument doc) throws ReadOnlyException{
        if(isReadOnly()){
            throw new ReadOnlyException(doc);
        }
    }

    protected void checkReadOnly(Class docClass) throws ReadOnlyException{
        if(isReadOnly()){
            throw new ReadOnlyException(docClass);
        }
    }


    protected void checkReadOnly(String counterKey) throws ReadOnlyException{
        if(isReadOnly()){
            throw new ReadOnlyException(counterKey);
        }
    }




    public long getCounter(String key) throws DaoNotFoundException{
        CouchbaseCounterDao dao = sessionFactory.getCounterDaoFactory().getDaoForKey(key);
        if(isCalcOnly() && counters.containsKey(key)){
            return counters.get(key);
        }
        Long value = dao.getCounter(key,isCalcOnly());
        if(isCalcOnly()){
            counters.put(key,value);
        }
        return value;
    }


    public long incrCounter(String key, long byVal) throws ReadOnlyException, DaoNotFoundException{
        checkReadOnly(key);
        if(isCalcOnly()){
            Long result = getCounter(key);
            result+=byVal;
            counters.put(key,result);
            return result;
        }
        else{
            CouchbaseCounterDao dao = sessionFactory.getCounterDaoFactory().getDaoForKey(key);
            return dao.incrCounter(key,byVal,isCalcOnly());
        }
    }


    public long decrCounter(String key, long byVal) throws ReadOnlyException, DaoNotFoundException{
        checkReadOnly(key);
        if(isCalcOnly()){
            Long result = getCounter(key);
            result-=byVal;
            counters.put(key,result);
            return result;
        }
        else{
            CouchbaseCounterDao dao = sessionFactory.getCounterDaoFactory().getDaoForKey(key);
            return dao.decrCounter(key, byVal,isCalcOnly());
        }
    }

    public void attachLink(CouchbaseDocumentLink link){
        if(link.getKey()!=null){
            if((link.getLinkedObjectFromCache()==null) && (sessionCache.containsKey(link.getKey()))){
                link.setLinkedObject(sessionCache.get(link.getKey()));
            }
            if(links.containsKey(link.getKey())){
                links.get(link.getKey()).add(link);
            }
            else{
                Set<CouchbaseDocumentLink> linkSet=new HashSet<CouchbaseDocumentLink>();
                linkSet.add(link);
                links.put(link.getKey(),linkSet);
            }
        }
    }

    public void attachDocumentToLinks(CouchbaseDocument doc){
        if(links.containsKey(doc.getBaseMeta().getKey())){
            for(CouchbaseDocumentLink link:links.get(doc.getBaseMeta().getKey())){
                link.setLinkedObject(doc);
            }
        }
    }
    
    public void attachDocument(BaseCouchbaseDocument doc){
        doc.getBaseMeta().setSession(this);
        if(doc.getBaseMeta().getKey()!=null){
            sessionCache.put(doc.getBaseMeta().getKey(),doc);
        }
        for(CouchbaseDocumentLink link:doc.getChildElementsOfType(CouchbaseDocumentLink.class)){
            attachLink(link);
        }
        if(doc instanceof CouchbaseDocument) {
            attachDocumentToLinks((CouchbaseDocument)doc);
        }
    }
    

    public <T extends BaseCouchbaseDocument> T newEntity(Class<T> clazz){
        try{
            T obj=clazz.newInstance();
            attachDocument(obj);
            return obj;
        }
        catch(Exception e){
            ///TODO log something
            return null;
        }
    }


    public <T extends BaseCouchbaseDocument> T create(T obj) throws DaoException,StorageException{
        checkReadOnly(obj);
        BaseCouchbaseDocumentDao<T> dao = (BaseCouchbaseDocumentDao<T>) sessionFactory.getDocumentDaoFactory().getDaoForClass(obj.getClass());
        dao.create(obj,isCalcOnly());
        attachDocument(obj);
        return obj;
    }
    
    /*public <T extends CouchbaseDocument> Collection<T> create(Collection<T> objs,Class<T> targetClass) throws DaoException,StorageException{
        checkReadOnly(targetClass);
        CouchbaseDocumentDao<T> dao = _sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
        dao.createBulk(objs,isCalcOnly());
        for(T obj:objs){
            attachDocument(obj);
        }
        return objs;
    }*/


    public <T extends CouchbaseDocument> T buildKey(T obj) throws DaoException,StorageException{
        if(obj.getBaseMeta().getState()== CouchbaseDocument.DocumentState.NEW){
            ((CouchbaseDocumentDao<T>) sessionFactory.getDocumentDaoFactory().getDaoForClass(obj.getClass())).buildKey(obj);
        }
        return obj;
    }
    

    public BaseCouchbaseDocument get(String key) throws DaoException,StorageException{
        BaseCouchbaseDocument result = sessionCache.get(key);
        if(result==null){
            BaseCouchbaseDocumentDao dao = sessionFactory.getDocumentDaoFactory().getDaoForKey(key);
            result = dao.get(key);
            attachDocument(result);
        }
        return result;
    }
    

    public <T extends BaseCouchbaseDocument> T get(String key, Class<T> targetClass) throws DaoException,StorageException{
        BaseCouchbaseDocument cacheResult = sessionCache.get(key);
        if(cacheResult !=null){
            return (T)cacheResult;
        }
        else{
            BaseCouchbaseDocumentDao<T> dao = sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
            T result = dao.get(key);
            attachDocument(result);
            return result;
        }
    }

    /*public <T extends CouchbaseDocument> Collection<T> update(Collection<T> objs,Class<T> targetClass)throws DaoException,StorageException{
        checkReadOnly(targetClass);
        CouchbaseDocumentDao<T> dao = _sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
        dao.updateBulk(objs,isCalcOnly());
        return objs;
    }*/


    public <T extends BaseCouchbaseDocument> T update(T obj)throws DaoException,StorageException{
        checkReadOnly(obj);
        BaseCouchbaseDocumentDao<T> dao = sessionFactory.getDocumentDaoFactory().getDaoForClass((Class<T>) obj.getClass());
        dao.update(obj,isCalcOnly());
        return obj;
    }


    public <T extends BaseCouchbaseDocument> T delete(T obj)throws DaoException,StorageException{
        checkReadOnly(obj);
        BaseCouchbaseDocumentDao<T> dao = sessionFactory.getDocumentDaoFactory().getDaoForClass((Class<T>) obj.getClass());
        dao.delete(obj,isCalcOnly());
        return obj;
    }



    public <T extends CouchbaseDocument> T getFromUID(String uid, Class<T> targetClass) throws DaoException,StorageException{
        CouchbaseDocumentDaoWithUID<T> dao = (CouchbaseDocumentDaoWithUID) sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
        return get(dao.getKeyFromUID(uid),targetClass);
    }


    public <T extends CouchbaseDocument> String getKeyFromUID(String uid, Class<T> targetClass) throws DaoException{
        CouchbaseDocumentDaoWithUID<T> dao = (CouchbaseDocumentDaoWithUID) sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
        return dao.getKeyFromUID(uid);
    }


    public <T extends CouchbaseDocument> T save(T obj) throws DaoException,StorageException{
        if(obj.getBaseMeta().getState().equals(BaseCouchbaseDocument.DocumentState.NEW)){
            return create(obj);
        }
        else if(obj.getBaseMeta().getState().equals(BaseCouchbaseDocument.DocumentState.DELETED)){
            return delete(obj);
        }
        else{
            return update(obj);
        }
    }




    public void addOrUpdateUniqueKey(CouchbaseDocument doc, Object value, String nameSpace)throws DaoException,StorageException{
        //Skip null value
        if(value==null){
            return;
        }
        checkReadOnly(doc);
        CouchbaseUniqueKeyDao dao = sessionFactory.getUniqueKeyDaoFactory().getDaoFor(nameSpace);
        CouchbaseUniqueKey keyDoc =dao.addOrUpdateUniqueKey(nameSpace,value.toString(),doc,isCalcOnly());
        keyCache.put(keyDoc.getBaseMeta().getKey(),keyDoc);
    }



    public CouchbaseUniqueKey getUniqueKey(String internalKey)throws DaoException,StorageException{
        CouchbaseUniqueKey keyDoc =keyCache.get(sessionFactory.getUniqueKeyDaoFactory().getDaoForInternalKey(internalKey).buildKey(internalKey));
        if(keyDoc==null){
            CouchbaseUniqueKeyDao dao = sessionFactory.getUniqueKeyDaoFactory().getDaoForInternalKey(internalKey);
            return dao.getFromInternalKey(internalKey);
        }
        return keyDoc;
    }


    public void removeUniqueKey(String internalKey) throws DaoException,StorageException{
        CouchbaseUniqueKey obj = getUniqueKey(internalKey);
        checkReadOnly(obj);
        CouchbaseUniqueKeyDao dao = sessionFactory.getUniqueKeyDaoFactory().getDaoForInternalKey(internalKey);
        dao.removeUniqueKey(obj,internalKey,isCalcOnly());
        if(obj.getBaseMeta().getState().equals(BaseCouchbaseDocument.DocumentState.DELETED)){
            keyCache.remove(obj.getBaseMeta().getKey());
        }
    }


    public enum SessionType{
        READ_ONLY,
        CALC_ONLY,
        READ_WRITE
    }

    public DateTimeService getDateTimeService(){ return dateTimeService; }
}
