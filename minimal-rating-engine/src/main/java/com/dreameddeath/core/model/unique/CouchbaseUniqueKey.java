package com.dreameddeath.core.model.unique;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.DuplicateKeyException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.impl.HashMapProperty;
import com.dreameddeath.core.model.property.MapProperty;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by ceaj8230 on 06/08/2014.
 */
public class CouchbaseUniqueKey extends CouchbaseDocument {
    @DocumentProperty("maps")
    MapProperty<String,String> _keyMaps = new HashMapProperty<String, String>(CouchbaseUniqueKey.this);

    public void checkKey(String buildKey,CouchbaseDocument doc) throws DuplicateKeyException,StorageException,DaoException{


        //If the key already exists
        if(_keyMaps.containsKey(buildKey)){
            //Compare the attached document key
            if(!_keyMaps.get(buildKey).equals(doc.getDocumentKey())){
                //If target document still exists
                if(doc.getSession().get(_keyMaps.get(buildKey))!=null){
                    throw new DuplicateKeyException(doc,"The key <"+buildKey+"> is already used by the document <"+_keyMaps.get(buildKey)+">");
                }
            }
        }
    }

    public void addKey(String key,CouchbaseDocument doc) throws StorageException,DaoException{
        if(doc.getDocumentKey()==null){ doc.getSession().buildKey(doc); }
        checkKey(key,doc);
        _keyMaps.put(key,doc.getDocumentKey());
    }

    public boolean isEmpty(){
        return _keyMaps.size()==0;
    }

    public boolean removeKey(String buildKey){
        return _keyMaps.remove(buildKey)!=null;
    }
}
