package com.dreameddeath.core.dao;

import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.dao.DaoNotFoundException;
import com.dreameddeath.core.exception.dao.ReadOnlyException;
import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentLink;

import java.util.*;

public class CouchbaseSession {
    final private CouchbaseDocumentDaoFactory _documentDaoFactory;
    final private CouchbaseCounterDaoFactory _counterDaoFactory;
    final private SessionType _sessionType;

    private Map<String,CouchbaseDocument> _sessionCache = new HashMap<String,CouchbaseDocument>();
    private Map<String,Set<CouchbaseDocumentLink>> _links = new HashMap<String,Set<CouchbaseDocumentLink>>();
    private Map<String,Long> _counters = new HashMap<String, Long>();

    public CouchbaseSession(CouchbaseDocumentDaoFactory docDaoFactory,CouchbaseCounterDaoFactory counterDaoFactory){
        this(docDaoFactory,counterDaoFactory,SessionType.READ_ONLY);
    }

    public CouchbaseSession(CouchbaseDocumentDaoFactory daoFactory,CouchbaseCounterDaoFactory counterDaoFactory,SessionType type){
        _documentDaoFactory = daoFactory;
        _counterDaoFactory = counterDaoFactory;
        _sessionType = type;
    }

    protected CouchbaseDocumentDaoFactory getDocumentFactory(){
        return _documentDaoFactory;
    }

    protected CouchbaseCounterDaoFactory getCounterDaoFactory(){
        return _counterDaoFactory;
    }

    protected void clean(){
        _sessionCache.clear();
        _links.clear();
        _counters.clear();
    }

    public boolean isCalcOnly(){
        return _sessionType==SessionType.CALC_ONLY;
    }

    public boolean isReadOnly(){
        return _sessionType==SessionType.READ_ONLY;
    }

    protected void checkReadOnly(CouchbaseDocument doc) throws ReadOnlyException{
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
        CouchbaseCounterDao dao = _counterDaoFactory.getDaoForKey(key);
        if(isCalcOnly() && _counters.containsKey(key)){
            return _counters.get(key);
        }
        Long value = dao.getCounter(key,isCalcOnly());
        if(isCalcOnly()){
            _counters.put(key,value);
        }
        return value;
    }

    public long incrCounter(String key,long byVal) throws ReadOnlyException, DaoNotFoundException{
        checkReadOnly(key);
        if(isCalcOnly()){
            Long result = getCounter(key);
            result+=byVal;
            _counters.put(key,result);
            return result;
        }
        else{
            CouchbaseCounterDao dao = _counterDaoFactory.getDaoForKey(key);
            return dao.incrCounter(key,byVal,isCalcOnly());
        }
    }

    public long decrCounter(String key, long byVal) throws ReadOnlyException, DaoNotFoundException{
        checkReadOnly(key);
        if(isCalcOnly()){
            Long result = getCounter(key);
            result-=byVal;
            _counters.put(key,result);
            return result;
        }
        else{
            CouchbaseCounterDao dao = _counterDaoFactory.getDaoForKey(key);
            return dao.decrCounter(key, byVal,isCalcOnly());
        }
    }

    public void attachLink(CouchbaseDocumentLink link){
        if(link.getKey()!=null){
            if((link.getLinkedObjectFromCache()==null) && (_sessionCache.containsKey(link.getKey()))){
                link.setLinkedObject(_sessionCache.get(link.getKey()));
            }
            if(_links.containsKey(link.getKey())){
                _links.get(link.getKey()).add(link);
            }
            else{
                Set<CouchbaseDocumentLink> linkSet=new HashSet<CouchbaseDocumentLink>();
                linkSet.add(link);
                _links.put(link.getKey(),linkSet);
            }
        }
    }

    public void attachDocumentToLinks(CouchbaseDocument doc){
        if(_links.containsKey(doc.getKey())){
            for(CouchbaseDocumentLink link:_links.get(doc.getKey())){
                link.setLinkedObject(doc);
            }
        }
    }
    
    public void attachDocument(CouchbaseDocument doc){
        doc.setSession(this);
        if(doc.getKey()!=null){
            _sessionCache.put(doc.getKey(),doc);
        }
        for(CouchbaseDocumentLink link:doc.getChildElementsOfType(CouchbaseDocumentLink.class)){
            attachLink(link);
        }
        attachDocumentToLinks(doc);
    }
    
    public <T extends CouchbaseDocument> T newEntity(Class<T> clazz){
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

    public <T extends CouchbaseDocument> T create(T obj) throws DaoException,StorageException{
        checkReadOnly(obj);
        CouchbaseDocumentDao<T> dao = (CouchbaseDocumentDao<T>) _documentDaoFactory.getDaoForClass(obj.getClass());
        dao.create(obj,isCalcOnly());
        attachDocument(obj);
        return obj;
    }
    
    public <T extends CouchbaseDocument> Collection<T> create(Collection<T> objs,Class<T> targetClass) throws DaoException,StorageException{
        checkReadOnly(targetClass);
        CouchbaseDocumentDao<T> dao = _documentDaoFactory.getDaoForClass(targetClass);
        dao.createBulk(objs,isCalcOnly());
        for(T obj:objs){
            attachDocument(obj);
        }
        return objs;
    }

    public <T extends CouchbaseDocument> T buildKey(T obj) throws DaoException,StorageException{
        if(obj.getState()== CouchbaseDocument.State.NEW){
            ((CouchbaseDocumentDao<T>) _documentDaoFactory.getDaoForClass(obj.getClass())).buildKey(obj);
        }
        return obj;
    }
    
    public CouchbaseDocument get(String key) throws DaoException,StorageException{
        CouchbaseDocument result = _sessionCache.get(key);
        if(result==null){
            CouchbaseDocumentDao dao = _documentDaoFactory.getDaoForKey(key);
            result = dao.get(key);
            attachDocument(result);
        }
        return result;
    }
    
    public <T extends CouchbaseDocument> T get(String key,Class<T> targetClass) throws DaoException,StorageException{
        CouchbaseDocument cacheResult = _sessionCache.get(key);
        if(cacheResult !=null){
            return (T)cacheResult;
        }
        else{
            CouchbaseDocumentDao<T> dao = _documentDaoFactory.getDaoForClass(targetClass);
            T result = dao.get(key);
            attachDocument(result);
            return result;
        }
    }

    public <T extends CouchbaseDocument> Collection<T> update(Collection<T> objs,Class<T> targetClass)throws DaoException,StorageException{
        checkReadOnly(targetClass);
        CouchbaseDocumentDao<T> dao = _documentDaoFactory.getDaoForClass(targetClass);

        dao.updateBulk(objs,isCalcOnly());

        return objs;
    }

    public <T extends CouchbaseDocument> T update(T obj)throws DaoException,StorageException{
        checkReadOnly(obj);
        CouchbaseDocumentDao<T> dao = _documentDaoFactory.getDaoForClass((Class<T>)obj.getClass());
        dao.update(obj,isCalcOnly());
        return obj;
    }

    public <T extends CouchbaseDocument> T getFromUID(String uid,Class<T> targetClass) throws DaoException,StorageException{
        CouchbaseDocumentDaoWithUID<T> dao = (CouchbaseDocumentDaoWithUID) _documentDaoFactory.getDaoForClass(targetClass);
        return get(dao.getKeyFromUID(uid),targetClass);
    }

    public void addUniqueKey(CouchbaseDocument doc,Object value,String nameSpace){

    }

    public enum SessionType{
        READ_ONLY,
        CALC_ONLY,
        READ_WRITE
    }
}
