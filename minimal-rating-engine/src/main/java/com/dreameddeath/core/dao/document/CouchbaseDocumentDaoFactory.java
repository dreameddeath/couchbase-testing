package com.dreameddeath.core.dao.document;


import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.validation.ValidatorFactory;
import com.dreameddeath.core.exception.dao.DaoNotFoundException;
import com.dreameddeath.core.model.document.CouchbaseDocument;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class CouchbaseDocumentDaoFactory{
    private Map<Class<? extends CouchbaseDocument>, CouchbaseDocumentDao<?>> _daosMap
            = new ConcurrentHashMap<Class<? extends CouchbaseDocument>, CouchbaseDocumentDao<?>>();
    private Map<Pattern,CouchbaseDocumentDao<?>> _patternsMap 
            = new ConcurrentHashMap<Pattern,CouchbaseDocumentDao<?>>();

    private ValidatorFactory _validatorFactory;
    private CouchbaseCounterDaoFactory _counterDaoFactory;

    public void setValidatorFactory(ValidatorFactory factory){
        _validatorFactory = factory;
    }

    public ValidatorFactory getValidatorFactory(){
        return _validatorFactory;
    }

    public CouchbaseCounterDaoFactory getCounterDaoFactory(){
        return _counterDaoFactory;
    }

    public void setCounterDaoFactory(CouchbaseCounterDaoFactory factory){
        _counterDaoFactory = factory;
    }

    public void registerCounter(CouchbaseCounterDao counterDao){
        _counterDaoFactory.addDao(counterDao);
    }

    public <T extends CouchbaseDocument> void addDaoFor(Class<T> entityClass,CouchbaseDocumentDao<T> dao){
        _daosMap.put(entityClass,dao);
        _patternsMap.put(Pattern.compile("^"+dao.getKeyPattern()+"$"),dao);
    }
    
    public <T extends CouchbaseDocument> CouchbaseDocumentDao<T> getDaoForClass(Class<T> entityClass) throws DaoNotFoundException{
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
            throw new DaoNotFoundException(entityClass);
        }
        return result;
    }
    
    public CouchbaseDocumentDao getDaoForKey(String key) throws DaoNotFoundException {
        for(Pattern pattern:_patternsMap.keySet()){
            if(pattern.matcher(key).matches()){
                return _patternsMap.get(pattern);
            }
        }
        throw new DaoNotFoundException(key,true);
    }
}