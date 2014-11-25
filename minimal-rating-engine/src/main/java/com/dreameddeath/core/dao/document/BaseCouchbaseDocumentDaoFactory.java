package com.dreameddeath.core.dao.document;


import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.validation.ValidatorFactory;
import com.dreameddeath.core.exception.dao.DaoNotFoundException;
import com.dreameddeath.core.model.document.BaseCouchbaseDocument;
import com.dreameddeath.core.model.business.CouchbaseDocument;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class BaseCouchbaseDocumentDaoFactory{
    private Map<Class<? extends BaseCouchbaseDocument>, BaseCouchbaseDocumentDao<?>> _daosMap
            = new ConcurrentHashMap<Class<? extends BaseCouchbaseDocument>, BaseCouchbaseDocumentDao<?>>();
    private Map<Pattern,BaseCouchbaseDocumentWithKeyPatternDao<?>> _patternsMap
            = new ConcurrentHashMap<Pattern,BaseCouchbaseDocumentWithKeyPatternDao<?>>();

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

    public <T extends BaseCouchbaseDocument> void addDaoFor(Class<T> entityClass,BaseCouchbaseDocumentDao<T> dao){
        _daosMap.put(entityClass,dao);
        if(dao instanceof BaseCouchbaseDocumentWithKeyPatternDao){
            _patternsMap.put(Pattern.compile("^"+((BaseCouchbaseDocumentWithKeyPatternDao) dao).getKeyPattern()+"$"),(BaseCouchbaseDocumentWithKeyPatternDao)dao);
        }
    }

    public <T extends BaseCouchbaseDocument> BaseCouchbaseDocumentDao<T> getDaoForClass(Class<T> entityClass) throws DaoNotFoundException{
        BaseCouchbaseDocumentDao<T> result = (BaseCouchbaseDocumentDao<T>)_daosMap.get(entityClass);
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

    public BaseCouchbaseDocumentWithKeyPatternDao getDaoForKey(String key) throws DaoNotFoundException {
        for(Pattern pattern:_patternsMap.keySet()){
            if(pattern.matcher(key).matches()){
                return _patternsMap.get(pattern);
            }
        }
        throw new DaoNotFoundException(key, DaoNotFoundException.Type.DOC);
    }
}