package com.dreameddeath.common.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.CouchbaseDocumentLink;

public class CouchbaseSession {
    private Map<String,CouchbaseDocument> _sessionCache = new HashMap<String,CouchbaseDocument>();
    //private Map<String,List<CouchbaseDocumentLink>> _dandlingLinks = new HashMap<String,List<CouchbaseDocumentLink>>();
    private CouchbaseDocumentDaoFactory _daoFactory;
    
    public CouchbaseSession(CouchbaseDocumentDaoFactory daoFactory){
        _daoFactory = daoFactory;
    }
    
    public void attachDocument(CouchbaseDocument doc){
        if(doc.getKey()!=null){
            _sessionCache.put(doc.getKey(),doc);
        }
        doc.setSession(this);
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
    
    
    public <T extends CouchbaseDocument> T create(T obj){
        CouchbaseDocumentDao<T> dao = (CouchbaseDocumentDao<T>)_daoFactory.getDaoForClass(obj.getClass());
        dao.create(obj);
        attachDocument(obj);
        return obj;
    }
    
    public <T extends CouchbaseDocument> Collection<T> create(Collection<T> objs,Class<T> targetClass){
        CouchbaseDocumentDao<T> dao = _daoFactory.getDaoForClass(targetClass);
        dao.createBulk(objs);
        for(T obj:objs){
            attachDocument(obj);
        }
        return objs;
    }
    
    public CouchbaseDocument get(String key){
        CouchbaseDocument result = _sessionCache.get(key);
        if(result==null){
            CouchbaseDocumentDao dao = _daoFactory.getDaoForKey(key);
            result = dao.get(key);
            attachDocument(result);
        }
        return result;
    }
    
    public <T extends CouchbaseDocument> T get(String key,Class<T> targetClass){
        CouchbaseDocument cacheResult = _sessionCache.get(key);
        if(cacheResult !=null){
            return (T)cacheResult;
        }
        else{
            CouchbaseDocumentDao<T> dao = _daoFactory.getDaoForClass(targetClass);
            T result = dao.get(key);
            attachDocument(result);
            return result;
        }
    }
    
    
}
