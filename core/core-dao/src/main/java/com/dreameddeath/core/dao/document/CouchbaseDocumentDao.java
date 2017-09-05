/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.dao.document;

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.couchbase.impl.ReadParams;
import com.dreameddeath.core.couchbase.impl.WriteParams;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.InconsistentStateException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.java.utils.ClassUtils;
import com.dreameddeath.core.model.annotation.HasEffectiveDomain;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocument.DocumentFlag;
import com.dreameddeath.core.model.entity.EntityDefinitionManager;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.entity.model.IVersionedEntity;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Created by Christophe Jeunesse on 12/10/2014.
 */
@DaoForClass(CouchbaseDocument.class)
public abstract class CouchbaseDocumentDao<T extends CouchbaseDocument>{
    private final Class<T> baseClass;
    private final UUID uuid = UUID.randomUUID();
    private final EntityDef rootEntity;
    private final List<EntityDef> childEntities=new ArrayList<>();
    private String domain;
    private ICouchbaseBucket client;
    private List<CouchbaseViewDao> daoViews=null;
    private boolean isReadOnly=false;
    private EntityDefinitionManager entityDefinitionManager=null;

    public abstract Class<? extends BucketDocument<T>> getBucketDocumentClass();
    public abstract Single<T> asyncBuildKey(ICouchbaseSession session,T newObject) throws DaoException;

    public EntityDef getRootEntity() {
        return rootEntity;
    }

    public void setEntityManager(EntityDefinitionManager manager) {
        this.entityDefinitionManager=manager;
        resyncChildren();
    }

    public List<EntityDef> getChildEntities() {
        return Collections.unmodifiableList(childEntities);
    }

    public final T blockingBuildKey(ICouchbaseSession session, T newObject) throws DaoException,StorageException{
        try {
            return asyncBuildKey(session, newObject).blockingGet();
        }
        catch(RuntimeException e){
            Throwable eCause=e.getCause();
            if(eCause!=null){
                if(eCause instanceof DaoException){
                    throw (DaoException)eCause;
                }
                if(eCause instanceof StorageException){
                    throw (StorageException)eCause;
                }
            }
            throw e;
        }
    }

    public CouchbaseDocumentDao() {
        baseClass=ClassUtils.getEffectiveGenericType(this.getClass(),CouchbaseDocumentDao.class,0);
        CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClass((Class)this.getBaseClass());
        this.rootEntity = EntityDef.build(structureReflection);
    }


    public BlockingDao toBlocking(){
        return new BlockingDao();
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

    public CouchbaseDocumentDao<T> setDomain(String domain){
        this.domain = domain;
        resyncChildren();
        return this;
    }

    private final void resyncChildren(){
        this.childEntities.clear();
        if(entityDefinitionManager!=null){
            List<EntityDef> children = entityDefinitionManager.getChildEntities(rootEntity);
            if(domain!=null){
                this.childEntities.addAll(
                        children
                                .stream()
                                .filter(child->child.getModelId().getDomain().equals(domain))
                                .collect(Collectors.toSet())
                );
            }
            else {
                this.childEntities.addAll(children);
            }
        }
    }


    public String getDomain() {
        return domain;
    }

    public ICouchbaseBucket getClient(){ return client; }

    public UUID getUuid() {
        return uuid;
    }

    public T checkUpdatableState(T obj) throws InconsistentStateException {
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
        return checkDomain(obj);
    }

    public T checkDomain(T obj) throws InconsistentStateException{
        String domain =null;
        if(obj instanceof HasEffectiveDomain){
            domain = ((HasEffectiveDomain) obj).getEffectiveDomain();
        }
        else if(obj instanceof IVersionedEntity && (((IVersionedEntity) obj).getModelId()!=null)){
            domain=((IVersionedEntity) obj).getModelId().getDomain();
        }
        else{
            domain= EntityDef.build(obj.getClass()).getModelId().getDomain();
        }
        
        if(!getDomain().equals(domain)){
            throw new InconsistentStateException(obj,"Document "+obj.getClass().getName()+" with key "+obj.getBaseMeta().getKey()+" and domain <"+domain+"> don't match the dao domain <"+getDomain()+">");
        }
        return obj;
    }

    public T checkCreatableState(T obj) throws InconsistentStateException{
        if(!obj.getBaseMeta().getState().equals(CouchbaseDocument.DocumentState.NEW)){
            throw new InconsistentStateException(obj,"The document is not new and then cannot be create");
        }
        if(isReadOnly){
            throw new InconsistentStateException(obj,"Cannot update document in readonly mode");
        }
        return checkDomain(obj);
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
        return checkDomain(obj);
    }

    public class FuncUpdateStateSync implements Function<T,T> {
        private final T origObj;
        public FuncUpdateStateSync(T origObj) {
            this.origObj=origObj;
        }

        @Override
        public T apply(T t) {
            t.getBaseMeta().setStateSync();
            origObj.getBaseMeta().setStateSync();
            return t;
        }
    }

    public class FuncCheckCreatable implements Function<T,T> {
        @Override
        public T apply(T t) throws Exception {
            checkCreatableState(t);
            return t;
        }
    }


    public class FuncCheckUpdatable implements Function<T,T> {
        @Override
        public T apply(T t) throws Exception {
            checkUpdatableState(t);
            return t;
        }
    }

    public class FuncCheckDeletable implements Function<T,T> {
        @Override
        public T apply(T t) throws Exception{
            checkDeletableState(t);
            return t;
        }
    }

    public Single<T> asyncCreate(ICouchbaseSession session, T obj, boolean isCalcOnly){
        Single<T> result=Single.just(obj);
        result = result.map(new FuncCheckCreatable());
        if(obj.getBaseMeta().getKey()==null) {
            result = result.flatMap(val -> asyncBuildKey(session, val));
        }
        result = result.flatMap(session::asyncValidate);
        if(!isCalcOnly) {
            final String keyPrefix = session.getKeyPrefix();
            if(session.getKeyPrefix()!=null) {
                result = result.flatMap(val -> getClient().asyncAdd(val, WriteParams.create().with(keyPrefix)));
            }
            else {
                result = result.flatMap(val -> getClient().asyncAdd(val));
            }
        }
        return result.map(new FuncUpdateStateSync(obj))
                .map(this::managePostReading)
                .onErrorResumeNext(throwable -> mapObservableException(obj,throwable));
    }

    public <T extends CouchbaseDocument> Single<T> mapObservableException(T doc,Throwable e){
        if(e instanceof ValidationException || e instanceof DaoException){
            return Single.error(e);
        }
        return ICouchbaseBucket.Utils.mapObservableStorageException(doc,e);
    }

    public Single<T> asyncGet(ICouchbaseSession session,String key){
        try {
            Single<T> result;
            if (session.getKeyPrefix() != null) {
                result = getClient().asyncGet(key, baseClass, ReadParams.create().with(session.getKeyPrefix()));
            }
            else {
                result = getClient().asyncGet(key, baseClass);
            }
            result = result.map(this::managePostReading);
            return result;
        }
        catch(Throwable e){
            return Single.error(new DaoException("Unexpected exception",e));
        }
    }

    public Single<T> asyncUpdate(ICouchbaseSession session,T obj,boolean isCalcOnly){
        try {
            Single<T> result = Single.just(obj);
            result = result.map(new FuncCheckUpdatable());
            result = result.flatMap(session::asyncValidate);
            if (!isCalcOnly) {
                final String keyPrefix = session.getKeyPrefix();
                if (keyPrefix != null) {
                    result = result.flatMap(val -> getClient().asyncReplace(val, WriteParams.create().with(keyPrefix)));
                }
                else {
                    result = result.flatMap(val -> getClient().asyncReplace(val));
                }
            }
            return result.map(new FuncUpdateStateSync(obj))
                    .map(this::managePostReading)
                    .onErrorResumeNext(throwable -> mapObservableException(obj,throwable));
        }
        catch(Throwable e){
            return Single.error(new DaoException("Unexpected exception",e));
        }
    }

    public Single<T> asyncDelete(ICouchbaseSession session,T obj,boolean isCalcOnly){
        try {
            Single<T> result = Single.just(obj);
            result = result.map(new FuncCheckDeletable());
            result = result.map(val -> {
                val.getBaseMeta().addFlag(DocumentFlag.Deleted);
                return val;
            });
            if (!isCalcOnly) {
                result = result.flatMap(val -> getClient().asyncDelete(val));
            }
            result = result.map(this::managePostReading);
            return result.onErrorResumeNext(throwable -> mapObservableException(obj,throwable));
        }
        catch(Throwable e){
            return Single.error(new DaoException("Unexpected exception",e));
        }
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void isReadOnly(boolean isReadOnly) {
        this.isReadOnly=isReadOnly;
    }


    //To be reused by cache Read
    public final T managePostReading(T obj){
        obj =manageDaoLevelTransientFields(obj);
        if (obj.getBaseMeta().hasFlag(DocumentFlag.Deleted)) {
            obj.getBaseMeta().setStateDeleted();
        }
        else {
            obj.getBaseMeta().setStateSync();
        }
        return obj;
    }

    public T manageDaoLevelTransientFields(T obj){
        return obj;
    }

    public class BlockingDao{
        public T blockingGet(ICouchbaseSession session, String key) throws DaoException,StorageException{
            try{
                return asyncGet(session,key).blockingGet();
            }
            catch(RuntimeException e){
                Throwable eCause=e.getCause();
                if(eCause!=null){
                    if(eCause instanceof DaoException){
                        throw (DaoException)eCause;
                    }
                    if(eCause instanceof StorageException){
                        throw (StorageException)eCause;
                    }
                }
                throw e;
            }
        }

        public T blockingCreate(ICouchbaseSession session, T obj, boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
            try {
                return asyncCreate(session, obj, isCalcOnly).blockingGet();
            }
            catch(RuntimeException e){
                Throwable eCause=e.getCause();
                if(eCause!=null){
                    if(eCause instanceof DaoException){
                        throw (DaoException)eCause;
                    }
                    else if(eCause instanceof StorageException){
                        throw (StorageException)eCause;
                    }
                    else if(eCause instanceof ValidationException){
                        throw (ValidationException)eCause;
                    }
                }
                throw e;
            }
        }

        public T blockingUpdate(ICouchbaseSession session, T obj, boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
            try {
                return asyncUpdate(session, obj, isCalcOnly).blockingGet();
            }
            catch(RuntimeException e){
                Throwable eCause=e.getCause();
                if(eCause!=null){
                    if(eCause instanceof DaoException){
                        throw (DaoException)eCause;
                    }
                    else if(eCause instanceof StorageException){
                        throw (StorageException)eCause;
                    }
                    else if(eCause instanceof ValidationException){
                        throw (ValidationException)eCause;
                    }
                }
                throw e;
            }

        }

        //Should only be used through DeletionJob
        public T blockingDelete(ICouchbaseSession session, T obj, boolean isCalcOnly) throws ValidationException,DaoException,StorageException{
            try {
                return asyncDelete(session, obj, isCalcOnly).blockingGet();
            }
            catch(RuntimeException e){
                Throwable eCause=e.getCause();
                if(eCause!=null){
                    if(eCause instanceof DaoException){
                        throw (DaoException)eCause;
                    }
                    else if(eCause instanceof StorageException){
                        throw (StorageException)eCause;
                    }
                    else if(eCause instanceof ValidationException){
                        throw (ValidationException)eCause;
                    }
                }
                throw e;
            }
        }
    }
}
