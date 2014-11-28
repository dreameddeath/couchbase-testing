package com.dreameddeath.core.dao.unique;

import com.dreameddeath.core.annotation.dao.DaoForClass;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.DocumentNotFoundException;
import com.dreameddeath.core.exception.storage.DuplicateDocumentKeyException;
import com.dreameddeath.core.exception.storage.DuplicateUniqueKeyStorageException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.exception.validation.ValidationException;
import com.dreameddeath.core.model.IHasUniqueKeysRef;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.storage.BucketDocument;
import com.dreameddeath.core.storage.ICouchbaseBucket;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by ceaj8230 on 06/08/2014.
 */
@DaoForClass(CouchbaseUniqueKey.class)
public class CouchbaseUniqueKeyDao extends CouchbaseDocumentDao<CouchbaseUniqueKey> {
    public static final String UNIQ_FMT_KEY="uniq/%s";
    public static final String UNIQ_KEY_PATTERN="uniq/.*";
    private static final String INTERNAL_KEY_FMT="%s/%s";
    private static final String INTERNAL_KEY_SEPARATOR="/";

    private CouchbaseDocumentDao _refDocumentDao;
    private String _namespace;

    public String getNameSpace(){return _namespace;}
    public void setNameSpace(String nameSpace){_namespace=nameSpace;}


    public CouchbaseUniqueKeyDao(Builder builder){
        super();
        setClient(builder.getClient());
        setBaseDocumentDao(builder.getBaseDao());
        setNameSpace(builder.getNameSpace());
    }

    public void setBaseDocumentDao(CouchbaseDocumentDao dao){_refDocumentDao = dao;}
    public CouchbaseDocumentDao getBaseDocumentDao(){return _refDocumentDao;}

    @Override
    public ICouchbaseBucket getClient(){
        ICouchbaseBucket client = super.getClient();
        if(client!=null) return client;
        else return _refDocumentDao.getClient();
    }

    public static class LocalBucketDocument extends BucketDocument<CouchbaseUniqueKey> {
        public LocalBucketDocument(CouchbaseUniqueKey obj){super(obj);}
    }

    public Class<? extends BucketDocument<CouchbaseUniqueKey>> getBucketDocumentClass(){return LocalBucketDocument.class;}


    public String buildInternalKey(String nameSpace, String value){
        return String.format(INTERNAL_KEY_FMT,nameSpace,value);
    }
    public String getHashKey(String builtKey) throws DaoException{
        return DigestUtils.sha1Hex(builtKey);
    }
    public String getHashKey(String nameSpace,String value) throws DaoException{
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

    public String buildKey(String nameSpace, String value) throws DaoException{
        return String.format(UNIQ_FMT_KEY, getHashKey(nameSpace,value));
    }

    public String buildKey( String internalKey) throws DaoException{
        return String.format(UNIQ_FMT_KEY, getHashKey(internalKey));
    }

    @Override
    public CouchbaseUniqueKey buildKey(ICouchbaseSession session,CouchbaseUniqueKey obj){
        throw new RuntimeException("Shouldn't append");
    }

    public CouchbaseUniqueKey get(String nameSpace,String value) throws DaoException,StorageException{
        return super.get(buildKey(nameSpace, value));
    }

    public CouchbaseUniqueKey getFromInternalKey(String internalKey) throws DaoException,StorageException{
        return super.get(buildKey(internalKey));
    }

    private CouchbaseUniqueKey create(ICouchbaseSession session,CouchbaseDocument doc,String internalKey,boolean isCalcOnly) throws DaoException,StorageException,ValidationException {
        if(doc.getBaseMeta().getKey()==null){
            throw new DaoException("The key object doesn't have a key before unique key setup. The doc was :"+doc);
        }
        CouchbaseUniqueKey keyDoc = new CouchbaseUniqueKey();
        keyDoc.getBaseMeta().setKey(buildKey(internalKey));
        try {
            keyDoc.addKey(internalKey, doc);
        }
        catch(DuplicateUniqueKeyException e){
            throw new DuplicateUniqueKeyStorageException(doc,e.getMessage());
        }

        if(isCalcOnly) {
            try {
                CouchbaseUniqueKey existingKeyDoc = session.getUniqueKey(internalKey);
                throw new DuplicateDocumentKeyException(existingKeyDoc,"The key <"+internalKey+"> is already pre-existing in calc only mode");
            } catch (DocumentNotFoundException e) {
                super.create(session,keyDoc,isCalcOnly);
                //Nothing to do as it means no duplicates
            }
        }
        else{
            super.create(session,keyDoc,isCalcOnly); //getClient().add(getTranscoder().newDocument(keyDoc));
        }
        //keyDoc.getBaseMeta().setStateSync();
        if(doc instanceof IHasUniqueKeysRef){((IHasUniqueKeysRef)doc).addDocUniqKeys(internalKey);}
        return keyDoc;
    }

    public void removeUniqueKey(ICouchbaseSession session,CouchbaseUniqueKey doc,String internalKey,boolean isCalcOnly) throws StorageException,DaoException,ValidationException {
        doc.removeKey(internalKey);
        if(doc.isEmpty()){
            delete(session,doc,isCalcOnly);//TODO manage expiration for timed removed document
        }
        else{
           update(session,doc,isCalcOnly);
        }
    }



    public CouchbaseUniqueKey addOrUpdateUniqueKey(ICouchbaseSession session,String nameSpace,String value,CouchbaseDocument doc,boolean isCalcOnly) throws StorageException,DaoException,DuplicateUniqueKeyException,ValidationException {
        if(doc.getBaseMeta().getKey()==null){
            session.buildKey(doc);
        }

        String internalKey= buildInternalKey(nameSpace, value);
        try{
            CouchbaseUniqueKey result=create(session,doc,internalKey,isCalcOnly);
            if(doc instanceof IHasUniqueKeysRef){((IHasUniqueKeysRef)doc).addDocUniqKeys(internalKey);}
            return result;
        }
        catch(DuplicateDocumentKeyException e){
            CouchbaseUniqueKey existingKeyDoc = session.getUniqueKey(internalKey);
            existingKeyDoc.addKey(internalKey, doc);
            if(doc instanceof IHasUniqueKeysRef){((IHasUniqueKeysRef)doc).addDocUniqKeys(internalKey);}
            return existingKeyDoc;
        }
    }


    public String getKeyPattern(){
        return "^"+UNIQ_KEY_PATTERN+"$";
    }

    public static class Builder{
        private String _namespace;
        private ICouchbaseBucket _client;
        private CouchbaseDocumentDao _baseDao;

        public Builder withNameSpace(String key){
            _namespace = key;
            return this;
        }


        public Builder withClient(ICouchbaseBucket client){
            _client = client;
            return this;
        }

        public Builder withBaseDao(CouchbaseDocumentDao dao){
            _baseDao = dao;
            return this;
        }

        public String getNameSpace(){return _namespace;}
        public ICouchbaseBucket getClient(){return _client;}
        public CouchbaseDocumentDao getBaseDao(){return _baseDao;}

        public CouchbaseUniqueKeyDao build(){
            return new CouchbaseUniqueKeyDao(this);
        }
    }
}
