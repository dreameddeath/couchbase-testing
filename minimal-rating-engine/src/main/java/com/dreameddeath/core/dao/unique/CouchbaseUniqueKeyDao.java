package com.dreameddeath.core.dao.unique;

import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.common.BaseCouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;
import com.dreameddeath.core.storage.CouchbaseClientWrapper;
import com.dreameddeath.core.storage.GenericJacksonTranscoder;
import net.spy.memcached.transcoders.Transcoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

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
        try {
            return MessageDigest.getInstance("SHA1")
                    .digest(builtKey.getBytes())
                    .toString();
        }
        catch(NoSuchAlgorithmException e){
            throw new DaoException("Cannot hash the key <"+builtKey+">",e);
        }
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

    private CouchbaseUniqueKey get(String objKey) throws DaoException,StorageException{
        CouchbaseUniqueKey result=_client.gets(objKey,getTranscoder());
        result.setDocStateSync();
        return result;
    }

    public CouchbaseUniqueKey get(String nameSpace,String value) throws DaoException,StorageException{
        CouchbaseUniqueKey result=_client.gets(getHashKey(nameSpace,value),getTranscoder());
        result.setDocStateSync();
        return result;
    }

    public CouchbaseUniqueKey getFromInternalKey(String internalKey) throws DaoException,StorageException{
        CouchbaseUniqueKey result=_client.gets(getHashKey(internalKey),getTranscoder());
        result.setDocStateSync();
        return result;
    }

    private CouchbaseUniqueKey update(CouchbaseUniqueKey obj,boolean isCalcOnly) throws ValidationException{
        if(obj.getDocumentKey()==null){/**TODO throw an error*/}
        if(!isCalcOnly) {
            _client.cas(obj, getTranscoder());
        }
        obj.setDocStateSync();
        return obj;
    }

    private CouchbaseUniqueKey delete(CouchbaseUniqueKey obj,boolean isCalcOnly) throws ValidationException{
        if(obj.getDocumentKey()==null){/**TODO throw an error*/}
        if(!isCalcOnly) {
            _client.deleteCas(obj);
        }
        obj.setDocStateDeleted();
        return obj;
    }


    public void removeUniqueKey(CouchbaseUniqueKey doc,String internalKey,boolean isCalcOnly) throws StorageException,DaoException{
        doc.removeKey(internalKey);
        if(doc.isEmpty()){
            delete(doc,isCalcOnly);
        }
        else{
           update(doc,isCalcOnly);
        }
    }

    public CouchbaseUniqueKey addOrUpdateUniqueKey(String nameSpace,String value,CouchbaseDocument doc,boolean isCalcOnly) throws StorageException,DaoException{
        CouchbaseUniqueKey keyDoc = new CouchbaseUniqueKey();
        String internalKey =buildKey(nameSpace,value.toString());
        keyDoc.setDocumentKey(buildObjKey(nameSpace,value));
        keyDoc.addKey(internalKey,doc);


        if(isCalcOnly){
            CouchbaseUniqueKey existingKeyDoc = doc.getSession().getUniqueKey(internalKey);
            if(existingKeyDoc!=null){
                existingKeyDoc.addKey(internalKey,doc);
                return existingKeyDoc;
            }
        }
        else{
            try {
                if(_client.add(keyDoc, getTranscoder()).get()==false){
                    CouchbaseUniqueKey existingKeyDoc = get(keyDoc.getDocumentKey());
                    existingKeyDoc.addKey(internalKey,doc);
                    update(existingKeyDoc,isCalcOnly);
                    return existingKeyDoc;
                }
            }
            catch(InterruptedException e) {
                throw new StorageException("Interrupted waiting for cas update", e);
            }
            catch (ExecutionException e) {
                if(e.getCause() instanceof CancellationException) {
                    throw new StorageException("Cancelled Update",e);
                } else {
                    throw new StorageException("Exception waiting for cas update",e);
                }
            }
        }
        return keyDoc;
    }


    public String getKeyPattern(){
        return "^"+UNIQ_KEY_PATTERN+"$";
    }

}
