/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.session.impl;

import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.document.IDaoForDocumentWithUID;
import com.dreameddeath.core.dao.document.IDaoWithKeyPattern;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.DaoObservableException;
import com.dreameddeath.core.dao.exception.ReadOnlyException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.exception.validation.ValidationObservableException;
import com.dreameddeath.core.dao.factory.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.factory.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.model.view.IViewAsyncQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.date.IDateTimeService;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.DuplicateUniqueKeyException;
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

    private Map<String,CouchbaseDocument> sessionCache = new ConcurrentHashMap<>();
    private Map<String,CouchbaseUniqueKey> keyCache = new ConcurrentHashMap<>();
    private Map<String,Long> counters = new ConcurrentHashMap<>();

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
        sessionFactory = factory;
        dateTimeService = factory.getDateTimeServiceFactory().getService();
        sessionType = type;
        this.user = user;
        this.keyPrefix = keyPrefix;
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
        return sessionType== SessionType.READ_ONLY;
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

    @Override
    public long getCounter(String key) throws DaoException,StorageException {
        return asyncGetCounter(key).toBlocking().first();
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
    public long incrCounter(String key, long byVal) throws DaoException,StorageException {
        return asyncIncrCounter(key,byVal).toBlocking().first();
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
    public long decrCounter(String key, long byVal) throws DaoException,StorageException {
        return asyncDecrCounter(key,byVal).toBlocking().first();
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

    public <T extends CouchbaseDocument> T attachDocument(T doc){
        if(doc.getBaseMeta().getKey()!=null){
            sessionCache.put(doc.getBaseMeta().getKey(),doc);
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
            attachDocument(entity);
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
    public <T extends CouchbaseDocument> T create(T obj) throws ValidationException,DaoException,StorageException {
        return manageAsyncResult(obj,asyncCreate(obj));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncCreate(T obj) throws DaoException {
        checkReadOnly(obj);
        CouchbaseDocumentDao<T> dao = sessionFactory.getDocumentDaoFactory().getDaoForClass((Class<T>)obj.getClass());
        Observable<T> result = dao.asyncCreate(this,obj,isCalcOnly());
        result = result.map(this::attachDocument);
        return result;
    }

    @Override
    public <T extends CouchbaseDocument> T buildKey(T obj) throws DaoException,StorageException {
        return asyncBuildKey(obj).toBlocking().first();
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncBuildKey(T obj) throws DaoException {
        if(obj.getBaseMeta().getState()== CouchbaseDocument.DocumentState.NEW){
            return sessionFactory.getDocumentDaoFactory().getDaoForClass((Class<T>)obj.getClass()).asyncBuildKey(this, obj);
        }
        else{
            return Observable.just(obj);
        }
    }

    @Override
    public CouchbaseDocument get(String key) throws DaoException,StorageException {
        return asyncGet(key).toBlocking().first();
    }

    @Override
    public Observable<CouchbaseDocument> asyncGet(String key) throws DaoException {
        CouchbaseDocument cachedObj = sessionCache.get(key);
        if(cachedObj!=null) {
            return Observable.just(cachedObj);
        }
        else{
            CouchbaseDocumentDao dao = sessionFactory.getDocumentDaoFactory().getDaoForKey(key);
            Observable<CouchbaseDocument> result = (Observable<CouchbaseDocument>)dao.asyncGet(this,key);
            result.doOnNext(this::attachDocument);
            return result;
        }
    }

    @Override
    public <T extends CouchbaseDocument> T get(String key, Class<T> targetClass) throws DaoException,StorageException {
        return asyncGet(key,targetClass).toBlocking().first();
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGet(String key, Class<T> targetClass) throws DaoException {
        CouchbaseDocument cacheResult = sessionCache.get(key);
        if(cacheResult !=null){
            return Observable.just((T)cacheResult);
        }
        else{
            CouchbaseDocumentDao<T> dao = sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
            Observable<T> result = dao.asyncGet(this,key);
            result.doOnNext(this::attachDocument);
            return result;
        }
    }

    @Override
    public <T extends CouchbaseDocument> T update(T obj)throws ValidationException,DaoException,StorageException {
        return manageAsyncResult(obj,asyncUpdate(obj));
    }

    public <T extends CouchbaseDocument> T manageAsyncResult(final T obj,Observable<T> obs)throws ValidationException,DaoException,StorageException {
        try{
            return obs.toBlocking().first();
        }
        catch(DaoObservableException e){
            throw (DaoException) e.getCause();
        }
        catch(ValidationObservableException e){
            throw (ValidationException) e.getCause();
        }
        catch (Throwable e){
            throw ICouchbaseBucket.Utils.mapStorageException(obj,e);
        }
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncUpdate(T obj) throws DaoException {
        checkReadOnly(obj);
        CouchbaseDocumentDao<T> dao = sessionFactory.getDocumentDaoFactory().getDaoForClass((Class<T>) obj.getClass());
        return dao.asyncUpdate(this, obj, isCalcOnly());
    }

    @Override
    public <T extends CouchbaseDocument> T delete(T obj)throws ValidationException,DaoException,StorageException {
        return manageAsyncResult(obj,asyncDelete(obj));
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncDelete(T obj) throws DaoException {
        checkReadOnly(obj);
        CouchbaseDocumentDao<T> dao = sessionFactory.getDocumentDaoFactory().getDaoForClass((Class<T>) obj.getClass());
        return dao.asyncDelete(this, obj, isCalcOnly());
    }

    @Override
    public void validate(CouchbaseDocument doc) throws ValidationException {
        sessionFactory.getValidatorFactory().getValidator((Class<CouchbaseDocument>)doc.getClass()).validate(ValidatorContext.buildContext(this),doc);
    }

    @Override
    public <T extends CouchbaseDocument> T getFromUID(String uid, Class<T> targetClass) throws DaoException,StorageException {
        IDaoForDocumentWithUID dao = (IDaoForDocumentWithUID) sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
        return get(dao.getKeyFromUID(uid), targetClass);
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGetFromUID(String uid, Class<T> targetClass) throws DaoException, StorageException {
        @SuppressWarnings("unchecked")
        IDaoForDocumentWithUID<T> dao = (IDaoForDocumentWithUID<T>) sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
        final String key = dao.getKeyFromUID(uid);
        CouchbaseDocument cacheResult = sessionCache.get(key);
        if(cacheResult !=null){
            return Observable.just((T)cacheResult);
        }
        else {
            return dao.asyncGetFromUid(this, uid).doOnNext(this::attachDocument);
        }
    }

    @Override
    public <T extends CouchbaseDocument> String getKeyFromUID(String uid, Class<T> targetClass) throws DaoException{
        IDaoForDocumentWithUID dao = (IDaoForDocumentWithUID) sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
        return dao.getKeyFromUID(uid);
    }


    @Override
    public <T extends CouchbaseDocument> T getFromKeyParams(Class<T> targetClass, Object... params) throws DaoException, StorageException {
        return asyncGetFromKeyParams(targetClass,params).toBlocking().first();
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncGetFromKeyParams(Class<T> targetClass, Object... params) throws DaoException, StorageException {
        @SuppressWarnings("unchecked")
        IDaoWithKeyPattern<T> dao = (IDaoWithKeyPattern<T>) sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
        final String key = dao.getKeyFromParams(params);
        CouchbaseDocument cacheResult = sessionCache.get(key);
        if(cacheResult !=null){
            return Observable.just((T)cacheResult);
        }
        else {
            return dao.asyncGetFromKeyParams(this, params).doOnNext(this::attachDocument);
        }
    }

    @Override
    public <T extends CouchbaseDocument> String getKeyFromKeyParams(Class<T> targetClass, Object... params) throws DaoException {
        IDaoWithKeyPattern dao = (IDaoWithKeyPattern) sessionFactory.getDocumentDaoFactory().getDaoForClass(targetClass);
        return dao.getKeyFromParams(params);
    }


    @Override
    public <T extends CouchbaseDocument> T save(T obj) throws ValidationException,DaoException,StorageException {
        return asyncSave(obj).toBlocking().first();
    }

    @Override
    public <T extends CouchbaseDocument> Observable<T> asyncSave(T obj) throws DaoException {
        if(obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
            return asyncCreate(obj);
        }
        else if(obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.DELETED)){
            return asyncDelete(obj);
        }
        else{
            return asyncUpdate(obj);
        }
    }

    @Override
    public void addOrUpdateUniqueKey(CouchbaseDocument doc, Object value, String nameSpace)throws ValidationException,DaoException,StorageException,DuplicateUniqueKeyException{
        //Skip null value
        if(value==null){
            return;
        }
        checkReadOnly(doc);
        CouchbaseUniqueKeyDao dao = sessionFactory.getUniqueKeyDaoFactory().getDaoFor(nameSpace);
        CouchbaseUniqueKey keyDoc =dao.addOrUpdateUniqueKey(this, nameSpace, value.toString(), doc, isCalcOnly());
        keyCache.put(keyDoc.getBaseMeta().getKey(),keyDoc);
    }



    @Override
    public CouchbaseUniqueKey getUniqueKey(String internalKey)throws DaoException,StorageException {
        CouchbaseUniqueKey keyDoc =keyCache.get(sessionFactory.getUniqueKeyDaoFactory().getDaoForInternalKey(internalKey).buildKey(internalKey));
        if(keyDoc==null){
            CouchbaseUniqueKeyDao dao = sessionFactory.getUniqueKeyDaoFactory().getDaoForInternalKey(internalKey);
            return dao.getFromInternalKey(this,internalKey);
        }
        return keyDoc;
    }


    @Override
    public void removeUniqueKey(String internalKey) throws ValidationException,DaoException,StorageException {
        CouchbaseUniqueKey obj = getUniqueKey(internalKey);
        checkReadOnly(obj);
        CouchbaseUniqueKeyDao dao = sessionFactory.getUniqueKeyDaoFactory().getDaoForInternalKey(internalKey);
        dao.removeUniqueKey(this,obj,internalKey,isCalcOnly());
        if(obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.DELETED)){
            keyCache.remove(obj.getBaseMeta().getKey());
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
    public <TKEY,TVALUE,T extends CouchbaseDocument> Observable<IViewAsyncQueryResult<TKEY,TVALUE,T >> executeAsyncQuery(IViewQuery<TKEY,TVALUE,T> query) throws DaoException,StorageException {
        return query.getDao().asyncQuery(this,isCalcOnly(),query);
    }
}
