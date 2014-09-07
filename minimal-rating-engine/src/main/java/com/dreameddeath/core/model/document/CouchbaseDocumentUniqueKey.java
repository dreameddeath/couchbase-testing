package com.dreameddeath.core.model.document;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.DuplicateKeyException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.property.impl.HashMapProperty;
import com.dreameddeath.core.model.property.MapProperty;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by ceaj8230 on 06/08/2014.
 */
public class CouchbaseDocumentUniqueKey extends CouchbaseDocument {
    private static final String KEY_FMT="%s/%s";
    @DocumentProperty("maps")
    MapProperty<String,String> _keyMaps = new HashMapProperty<String, String>(CouchbaseDocumentUniqueKey.this);


    public String getHashKey() throws NoSuchAlgorithmException{
        return MessageDigest.getInstance("SHA1")
                    .digest(_keyMaps.keySet().iterator().next().getBytes())
                    .toString();
    }

    public Map<String,String> getKeyMaps(){return _keyMaps.get();}

    public boolean addKey(String nameSpace,String value,CouchbaseDocument doc) throws StorageException,DaoException{
        if(doc.getKey()==null){
            doc.getSession().buildKey(doc);
        }

        String key = String.format(KEY_FMT,nameSpace,value);

        //If the key already exists
        if(_keyMaps.containsKey(key)){
            //Compare the attached document key
            if(!_keyMaps.get(key).equals(doc.getKey())){
                if(doc.getSession().get(_keyMaps.get(key))!=null){
                    throw new DuplicateKeyException(doc,"The key <"+value+"> is already used by the document <"+_keyMaps.get(value)+">");
                }
            }
            else {
                return false;
            }
        }
        return (_keyMaps.put(key,doc.getKey()))==null;
    }
}
