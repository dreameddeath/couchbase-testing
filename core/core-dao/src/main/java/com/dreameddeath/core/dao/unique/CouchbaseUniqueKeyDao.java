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

package com.dreameddeath.core.dao.unique;

import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.annotation.BucketDocumentForClass;
import com.dreameddeath.core.couchbase.exception.*;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.DaoObservableException;
import com.dreameddeath.core.dao.exception.DuplicateUniqueKeyDaoException;
import com.dreameddeath.core.dao.exception.InconsistentStateException;
import com.dreameddeath.core.dao.exception.validation.ValidationException;
import com.dreameddeath.core.dao.exception.validation.ValidationObservableException;
import com.dreameddeath.core.dao.model.IHasUniqueKeysRef;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import org.apache.commons.codec.digest.DigestUtils;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Christophe Jeunesse on 06/08/2014.
 */
@DaoForClass(CouchbaseUniqueKey.class)
public class CouchbaseUniqueKeyDao extends CouchbaseDocumentDao<CouchbaseUniqueKey> {
    public static final String UNIQ_FMT_KEY="uniq/%s";
    public static final String UNIQ_KEY_PATTERN="uniq/.*";
    private static final String INTERNAL_KEY_FMT="%s/%s";
    private static final String INTERNAL_KEY_SEPARATOR="/";

    private CouchbaseDocumentDao refDocumentDao;
    private String namespace;

    public String getNameSpace(){return namespace;}
    public void setNameSpace(String nameSpace){namespace=nameSpace;}


    public CouchbaseUniqueKeyDao(Builder builder){
        super();
        setClient(builder.getClient());
        setBaseDocumentDao(builder.getBaseDao());
        setNameSpace(builder.getNameSpace());
    }

    public void setBaseDocumentDao(CouchbaseDocumentDao dao){refDocumentDao = dao;}
    public CouchbaseDocumentDao getBaseDocumentDao(){return refDocumentDao;}


    @Override
    public ICouchbaseBucket getClient(){
        ICouchbaseBucket client = super.getClient();
        if(client!=null) return client;
        else return refDocumentDao.getClient();
    }


    @BucketDocumentForClass(CouchbaseUniqueKey.class)
    public static class LocalBucketDocument extends BucketDocument<CouchbaseUniqueKey> {
        public LocalBucketDocument(CouchbaseUniqueKey obj){super(obj);}
    }

    @Override
    public Class<? extends BucketDocument<CouchbaseUniqueKey>> getBucketDocumentClass(){return LocalBucketDocument.class;}


    public String buildInternalKey(String nameSpace, String value){
        return String.format(INTERNAL_KEY_FMT,nameSpace,value);
    }
    public String getHashKey(String builtKey){
        return DigestUtils.sha1Hex(builtKey);
    }
    public String getHashKey(String nameSpace,String value){
        return getHashKey(buildInternalKey(nameSpace, value));
    }
    public String extractNameSpace(String builtKey){
        return builtKey.split("/")[0];
    }

    public String extractValue(String buildKey){
        String[] splitResult=buildKey.split(INTERNAL_KEY_SEPARATOR);
        StringBuilder builder=new StringBuilder();
        for(int pos=1;pos<splitResult.length;++pos){
            if(pos>1){
                builder.append(INTERNAL_KEY_SEPARATOR);
            }
            builder.append(splitResult[pos]);
        }
        return builder.toString();
    }

    public String buildKey(String nameSpace, String value){
        return String.format(UNIQ_FMT_KEY, getHashKey(nameSpace,value));
    }

    public String buildKey(String internalKey){
        return String.format(UNIQ_FMT_KEY, getHashKey(internalKey));
    }

    @Override
    public Observable<CouchbaseUniqueKey> asyncBuildKey(ICouchbaseSession session, CouchbaseUniqueKey newObject) throws DaoException {
        throw new RuntimeException("Shouldn't append");
    }

    public CouchbaseUniqueKey get(ICouchbaseSession session,String nameSpace,String value) throws DaoException,StorageException{
        return super.toBlocking().blockingGet(session,buildKey(nameSpace, value));
    }

    public Observable<CouchbaseUniqueKey> asyncGet(ICouchbaseSession session,String nameSpace,String value){
        return super.asyncGet(session,buildKey(nameSpace, value));
    }


    public CouchbaseUniqueKey getFromInternalKey(ICouchbaseSession session,String internalKey) throws DaoException,StorageException{
        return super.toBlocking().blockingGet(session,buildKey(internalKey));
    }

    public Observable<CouchbaseUniqueKey> asyncGetFromInternalKey(ICouchbaseSession session, String internalKey){
        return super.asyncGet(session,buildKey(internalKey));
    }

    private CouchbaseUniqueKey create(ICouchbaseSession session,CouchbaseDocument doc,String internalKey,boolean isCalcOnly) throws DaoException,StorageException,ValidationException {
        try {
            return asyncCreate(session, doc, internalKey, isCalcOnly).toBlocking().single();
        }
        catch (StorageObservableException e){
            throw e.getCause();
        }
        catch (DaoObservableException e){
            throw e.getCause();
        }
        catch (ValidationObservableException e){
            throw e.getCause();
        }
    }

    private <T extends CouchbaseDocument> Observable<CouchbaseUniqueKey> asyncCreate(ICouchbaseSession session,final T doc,String internalKey,boolean isCalcOnly){
        if(doc.getBaseMeta().getKey()==null){
            throw new DaoObservableException(new DaoException("The key object doesn't have a key before unique key setup. The doc was :"+doc));
        }
        if(refDocumentDao.isReadOnly()){
            throw new DaoObservableException(new InconsistentStateException(doc,"Cannot update unique key <"+internalKey+"> in readonly mode"));
        }
        CouchbaseUniqueKey keyDoc = new CouchbaseUniqueKey();
        keyDoc.getBaseMeta().setKey(buildKey(internalKey));
        try {
            keyDoc.addKey(internalKey, doc);
        }
        catch(DuplicateUniqueKeyException e){
            throw new DaoObservableException(new DuplicateUniqueKeyDaoException(doc,e.getMessage(),e));
        }
        Observable<CouchbaseUniqueKey> result ;
        if(!isCalcOnly) {
            result = super.asyncCreate(session, keyDoc, isCalcOnly);
        }
        else{
            result =
                session.asyncGetUniqueKey(internalKey)
                    .map(existingKeyDoc -> {
                        DuplicateUniqueKeyException duplicateException =new DuplicateUniqueKeyException(internalKey,existingKeyDoc.getKeyRefDocKey(internalKey),doc,existingKeyDoc);
                        throw new StorageObservableException(new DuplicateDocumentKeyException(existingKeyDoc,"The key <"+internalKey+"> is already pre-existing in calc only mode",duplicateException));
                    });
            result = result
                    .onErrorResumeNext((Func1<Throwable, Observable<CouchbaseUniqueKey>>) throwable -> {
                        if (throwable instanceof StorageObservableException && (throwable.getCause() instanceof DocumentNotFoundException)) {
                            return (Observable<CouchbaseUniqueKey>)CouchbaseUniqueKeyDao.super.asyncCreate(session, keyDoc, isCalcOnly);
                        } else if (throwable instanceof StorageObservableException) {
                            throw (StorageObservableException) throwable;
                        }
                        else{
                            throw new StorageObservableException(new StorageUnkownException(throwable));
                        }
                    });

        }

        return result.doOnEach(notif->{
                if(doc instanceof IHasUniqueKeysRef){((IHasUniqueKeysRef)doc).addDocUniqKeys(internalKey);}
        });
    }

    public void removeUniqueKey(ICouchbaseSession session,CouchbaseUniqueKey doc,String internalKey,boolean isCalcOnly) throws DaoException,ValidationException,StorageException {
        doc.removeKey(internalKey);
        if(doc.isEmpty()){
            toBlocking().blockingDelete(session,doc,isCalcOnly);//TODO manage expiration for timed removed document
        }
        else{
           toBlocking().blockingUpdate(session,doc,isCalcOnly);
        }
    }


    public Observable<CouchbaseUniqueKey> asyncRemoveUniqueKey(ICouchbaseSession session,CouchbaseUniqueKey doc,String internalKey,boolean isCalcOnly) {
        doc.removeKey(internalKey);
        if(doc.isEmpty()){
            return asyncDelete(session,doc,isCalcOnly);//TODO manage expiration for timed removed document
        }
        else{
            return asyncUpdate(session,doc,isCalcOnly);
        }
    }



    public CouchbaseUniqueKey addOrUpdateUniqueKey(ICouchbaseSession session,String nameSpace,String value,CouchbaseDocument doc,boolean isCalcOnly) throws StorageException,DaoException,DuplicateUniqueKeyException,ValidationException {
        try {
            return asyncAddOrUpdateUniqueKey(session, nameSpace, value, doc, isCalcOnly).toBlocking().single();
        }
        catch (StorageObservableException e){
            throw e.getCause();
        }
        catch (DaoObservableException e){
            throw e.getCause();
        }
        catch (ValidationObservableException e){
            throw e.getCause();
        }
    }

    public <T extends CouchbaseDocument> Observable<CouchbaseUniqueKey> asyncAddOrUpdateUniqueKey(ICouchbaseSession session,String nameSpace,String value,T doc,boolean isCalcOnly){
        String internalKey= buildInternalKey(nameSpace, value);
        if(refDocumentDao.isReadOnly()){
            throw new DaoObservableException(new InconsistentStateException(doc,"Cannot update unique key <"+internalKey+"> in readonly mode"));
        }
        Observable<T> docObs;
        if(doc.getBaseMeta().getKey()==null){
            docObs=session.asyncBuildKey(doc);
        }
        else{
            docObs=Observable.just(doc);
        }

        Observable<CouchbaseUniqueKey> result;
        result=docObs.flatMap(effectiveDoc->asyncCreate(session,effectiveDoc,internalKey,isCalcOnly));
        result =result.onErrorResumeNext(throwable->{
                if(throwable instanceof StorageObservableException && throwable.getCause() instanceof DuplicateDocumentKeyException){
                    return session.asyncGetUniqueKey(internalKey).map(
                            existingDoc-> {
                                try {
                                    existingDoc.addKey(internalKey, doc);
                                    return existingDoc;
                                }
                                catch(DuplicateUniqueKeyException e){
                                    throw new DaoObservableException(new DuplicateUniqueKeyDaoException(doc,e.getMessage(),e));
                                }
                            });
                }
                else if(throwable instanceof StorageObservableException) {
                    throw (StorageObservableException) throwable;
                }
                else {
                    throw new StorageObservableException(new StorageUnkownException(throwable));
                }
        });
        return result.doOnEach(notif->{
            if(doc instanceof IHasUniqueKeysRef){((IHasUniqueKeysRef)doc).addDocUniqKeys(internalKey);}
        });
    }


    public String getKeyPattern(){
        return "^"+UNIQ_KEY_PATTERN+"$";
    }

    public static class Builder{
        private String namespace;
        private ICouchbaseBucket client;
        private CouchbaseDocumentDao baseDao;

        public Builder withNameSpace(String key){
            namespace = key;
            return this;
        }


        public Builder withClient(ICouchbaseBucket client){
            this.client = client;
            return this;
        }

        public Builder withBaseDao(CouchbaseDocumentDao dao){
            baseDao = dao;
            return this;
        }

        public String getNameSpace(){return namespace;}
        public ICouchbaseBucket getClient(){return client;}
        public CouchbaseDocumentDao getBaseDao(){return baseDao;}

        public CouchbaseUniqueKeyDao build(){
            return new CouchbaseUniqueKeyDao(this);
        }
    }
}
