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

package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.couchbase.exception.StorageObservableException;
import com.dreameddeath.core.couchbase.impl.ReadParams;
import com.dreameddeath.core.couchbase.impl.WriteParams;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.DaoObservableException;
import com.dreameddeath.core.dao.exception.InconsistentStateException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.exception.validation.ValidationObservableException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.java.utils.ClassUtils;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocument.DocumentFlag;
import rx.Observable;
import rx.functions.Func1;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


/**
 * Created by Christophe Jeunesse on 12/10/2014.
 */
@DaoForClass(CouchbaseDocument.class)
public abstract class CouchbaseDocumentDao<T extends CouchbaseDocument>{
    private final Class<T> baseClass;
    private final UUID uuid = UUID.randomUUID();
    private ICouchbaseBucket client;
    private List<CouchbaseViewDao> daoViews=null;
    private boolean isReadOnly=false;

    public abstract Class<? extends BucketDocument<T>> getBucketDocumentClass();
    public abstract Observable<T> asyncBuildKey(ICouchbaseSession session,T newObject) throws DaoException;

    public final T buildKey(ICouchbaseSession session,T newObject) throws DaoException,StorageException{
        return asyncBuildKey(session,newObject).toBlocking().first();
    }

    public CouchbaseDocumentDao() {
        baseClass=ClassUtils.getEffectiveGenericType(this.getClass(),CouchbaseDocumentDao.class,0);
    }

    public final Class<T> getBaseClass(){
        return baseClass;
    }

    public List<CouchbaseCounterDao.Builder> getCountersBuilder(){return Collections.emptyList();}
    public List<CouchbaseUniqueKeyDao.Builder> getUniqueKeysBuilder(){return Collections.emptyList();}
    protected List<CouchbaseViewDao> generateViewDaos(){ return Collections.emptyList();}


    public void init(){
        daoViews = generateViewDaos();
    }

    public List<CouchbaseViewDao> getViewDaos(){
        return Collections.unmodifiableList(daoViews);
    }

    public CouchbaseViewDao getViewDao(String name){
        for(CouchbaseViewDao viewDao:daoViews){
            if(viewDao.getViewName().equals(name)){
                return viewDao;
            }
        }
        return null;
    }

    public CouchbaseDocumentDao<T> setClient(ICouchbaseBucket client){
        this.client = client;
        return this;
    }

    public ICouchbaseBucket getClient(){ return client; }

    public UUID getUuid() {
        return uuid;
    }

    public void checkUpdatableState(T obj) throws InconsistentStateException {
        if(obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
            throw new InconsistentStateException(obj,"The document is new and then cannot be updated");
        }
        if(obj.getBaseMeta().getKey()==null){
            throw new InconsistentStateException(obj,"The document is doesn't have a key and then cannot be deleted");
        }
        if(obj.getBaseMeta().hasFlag(DocumentFlag.Deleted) ||
                obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.DELETED)){
            throw new InconsistentStateException(obj,"The document is in deletion and then cannot be modified");
        }
        if(isReadOnly){
            throw new InconsistentStateException(obj,"Cannot update document in readonly mode");
        }
    }

    public T checkCreatableState(T obj) throws InconsistentStateException{
        if(!obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
            throw new InconsistentStateException(obj,"The document is not new and then cannot be create");
        }
        if(isReadOnly){
            throw new InconsistentStateException(obj,"Cannot update document in readonly mode");
        }
        return obj;
    }

    public T checkDeletableState(T obj) throws InconsistentStateException{
        if(obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
            throw new InconsistentStateException(obj,"The document is new and then cannot be deleted");
        }
        if(obj.getBaseMeta().getKey()==null){
            throw new InconsistentStateException(obj,"The document is doesn't have a key and then cannot be deleted");
        }
        if(obj.getBaseMeta().hasFlag(DocumentFlag.Deleted) ||
                obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.DELETED)){
            throw new InconsistentStateException(obj,"The document is already in deletion and then cannot be deleted");
        }
        if(isReadOnly){
            throw new InconsistentStateException(obj,"Cannot update document in readonly mode");
        }
        return obj;
    }

    public class FuncUpdateStateSync implements Func1<T,T> {
        @Override
        public T call(T t) {
            t.getBaseMeta().setStateSync();
            return t;
        }
    }

    public class FuncCheckCreatable implements Func1<T,T> {
        @Override
        public T call(T t) {
            try{
                checkCreatableState(t);
                return t;
            }
            catch (InconsistentStateException e){
                throw new DaoObservableException(e);
            }
        }
    }


    public class FuncCheckUpdatable implements Func1<T,T> {
        @Override
        public T call(T t) {
            try{
                checkUpdatableState(t);
                return t;
            }
            catch (InconsistentStateException e){
                throw new DaoObservableException(e);
            }
        }
    }

    public class FuncCheckDeletable implements Func1<T,T> {
        @Override
        public T call(T t) {
            try{
                checkDeletableState(t);
                return t;
            }
            catch (InconsistentStateException e){
                throw new DaoObservableException(e);
            }

        }
    }


    public class FuncValidate implements Func1<T,T> {
        private final ICouchbaseSession session;

        public FuncValidate(ICouchbaseSession session){
            this.session  = session;
        }
        @Override
        public T call(T t) {
            try {
                session.validate(t);
                return t;
            }
            catch(ValidationException e){
                throw new ValidationObservableException(e);
            }
        }
    }

    public Observable<T> asyncCreate(ICouchbaseSession session, T obj, boolean isCalcOnly){
        Observable<T> result=Observable.just(obj);
        result = result.map(new FuncCheckCreatable());
        if(obj.getBaseMeta().getKey()==null) {
            result = result.flatMap(val -> {
                        try {
                            return asyncBuildKey(session, val);
                        } catch (DaoException e) {
                            throw new DaoObservableException(e);
                        }
                    }
            );
        }
        result = result.map(new FuncValidate(session));
        if(!isCalcOnly) {
            final String keyPrefix = session.getKeyPrefix();
            if(session.getKeyPrefix()!=null) {
                result = result.flatMap(val -> getClient().asyncAdd(val, WriteParams.create().with(keyPrefix)));
            }
            else {
                result = result.flatMap(val -> getClient().asyncAdd(val));
            }
        }
        result = result.map(new FuncUpdateStateSync());
        return result.onErrorResumeNext(throwable -> mapObservableException(obj,throwable));
    }

    public <T extends CouchbaseDocument> Observable<T> mapObservableException(T doc,Throwable e){
        if(e instanceof ValidationObservableException){
            throw (ValidationObservableException)e;
        }
        else if(e instanceof DaoObservableException){
            throw (DaoObservableException)e;
        }
        return ICouchbaseBucket.Utils.mapObservableStorageException(doc,e);
    }

    public T create(ICouchbaseSession session,T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        try {
            return asyncCreate(session, obj, isCalcOnly).toBlocking().first();
        }
        catch(DaoObservableException e){
            throw e.getCause();
        }
        catch(ValidationObservableException e){
            throw e.getCause();
        }
        catch(StorageObservableException e){
            throw e.getCause();
        }
    }

    public T get(ICouchbaseSession session,String key) throws DaoException,StorageException{
        try{
            return asyncGet(session,key).toBlocking().first();
        }
        catch (StorageObservableException e) {
            throw e.getCause();
        }
        catch(DaoObservableException e){
            throw e.getCause();
        }
    }

    public Observable<T> asyncGet(ICouchbaseSession session,String key){
        try {
            Observable<T> result;
            if (session.getKeyPrefix() != null) {
                result = getClient().asyncGet(key, baseClass, ReadParams.create().with(session.getKeyPrefix()));
            }
            else {
                result = getClient().asyncGet(key, baseClass);
            }
            result = result.map(val -> {
                        if (val.getBaseMeta().hasFlag(DocumentFlag.Deleted)) {
                            val.getBaseMeta().setStateDeleted();
                        }
                        else {
                            val.getBaseMeta().setStateSync();
                        }
                        return val;
                    }
            );

            return result;
        }
        catch(Throwable e){
            throw new DaoObservableException(new DaoException("Unexpected exception",e));
        }
    }

    public T update(ICouchbaseSession session,T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        try {
            return asyncUpdate(session, obj, isCalcOnly).toBlocking().first();
        }
        catch(DaoObservableException e){
            throw e.getCause();
        }
        catch(ValidationObservableException e){
            throw e.getCause();
        }
        catch (StorageObservableException e){
            throw e.getCause();
        }
    }

    public Observable<T> asyncUpdate(ICouchbaseSession session,T obj,boolean isCalcOnly){
        try {
            Observable<T> result = Observable.just(obj);
            result = result.map(new FuncCheckUpdatable());
            result = result.map(new FuncValidate(session));
            if (!isCalcOnly) {
                final String keyPrefix = session.getKeyPrefix();
                if (keyPrefix != null) {
                    result = result.flatMap(val -> getClient().asyncReplace(val, WriteParams.create().with(keyPrefix)));
                }
                else {
                    result = result.flatMap(val -> getClient().asyncReplace(val));
                }
            }
            return result.map(new FuncUpdateStateSync())
                    .onErrorResumeNext(throwable -> mapObservableException(obj,throwable));
        }
        catch(Throwable e){
            throw new DaoObservableException(new DaoException("Unexpected exception",e));
        }
    }

    public Observable<T> asyncDelete(ICouchbaseSession session,T obj,boolean isCalcOnly){
        try {
            Observable<T> result = Observable.just(obj);
            result = result.map(new FuncCheckDeletable());
            result = result.map(val -> {
                val.getBaseMeta().addFlag(DocumentFlag.Deleted);
                return val;
            });
            if (!isCalcOnly) {
                result = result.flatMap(val -> getClient().asyncDelete(val));
            }
            result = result.map(val -> {
                val.getBaseMeta().setStateDeleted();
                return val;
            });
            return result.onErrorResumeNext(throwable -> mapObservableException(obj,throwable));
        }
        catch(Throwable e){
            throw new DaoObservableException(new DaoException("Unexpected exception",e));
        }
    }

    //Should only be used through DeletionJob
    public T delete(ICouchbaseSession session,T obj,boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
        try {
            return asyncDelete(session, obj, isCalcOnly).toBlocking().first();
        }
        catch(DaoObservableException e){
            throw e.getCause();
        }
        catch(ValidationObservableException e){
            throw e.getCause();
        }
        catch(StorageObservableException e){
            throw e.getCause();
        }
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void isReadOnly(boolean isReadOnly) {
        this.isReadOnly=isReadOnly;
    }


}
