package com.dreameddeath.core.dao.document;


import com.dreameddeath.core.annotation.dao.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDaoFactory;
import com.dreameddeath.core.exception.dao.DaoNotFoundException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.storage.impl.GenericCouchbaseTranscoder;
import com.dreameddeath.core.transcoder.ITranscoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class CouchbaseDocumentDaoFactory {
    private Map<Class<? extends CouchbaseDocument>, CouchbaseDocumentDao<?>> _daosMap
            = new ConcurrentHashMap<Class<? extends CouchbaseDocument>, CouchbaseDocumentDao<?>>();
    private Map<Pattern,CouchbaseDocumentWithKeyPatternDao<?>> _patternsMap
            = new ConcurrentHashMap<Pattern,CouchbaseDocumentWithKeyPatternDao<?>>();

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

    public <T extends CouchbaseDocument> void addDao(CouchbaseDocumentDao<T> dao,ITranscoder<T> transcoder) {
        DaoForClass annotation = dao.getClass().getAnnotation(DaoForClass.class);
        if(annotation==null){
            throw new NullPointerException("Annotation DaoForClass not defined for dao <"+dao.getClass().getName()+">");
        }
        dao.setTranscoder(new GenericCouchbaseTranscoder<T>(transcoder,dao.getBucketDocumentClass()));
        addDaoFor((Class<T>)annotation.value(),dao);
    }

    public <T extends CouchbaseDocument> void addDaoFor(Class<T> entityClass,CouchbaseDocumentDao<T> dao){
        _daosMap.put(entityClass,dao);
        if(dao instanceof CouchbaseDocumentWithKeyPatternDao){
            _patternsMap.put(Pattern.compile("^"+((CouchbaseDocumentWithKeyPatternDao) dao).getKeyPattern()+"$"),(CouchbaseDocumentWithKeyPatternDao)dao);
        }
        for(CouchbaseCounterDao.Builder daoCounterBuilder:dao.getCountersBuilder()){
            registerCounter(daoCounterBuilder.build());
        }
        for(CouchbaseUniqueKeyDao.Builder daoUniqueKeyBuilder:dao.getUniqueKeysBuilder()){
            _uniqueKeyDaoFactory.addDaoFor(daoUniqueKeyBuilder.getNameSpace(),daoUniqueKeyBuilder.build());
        }
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

    public CouchbaseDocumentWithKeyPatternDao getDaoForKey(String key) throws DaoNotFoundException {
        for(Pattern pattern:_patternsMap.keySet()){
            if(pattern.matcher(key).matches()){
                return _patternsMap.get(pattern);
            }
        }
        throw new DaoNotFoundException(key, DaoNotFoundException.Type.DOC);
    }
}