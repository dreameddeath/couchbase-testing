package com.dreameddeath.common.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;

import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.CouchbaseDocumentLink;

public class CouchbaseSession {
    private Map<String,CouchbaseDocument> _sessionCache = new HashMap<String,CouchbaseDocument>();
    private Map<String,Set<CouchbaseDocumentLink>> _links = new HashMap<String,Set<CouchbaseDocumentLink>>();
    private CouchbaseDocumentDaoFactory _daoFactory;
    
    public CouchbaseSession(CouchbaseDocumentDaoFactory daoFactory){
        _daoFactory = daoFactory;
    }
    
    public void attachLink(CouchbaseDocumentLink link){
        if(link.getKey()!=null){
            System.out.println("Adding link "+link.getClass().getName() + " for key "+link.getKey()+" with value "+link);
            if((link.getLinkedObject(true)==null) && (_sessionCache.containsKey(link.getKey()))){
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
