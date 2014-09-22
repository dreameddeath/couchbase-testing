package com.dreameddeath.core.dao.unique;

import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.DocumentNotFoundException;
import com.dreameddeath.core.exception.storage.DuplicateDocumentKeyException;
import com.dreameddeath.core.exception.storage.DuplicateUniqueKeyException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;

import net.spy.memcached.transcoders.Transcoder;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import org.apache.commons.codec.digest.DigestUtils;
/**
 * Created by ceaj8230 on 06/08/2014.
 */
public class CouchbaseUniqueKeyDao {
    public static final String UNIQ_FMT_KEY="uniq/%s";
    public static final String UNIQ_KEY_PATTERN="uniq/.*";
    private static final String INTERNAL_KEY_FMT="%s/%s";
    private static final String INTERNAL_KEY_SEPARATOR="/";

    private CouchbaseClientWrapper _client;
    private CouchbaseUniqueKeyDaoFactory _parentFactory;

    private static GenericJacksonTranscoder<CouchbaseUniqueKey> _tc = new GenericJacksonTranscoder<CouchbaseUniqueKey>(CouchbaseUniqueKey.class);
    public Transcoder<CouchbaseUniqueKey> getTranscoder(){
        return _tc;
    }


    public String buildKey(String nameSpace,String value){
        return String.format(INTERNAL_KEY_FMT,nameSpace,value);
    }

    public String getHashKey(String builtKey) throws DaoException{
        return DigestUtils.sha1Hex(builtKey);
    }

    public String getHashKey(String nameSpace,String value) throws DaoException{
        return getHashKey(buildKey(nameSpace,value));
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


    public CouchbaseUniqueKeyDao(CouchbaseClientWrapper client, CouchbaseUniqueKeyDaoFactory factory){
        _client=client;
        _parentFactory = factory;
    }

    public String buildObjKey(String nameSpace,String value) throws DaoException{
        return String.format(UNIQ_FMT_KEY, getHashKey(nameSpace,value));
    }

    public String buildObjKey(String internalKey) throws DaoException{
        return String.format(UNIQ_FMT_KEY, getHashKey(internalKey));
    }

    private CouchbaseUniqueKey get(String objKey) throws DaoException,StorageException{
        CouchbaseUniqueKey result=_client.get(objKey, getTranscoder());
        result.setDocStateSync();
        return result;
    }

    public CouchbaseUniqueKey get(String nameSpace,String value) throws DaoException,StorageException{
        CouchbaseUniqueKey result=_client.get(buildObjKey(nameSpace, value), getTranscoder());
        result.setDocStateSync();
        return result;
    }

    public CouchbaseUniqueKey getFromInternalKey(String internalKey) throws DaoException,StorageException{
        CouchbaseUniqueKey result=_client.get(buildObjKey(internalKey), getTranscoder());
        result.setDocStateSync();
        return result;
    }

    private CouchbaseUniqueKey create(CouchbaseDocument doc,String internalKey,boolean isCalcOnly) throws DaoException,StorageException{
        if(doc.getDocumentKey()==null){
            throw new DaoException("The key object doesn't have a key before unique key setup. The doc was :"+doc);
        }
        CouchbaseUniqueKey keyDoc = new CouchbaseUniqueKey();
        keyDoc.setDocumentKey(buildObjKey(internalKey));
        keyDoc.addKey(internalKey,doc);

        if(isCalcOnly) {
            try {
                CouchbaseUniqueKey existingKeyDoc = doc.getSession().getUniqueKey(internalKey);
            } catch (DocumentNotFoundException e) {
                //Nothing to do as it means no duplicates
            }
        }
        else{
            _client.add(keyDoc, getTranscoder());
        }
        keyDoc.setDocStateSync();
        doc.addDocUniqKeys(internalKey);
        return keyDoc;
    }


    private CouchbaseUniqueKey update(CouchbaseUniqueKey obj,boolean isCalcOnly) throws DaoException,StorageException{
        if(obj.getDocumentKey()==null){
            throw new DaoException("The key object doesn't have a key before update. The doc was :"+obj);
        }
        if(!isCalcOnly) {
            _client.cas(obj, getTranscoder());
        }
        obj.setDocStateSync();
        return obj;
    }

    private CouchbaseUniqueKey delete(CouchbaseUniqueKey obj,boolean isCalcOnly,int expiration) throws DaoException,StorageException{
        if(obj.getDocumentKey()==null){
            throw new DaoException("The key object doesn't have a key before deletion. The doc was :"+obj);
        }
        if(!isCalcOnly) {
            if(expiration>0){
                _client.cas(obj,_tc,expiration);
            }
            else {
                _client.deleteCas(obj);
            }
        }
        obj.setDocStateDeleted();
        return obj;
    }


    public void removeUniqueKey(CouchbaseUniqueKey doc,String internalKey,boolean isCalcOnly) throws StorageException,DaoException{
        doc.removeKey(internalKey);
        if(doc.isEmpty()){
            delete(doc,isCalcOnly,0);//TODO manage expiration for timed removed document
        }
        else{
           update(doc,isCalcOnly);
        }
    }

    public void removeUniqueKey(CouchbaseUniqueKey doc,String internalKey,boolean isCalcOnly,int expiration) throws StorageException,DaoException{
        doc.removeKey(internalKey);
        if(doc.isEmpty()){
            delete(doc,isCalcOnly,expiration);
        }
        else{
            update(doc,isCalcOnly);
        }
    }


    public CouchbaseUniqueKey addOrUpdateUniqueKey(String nameSpace,String value,CouchbaseDocument doc,boolean isCalcOnly) throws StorageException,DaoException{
        if(doc.getDocumentKey()==null){
            doc.getSession().buildKey(doc);
        }

        String internalKey=buildKey(nameSpace,value);
        try{
            CouchbaseUniqueKey result=create(doc,internalKey,isCalcOnly);
            doc.addDocUniqKeys(buildKey(nameSpace,value));
            return result;
        }
        catch(DuplicateDocumentKeyException e){
            CouchbaseUniqueKey existingKeyDoc = doc.getSession().getUniqueKey(internalKey);
            existingKeyDoc.addKey(internalKey,doc);
            doc.addDocUniqKeys(internalKey);
            return existingKeyDoc;
        }
    }


    public String getKeyPattern(){
        return "^"+UNIQ_KEY_PATTERN+"$";
    }

}
