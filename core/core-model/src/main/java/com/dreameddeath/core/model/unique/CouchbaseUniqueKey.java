package com.dreameddeath.core.model.unique;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.model.common.RawCouchbaseDocument;
import com.dreameddeath.core.model.property.MapProperty;
import com.dreameddeath.core.model.property.impl.HashMapProperty;

import java.util.Map;

/**
 * Created by ceaj8230 on 06/08/2014.
 */
public class CouchbaseUniqueKey extends RawCouchbaseDocument {
    @DocumentProperty("maps")
    MapProperty<String,String> _keyMaps = new HashMapProperty<String, String>(CouchbaseUniqueKey.this);

    public Map<String,String> getMaps(){ return _keyMaps.get();}
    public void setMaps(Map<String,String> maps){_keyMaps.set(maps);}

    public void addKey(String key,RawCouchbaseDocument doc) throws DuplicateUniqueKeyException {
        if(_keyMaps.containsKey(key)) {
            if (!doc.getBaseMeta().getKey().equals(_keyMaps.get(key))) {
                throw new DuplicateUniqueKeyException(key,_keyMaps.get(key),doc,this);
            }
        }
        _keyMaps.put(key,doc.getBaseMeta().getKey());
    }

    public boolean isEmpty(){
        return _keyMaps.size()==0;
    }

    public boolean removeKey(String buildKey){
        return _keyMaps.remove(buildKey)!=null;
    }
}
