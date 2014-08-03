package com.dreameddeath.common.dao;

import com.dreameddeath.common.model.document.CouchbaseDocument;
import com.dreameddeath.common.model.process.AbstractJob;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

//import java.util.AbstractMap.SimpleEntry;

public class CouchbaseDocumentDaoFactory{
    private Map<Class<? extends CouchbaseDocument>, CouchbaseDocumentDao<?>> _daosMap
            = new HashMap<Class<? extends CouchbaseDocument>, CouchbaseDocumentDao<?>>();
    private Map<Pattern,CouchbaseDocumentDao<?>> _patternsMap 
            = new HashMap<Pattern,CouchbaseDocumentDao<?>>();


    public CouchbaseSession newSession(){
        return new CouchbaseSession(this);
    }
    
    public <T extends CouchbaseDocument> void addDaoFor(Class<T> entityClass,CouchbaseDocumentDao<T> dao){
        _daosMap.put(entityClass,dao);
        _patternsMap.put(Pattern.compile("^"+dao.getKeyPattern()+"$"),dao);
    }
    
    public <T extends CouchbaseDocument> CouchbaseDocumentDao<T> getDaoForClass(Class<T> entityClass) {
        CouchbaseDocumentDao<T> result = (CouchbaseDocumentDao<T>)_daosMap.get(entityClass);
        if(result==null){
            Class parentClass=entityClass.getSuperclass();
            if(CouchbaseDocument.class.isAssignableFrom(parentClass)){
                result = getDaoForClass(parentClass.asSubclass(CouchbaseDocument.class));
                if(result!=null){
                    _daosMap.put(entityClass,result);
                }
            }
        }
        if(result==null){
            throw new NoSuchElementException("Cannot found dao for class <"+entityClass.getName()+">");
        }
        return result;
    }
    
    public CouchbaseDocumentDao getDaoForKey(String key) {
        for(Pattern pattern:_patternsMap.keySet()){
            if(pattern.matcher(key).matches()){
                return _patternsMap.get(pattern);
            }
        }
        throw new NoSuchElementException("Cannot found dao for key <"+key+">");

    }
}