package com.dreameddeath.common.dao;

import java.util.Map;
import java.util.HashMap;

import com.dreameddeath.common.model.CouchbaseDocument;

public class CouchbaseDocumentDaoFactory{
    private Map<Class<? extends CouchbaseDocument>, CouchbaseDocumentDao<?>> _daosMap
            = new HashMap<Class<? extends CouchbaseDocument>, CouchbaseDocumentDao<?>>();
    
    public <T extends CouchbaseDocument> void addDaoFor(Class<T> entityClass,CouchbaseDocumentDao<T> dao){
        _daosMap.put(entityClass,dao);
    }
    
    
    public <T extends CouchbaseDocument> CouchbaseDocumentDao<T> getDaoFor(Class<T> entityClass) {
        return (CouchbaseDocumentDao<T>) _daosMap.get(entityClass);
    }
}