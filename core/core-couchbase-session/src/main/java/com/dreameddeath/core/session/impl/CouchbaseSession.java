/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.session.impl;

import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.document.IDaoForDocumentWithUID;
import com.dreameddeath.core.dao.document.IDaoWithKeyPattern;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.DaoNotFoundException;
import com.dreameddeath.core.dao.exception.DaoObservableException;
import com.dreameddeath.core.dao.exception.ReadOnlyException;
import com.dreameddeath.core.dao.exception.validation.ValidationObservableException;
import com.dreameddeath.core.dao.factory.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.factory.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.model.view.IViewAsyncQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.dao.session.IBlockingCouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.date.IDateTimeService;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.validation.ValidatorContext;
import org.joda.time.DateTime;
import rx.Observable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CouchbaseSession implements ICouchbaseSession {
    final private CouchbaseSessionFactory sessionFactory;
    final private String keyPrefix;
    final private SessionType sessionType;
    final private IDateTimeService dateTimeService;
    final private IUser user;
    final private IBlockingCouchbaseSession blockingSession;

    private final BucketDocumentCache sessionCache; //= new BucketDocumentCache(this);
    private final Map<String,CouchbaseUniqueKey> keyCache; //= new ConcurrentHashMap<>();
    private final Map<String,Long> counters; //= new ConcurrentHashMap<>();
    private volatile boolean temporaryReadOnlyMode;

    public CouchbaseSession(CouchbaseSessionFactory factory, IUser user){
        this(factory, SessionType.READ_ONLY,user);
    }

    public CouchbaseSession(CouchbaseSessionFactory factory, IUser user,String keyPrefix){
        this(factory, SessionType.READ_ONLY,user,keyPrefix);
    }

    public CouchbaseSession(CouchbaseSessionFactory factory, SessionType type, IUser user){
        this(factory, type, user,null);
    }

    public CouchbaseSession(CouchbaseSessionFactory factory, SessionType type, IUser user,String keyPrefix) {
        this(factory, type, user,keyPrefix,new ConcurrentHashMap<>(),new ConcurrentHashMap<>(),null);
    }

    private CouchbaseSession(CouchbaseSessionFactory factory, SessionType type, IUser user,String keyPrefix,
                             Map<String,CouchbaseUniqueKey> keyCache,Map<String,Long> counters,BucketDocumentCache sessionCache) {
        this.sessionFactory = factory;
        this.dateTimeService = factory.getDateTimeServiceFactory().getService();
        this.sessionType = type;
        this.user = user;
        this.keyPrefix = keyPrefix;
        this.blockingSession = new BlockingCouchbaseSession(this);
        this.sessionCache=(sessionCache!=null)?sessionCache:new BucketDocumentCache(this);
        this.keyCache=keyCache;
        this.counters=counters;
    }

    @Override
    public IBlockingCouchbaseSession toBlocking() {
        return blockingSession;
    }

    protected CouchbaseDocumentDaoFactory getDocumentFactory(){
        return sessionFactory.getDocumentDaoFactory();
    }

    protected CouchbaseCounterDaoFactory getCounterDaoFactory(){
        return sessionFactory.getCounterDaoFactory();
    }

    @Override
    public void reset(){
        sessionCache.clear();
        counters.clear();
    }

    @Override
    public String getKeyPrefix() {
        return keyPrefix;
    }

    @Override
    public SessionType getSessionType() {
        return sessionType;
    }


    public boolean isCalcOnly(){
        return sessionType== SessionType.CALC_ONLY;
    }
    public boolean isReadOnly(){
        return (sessionType== SessionType.READ_ONLY);
    }

    protected void checkReadOnly(CouchbaseDocument doc) throws ReadOnlyException{
        if(isReadOnly()){
            throw new ReadOnlyException(doc);
        }
    }

    protected void checkReadOnly(String counterKey) throws ReadOnlyException{
        if(isReadOnly()){
            throw new ReadOnlyException(counterKey);
        }
    }


    @Override
    public Observable<Long> asyncGetCounter(String key) throws DaoException {
        CouchbaseCounterDao dao = sessionFactory.getCounterDaoFactory().getDaoForKey(key);
        if(isCalcOnly() && counters.containsKey(key)){
            return Observable.just(counters.get(key));
        }
        Observable<Long> result = dao.asyncGetCounter(this,key,isCalcOnly());

        if(isCalcOnly()){
            result.doOnNext(val->counters.put(key,val));
        }
        return result;
    }


    @Override
    public Observable<Long> asyncIncrCounter(String key, long byVal) throws DaoException {
        checkReadOnly(key);
        if(isCalcOnly()){
            Observable<Long> result = asyncGetCounter(key);
            result = result.map(val->val+byVal);
            result.doOnNext(val->counters.put(key,val));
            return result;
        }
        else{
            CouchbaseCounterDao dao = sessionFactory.getCounterDaoFactory().getDaoForKey(key);
            return dao.asyncIncrCounter(this,key, byVal, isCalcOnly());
        }
    }


    @Override
    public Observable<Long> asyncDecrCounter(String key, long byVal) throws DaoException {
        checkReadOnly(key);
        if(isCalcOnly()){
            Observable<Long> result = asyncGetCounter(key).map(val->val-byVal);
            result.doOnNext(val->counters.put(key,val));
            return result;
        }
        else{
            CouchbaseCounterDao dao = sessionFactory.getCounterDaoFactory().getDaoForKey(key);
            return dao.asyncDecrCounter(this,key, byVal, isCalcOnly());
        }
    }

    public <T extends CouchbaseDocument> T updateCache(T doc){
        if(doc.getBaseMeta().getKey()!=null){
            sessionCache.put(doc);
        }
        return doc;
    }
    

    @Override
    public <T extends CouchbaseDocument> T newEntity(Class<T> clazz){
        try{
            return attachEntity(clazz.newInstance());
        }
        catch(Exception e){
            ///TODO log something
            return null;
        }
    }


    @Override
    public <T extends CouchbaseDocument> T attachEntity(T entity){
        try{
            updateCache(entity);
            return entity;
        }
        catch(Exception e){
            ///TODO log something
            return null;
        }
    }

    @Override
    public IUser getUser() {
        return user;
    }


    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncCreate(T obj){
        try {
            checkReadOnly(obj);
            CouchbaseDocumentDao<T> dao = sessionFactory.getDocumentDaoFactory().getDaoForClass((Class<T>) obj.getClass());
            return dao.asyncCreate(this, obj, isCalcOnly()).map(this::updateCache);
        }
        catch (DaoException e){
            throw new DaoObservableException(e);
        }
    }


    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncBuildKey(T obj) {
        if(obj.getBaseMeta().getState()== CouchbaseDocument.DocumentState.NEW){
            try {
                return sessionFactory.getDocumentDaoFactory().getDaoForClass((Class<T>) obj.getClass()).asyncBuildKey(this, obj);
            }
            catch(DaoException e){
                throw new DaoObservableException(e);
            }
        }
        else{
            return Observable.just(obj);
        }
    }


    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGet(String key){
        try {
            T cachedObj = sessionCache.get(key);
            if (cachedObj != null) {
                return Observable.just(cachedObj);
            } else {
                CouchbaseDocumentDao<T> dao = sessionFactory.getDocumentDaoFactory().getDaoForKey(key);
                Observable<T> result = dao.asyncGet(this, key);
                result.doOnNext(this::updateCache);
                return result;
            }
        }
        catch(DaoException e){
            throw new DaoObservableException(e);
        }
    }


    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGet(String key, Class<T> targetClass){
        try {
            CouchbaseDocument cacheResult = sessionCache.get(key);
            if (cacheResult != null) {
                return Observable.just((T) cacheResult);
            } else {
                CouchbaseDocumentDao<T> dao = sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
                Observable<T> result = dao.asyncGet(this, key);
                result.doOnNext(this::updateCache);
                return result;
            }
        }
        catch(DaoException e){
            throw new DaoObservableException(e);
        }
    }



    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncRefresh(T doc){
        try {
            CouchbaseDocumentDao<T> dao = sessionFactory.getDocumentDaoFactory().getDaoForClass((Class<T>) doc.getClass());
            Observable<T> result = dao.asyncGet(this, doc.getBaseMeta().getKey());
            result.doOnNext(this::updateCache);
            return result;
        }
        catch(DaoException e){
            throw new DaoObservableException(e);
        }
    }



    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncUpdate(T obj){
        try {
            checkReadOnly(obj);
            CouchbaseDocumentDao<T> dao = sessionFactory.getDocumentDaoFactory().getDaoForClass((Class<T>) obj.getClass());
            return dao.asyncUpdate(this, obj, isCalcOnly()).map(this::updateCache);
        }
        catch(DaoException e){
            throw new DaoObservableException(e);
        }
    }


    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncDelete(T obj) {
        try {
            checkReadOnly(obj);
            CouchbaseDocumentDao<T> dao = sessionFactory.getDocumentDaoFactory().getDaoForClass((Class<T>) obj.getClass());
            return dao.asyncDelete(this, obj, isCalcOnly()).map(this::updateCache);
        }
        catch(DaoException e){
            throw new DaoObservableException(e);
        }
    }


    @Override
    public <T extends CouchbaseDocument>  Observable<T> asyncValidate(final T doc){
        return
                sessionFactory.getValidatorFactory().getValidator((Class<T>)doc.getClass())
                        .asyncValidate(ValidatorContext.buildContext(this),doc)
                        .doOnNext(validation->{
                            throw new ValidationObservableException(validation);}
                        )
                        .map(validation->doc)
                        .firstOrDefault(doc);
    }



    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGetFromUID(String uid, Class<T> targetClass){
        try {
            @SuppressWarnings("unchecked")
            IDaoForDocumentWithUID<T> dao = (IDaoForDocumentWithUID<T>) sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
            final String key = dao.getKeyFromUID(uid);
            CouchbaseDocument cacheResult = sessionCache.get(key);
            if (cacheResult != null) {
                return Observable.just((T) cacheResult);
            } else {
                return dao.asyncGetFromUid(this, uid).doOnNext(this::updateCache);
            }
        }
        catch(DaoException e){
            throw new DaoObservableException(e);
        }
    }

    @Override
    public <T extends CouchbaseDocument> String getKeyFromUID(String uid, Class<T> targetClass) throws DaoException{
        IDaoForDocumentWithUID dao = (IDaoForDocumentWithUID) sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
        return dao.getKeyFromUID(uid);
    }



    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGetFromKeyParams(Class<T> targetClass, Object... params){
        try {
            @SuppressWarnings("unchecked")
            IDaoWithKeyPattern<T> dao = (IDaoWithKeyPattern<T>) sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
            final String key = dao.getKeyFromParams(params);
            CouchbaseDocument cacheResult = sessionCache.get(key);
            if (cacheResult != null) {
                return Observable.just((T) cacheResult);
            } else {
                return dao.asyncGetFromKeyParams(this, params).doOnNext(this::updateCache);
            }
        }
        catch(DaoException e){
            throw new DaoObservableException(e);
        }
    }

    @Override
    public <T extends CouchbaseDocument> String getKeyFromKeyParams(Class<T> targetClass, Object... params) throws DaoException {
        IDaoWithKeyPattern dao = (IDaoWithKeyPattern) sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
        return dao.getKeyFromParams(params);
    }



    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncSave(T obj){
        if(obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
            return asyncCreate(obj);
        }
        else if(obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.DELETED)){
            return asyncDelete(obj);
        }
        else if(obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.SYNC)){
            return Observable.just(obj);
        }
        else{
            return asyncUpdate(obj);
        }
    }


    @Override
    public <T extends CouchbaseDocument> String buildUniqueKey(T doc,String value, String nameSpace) throws DaoException{
        CouchbaseUniqueKeyDao dao;
        try {
            dao = sessionFactory.getUniqueKeyDaoFactory().getDaoFor(nameSpace);
        }
        catch(DaoNotFoundException e){
            dao = sessionFactory.getUniqueKeyDaoFactory().getDaoFor(nameSpace, sessionFactory.getDocumentDaoFactory().getDaoForClass(doc.getClass()));
        }

        return dao.buildInternalKey(nameSpace,value);
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncAddOrUpdateUniqueKey(final T doc, String value, String nameSpace) {
        //Skip null value
        if(value==null){
            return Observable.just(doc);
        }
        try {
            checkReadOnly(doc);
        }
        catch(ReadOnlyException e){
            throw new DaoObservableException(e);
        }
        CouchbaseUniqueKeyDao dao;
        try {
            dao = sessionFactory.getUniqueKeyDaoFactory().getDaoFor(nameSpace);
        }
        catch(DaoNotFoundException e){
            try {
                dao = sessionFactory.getUniqueKeyDaoFactory().getDaoFor(nameSpace, sessionFactory.getDocumentDaoFactory().getDaoForClass(doc.getClass()));
            }
            catch(DaoNotFoundException e1){
                throw new DaoObservableException(e);
            }
        }
        Observable<CouchbaseUniqueKey> keyDocObservable = dao.asyncAddOrUpdateUniqueKey(this, nameSpace, value.toString(), doc, isCalcOnly());

        //keyCache.put(keyDoc.getBaseMeta().getKey(),keyDoc);
        return keyDocObservable.map(keyDoc->{
            keyCache.put(keyDoc.getBaseMeta().getKey(),keyDoc);
            return doc;
        });
    }

    @Override
    public Observable<CouchbaseUniqueKey> asyncGetUniqueKey(String internalKey) {
        try {
            CouchbaseUniqueKey keyDoc = keyCache.get(sessionFactory.getUniqueKeyDaoFactory().getDaoForInternalKey(internalKey).buildKey(internalKey));
            if (keyDoc == null) {
                CouchbaseUniqueKeyDao dao = sessionFactory.getUniqueKeyDaoFactory().getDaoForInternalKey(internalKey);
                return dao.asyncGetFromInternalKey(this, internalKey);
            }
            return Observable.just(keyDoc);
        }
        catch(DaoException e){
            throw new DaoObservableException(e);
        }
    }

    @Override
    public Observable<Boolean> asyncRemoveUniqueKey(String internalKey) {
        try {
            final CouchbaseUniqueKeyDao dao = sessionFactory.getUniqueKeyDaoFactory().getDaoForInternalKey(internalKey);
            return asyncGetUniqueKey(internalKey).map(keyDoc -> {
                        try {
                            checkReadOnly(keyDoc);
                        } catch (ReadOnlyException e) {
                            throw new DaoObservableException(e);
                        }
                        return keyDoc;
                    })
                    .flatMap(keyDoc -> dao.asyncRemoveUniqueKey(this, keyDoc, internalKey, isCalcOnly()))
                    .map(keyDoc -> keyCache.remove(keyDoc.getBaseMeta().getKey()) != null);
        }
        catch(DaoNotFoundException e){
            throw new DaoObservableException(e);
        }
    }

    @Override
    public DateTime getCurrentDate() {
        return null;
    }

    public IDateTimeService getDateTimeService(){ return dateTimeService; }

    @Override
    public <TKEY,TVALUE,T extends CouchbaseDocument> IViewQuery<TKEY,TVALUE,T> initViewQuery(Class<T> forClass,String viewName) throws DaoException{
        CouchbaseViewDao<TKEY,TVALUE,T> viewDao = (CouchbaseViewDao<TKEY,TVALUE,T>)sessionFactory.getDocumentDaoFactory().getViewDaoFactory().getViewDaoFor(forClass,viewName);
        return viewDao.buildViewQuery(keyPrefix);
    }

    @Override
    public <TKEY,TVALUE,T extends CouchbaseDocument> IViewQueryResult<TKEY,TVALUE,T> executeQuery(IViewQuery<TKEY,TVALUE,T> query){
        return query.getDao().query(this,isCalcOnly(),query);
    }

    @Override
    public <TKEY,TVALUE,T extends CouchbaseDocument> Observable<IViewAsyncQueryResult<TKEY,TVALUE,T >> executeAsyncQuery(IViewQuery<TKEY,TVALUE,T> query) {
        return query.getDao().asyncQuery(this,isCalcOnly(),query);
    }


    @Override
    public ICouchbaseSession getTemporaryReadOnlySession() {
        return new TemporaryCouchbaseSession(sessionFactory,sessionType,user,keyPrefix,keyCache,counters,sessionCache);
    }

    @Override
    public ICouchbaseSession toParentSession() {
        throw new IllegalStateException();
    }

    public <T extends CouchbaseDocument> ICouchbaseBucket getClientForClass(Class<T> clazz){
        try {
            return sessionFactory.getDocumentDaoFactory().getDaoForClass(clazz).getClient();
        }
        catch(DaoNotFoundException e){
            return null;
        }
    }

    private class TemporaryCouchbaseSession extends CouchbaseSession implements ICouchbaseSession{
        private final IBlockingCouchbaseSession blockingCouchbaseSession;

        private TemporaryCouchbaseSession(CouchbaseSessionFactory factory, SessionType type, IUser user, String keyPrefix, Map<String, CouchbaseUniqueKey> keyCache, Map<String, Long> counters, BucketDocumentCache sessionCache) {
            super(factory, type, user, keyPrefix, keyCache, counters, sessionCache);
            this.blockingCouchbaseSession = new BlockingCouchbaseSession(this);
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        @Override
        public <T extends CouchbaseDocument> T updateCache(T doc){
            return CouchbaseSession.this.updateCache(doc);
        }

        @Override
        public IBlockingCouchbaseSession toBlocking() {
            return blockingCouchbaseSession;
        }

        @Override
        public ICouchbaseSession getTemporaryReadOnlySession() {
            return this;
        }

        @Override
        public ICouchbaseSession toParentSession() {
            return CouchbaseSession.this;
        }
    }
}
