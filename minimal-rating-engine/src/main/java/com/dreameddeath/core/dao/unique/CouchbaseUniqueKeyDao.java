package com.dreameddeath.core.dao.unique;

import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDao;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.DocumentNotFoundException;
import com.dreameddeath.core.exception.storage.DuplicateDocumentKeyException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.BucketDocument;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.storage.CouchbaseBucketWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import com.dreameddeath.core.storage.GenericTranscoder;
import org.apache.commons.codec.digest.DigestUtils;
/**
 * Created by ceaj8230 on 06/08/2014.
 */
public class CouchbaseUniqueKeyDao extends BaseCouchbaseDocumentDao<CouchbaseUniqueKey> {
    public static final String UNIQ_FMT_KEY="uniq/%s";
    public static final String UNIQ_KEY_PATTERN="uniq/.*";
    private static final String INTERNAL_KEY_FMT="%s/%s";
    private static final String INTERNAL_KEY_SEPARATOR="/";

    public static class LocalBucketDocument extends BucketDocument<CouchbaseUniqueKey> {
        public LocalBucketDocument(CouchbaseUniqueKey obj){super(obj);}
    }


    private static GenericJacksonTranscoder<CouchbaseUniqueKey> _tc = new GenericJacksonTranscoder<CouchbaseUniqueKey>(CouchbaseUniqueKey.class,LocalBucketDocument.class);
    public GenericTranscoder<CouchbaseUniqueKey> getTranscoder(){
        return _tc;
    }


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


    public CouchbaseUniqueKeyDao(CouchbaseBucketWrapper client, CouchbaseUniqueKeyDaoFactory factory){
        super(client,null);
        CouchbaseUniqueKeyDaoFactory _parentFactory = factory;
    }

    public String buildKey(String nameSpace, String value) throws DaoException{
        return String.format(UNIQ_FMT_KEY, getHashKey(nameSpace,value));
    }

    public String buildKey(String internalKey) throws DaoException{
        return String.format(UNIQ_FMT_KEY, getHashKey(internalKey));
    }

    public void buildKey(CouchbaseUniqueKey obj){
        throw new RuntimeException("Shouldn't append");
    }

    public CouchbaseUniqueKey get(String nameSpace,String value) throws DaoException,StorageException{
        return super.get(buildKey(nameSpace, value));
    }

    public CouchbaseUniqueKey getFromInternalKey(String internalKey) throws DaoException,StorageException{
        return super.get(buildKey(internalKey));
    }

    private CouchbaseUniqueKey create(CouchbaseDocument doc,String internalKey,boolean isCalcOnly) throws DaoException,StorageException{
        if(doc.getMeta().getKey()==null){
            throw new DaoException("The key object doesn't have a key before unique key setup. The doc was :"+doc);
        }
        CouchbaseUniqueKey keyDoc = new CouchbaseUniqueKey();
        keyDoc.getBaseMeta().setKey(buildKey(internalKey));
        keyDoc.addKey(internalKey,doc);

        if(isCalcOnly) {
            try {
                CouchbaseUniqueKey existingKeyDoc = doc.getMeta().getSession().getUniqueKey(internalKey);
                throw new DuplicateDocumentKeyException(existingKeyDoc,"The key <"+internalKey+"> is already pre-existing in calc only mode");
            } catch (DocumentNotFoundException e) {
                super.create(keyDoc,isCalcOnly);
                //Nothing to do as it means no duplicates
            }
        }
        else{
            super.create(keyDoc,isCalcOnly); //getClient().add(getTranscoder().newDocument(keyDoc));
        }
        //keyDoc.getBaseMeta().setStateSync();
        doc.addDocUniqKeys(internalKey);
        return keyDoc;
    }

    public void removeUniqueKey(CouchbaseUniqueKey doc,String internalKey,boolean isCalcOnly) throws StorageException,DaoException{
        doc.removeKey(internalKey);
        if(doc.isEmpty()){
            delete(doc,isCalcOnly);//TODO manage expiration for timed removed document
        }
        else{
           update(doc,isCalcOnly);
        }
    }



    public CouchbaseUniqueKey addOrUpdateUniqueKey(String nameSpace,String value,CouchbaseDocument doc,boolean isCalcOnly) throws StorageException,DaoException{
        if(doc.getMeta().getKey()==null){
            doc.getMeta().getSession().buildKey(doc);
        }

        String internalKey= buildInternalKey(nameSpace, value);
        try{
            CouchbaseUniqueKey result=create(doc,internalKey,isCalcOnly);
            doc.addDocUniqKeys(internalKey);
            return result;
        }
        catch(DuplicateDocumentKeyException e){
            CouchbaseUniqueKey existingKeyDoc = doc.getMeta().getSession().getUniqueKey(internalKey);
            existingKeyDoc.addKey(internalKey,doc);
            doc.addDocUniqKeys(internalKey);
            return existingKeyDoc;
        }
    }


    public String getKeyPattern(){
        return "^"+UNIQ_KEY_PATTERN+"$";
    }

}
