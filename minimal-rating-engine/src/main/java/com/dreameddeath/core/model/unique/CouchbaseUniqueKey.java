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

package com.dreameddeath.core.model.unique;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.couchbase.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.exception.dao.DaoException;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.document.BaseCouchbaseDocument;
import com.dreameddeath.core.model.property.MapProperty;
import com.dreameddeath.core.model.property.impl.HashMapProperty;

import java.util.Map;

/**
 * Created by Christophe Jeunesse on 06/08/2014.
 */
public class CouchbaseUniqueKey extends BaseCouchbaseDocument {
    @DocumentProperty("maps")
    MapProperty<String,String> keyMaps = new HashMapProperty<String, String>(CouchbaseUniqueKey.this);

    public Map<String,String> getMaps(){ return keyMaps.get();}
    public void setMaps(Map<String,String> maps){keyMaps.set(maps);}

    public void checkKey(String buildKey,CouchbaseDocument doc) throws StorageException,DaoException{
        //If the key already exists
        if(keyMaps.containsKey(buildKey)){
            //Compare the attached document key
            if(!keyMaps.get(buildKey).equals(doc.getBaseMeta().getKey())){
                //If target document still exists
                if(doc.getBaseMeta().getSession().get(keyMaps.get(buildKey))!=null){
                    throw new DuplicateUniqueKeyException(doc,"The key <"+buildKey+"> is already used by the document <"+keyMaps.get(buildKey)+">");
                }
            }
        }
    }

    public void addKey(String key,CouchbaseDocument doc) throws StorageException,DaoException{
        if(doc.getBaseMeta().getKey()==null){ doc.getBaseMeta().getSession().buildKey(doc); }
        checkKey(key,doc);
        keyMaps.put(key,doc.getBaseMeta().getKey());
    }

    public boolean isEmpty(){
        return keyMaps.size()==0;
    }

    public boolean removeKey(String buildKey){
        return keyMaps.remove(buildKey)!=null;
    }
}
