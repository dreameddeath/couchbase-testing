package com.dreameddeath.core.dao.common;


import com.dreameddeath.core.annotation.dao.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDaoFactory;
import com.dreameddeath.core.exception.dao.DaoNotFoundException;
import com.dreameddeath.core.model.common.RawCouchbaseDocument;
import com.dreameddeath.core.storage.impl.GenericCouchbaseTranscoder;
import com.dreameddeath.core.transcoder.ITranscoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class BaseCouchbaseDocumentDaoFactory{
    private Map<Class<? extends RawCouchbaseDocument>, BaseCouchbaseDocumentDao<?>> _daosMap
            = new ConcurrentHashMap<Class<? extends RawCouchbaseDocument>, BaseCouchbaseDocumentDao<?>>();
    private Map<Pattern,BaseCouchbaseDocumentWithKeyPatternDao<?>> _patternsMap
            = new ConcurrentHashMap<Pattern,BaseCouchbaseDocumentWithKeyPatternDao<?>>();

    private CouchbaseCounterDaoFactory _counterDaoFactory;
    private CouchbaseUniqueKeyDaoFactory _uniqueKeyDaoFactory;

    public CouchbaseCounterDaoFactory getCounterDaoFactory(){
        return _counterDaoFactory;
    }
    public void setCounterDaoFactory(CouchbaseCounterDaoFactory factory){ _counterDaoFactory = factory; }

    public CouchbaseUniqueKeyDaoFactory getUniqueKeyDaoFactory(){return _uniqueKeyDaoFactory;}
    public void setUniqueKeyDaoFactory(CouchbaseUniqueKeyDaoFactory factory){_uniqueKeyDaoFactory=factory;}

    public void registerCounter(CouchbaseCounterDao counterDao){
        _counterDaoFactory.addDao(counterDao);
    }

    public <T extends RawCouchbaseDocument> void addDao(BaseCouchbaseDocumentDao<T> dao,ITranscoder<T> transcoder) {
        DaoForClass annotation = dao.getClass().getAnnotation(DaoForClass.class);
        dao.setTranscoder(new GenericCouchbaseTranscoder<T>(transcoder,dao.getBucketDocumentClass()));
        addDaoFor((Class<T>)annotation.value(),dao);
    }

    public <T extends RawCouchbaseDocument> void addDaoFor(Class<T> entityClass,BaseCouchbaseDocumentDao<T> dao){
        _daosMap.put(entityClass,dao);
        if(dao instanceof BaseCouchbaseDocumentWithKeyPatternDao){
            _patternsMap.put(Pattern.compile("^"+((BaseCouchbaseDocumentWithKeyPatternDao) dao).getKeyPattern()+"$"),(BaseCouchbaseDocumentWithKeyPatternDao)dao);
        }
        for(CouchbaseCounterDao.Builder daoCounterBuilder:dao.getCountersBuilder()){
            registerCounter(daoCounterBuilder.build());
        }
        for(CouchbaseUniqueKeyDao.Builder daoUniqueKeyBuilder:dao.getUniqueKeysBuilder()){
            _uniqueKeyDaoFactory.addDaoFor(daoUniqueKeyBuilder.getNameSpace(),daoUniqueKeyBuilder.build());
        }
    }

    public <T extends RawCouchbaseDocument> BaseCouchbaseDocumentDao<T> getDaoForClass(Class<T> entityClass) throws DaoNotFoundException{
        BaseCouchbaseDocumentDao<T> result = (BaseCouchbaseDocumentDao<T>)_daosMap.get(entityClass);
        if(result==null){
            Class parentClass=entityClass.getSuperclass();
            if(RawCouchbaseDocument.class.isAssignableFrom(parentClass)){
                result = getDaoForClass(parentClass.asSubclass(RawCouchbaseDocument.class));
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