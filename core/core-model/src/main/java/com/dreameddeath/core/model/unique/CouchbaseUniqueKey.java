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

import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.model.property.MapProperty;
import com.dreameddeath.core.model.property.impl.HashMapProperty;

import java.util.Map;

/**
 * Created by Christophe Jeunesse on 06/08/2014.
 */
@DocumentDef(domain="core")
public class CouchbaseUniqueKey extends CouchbaseDocument {
    @DocumentProperty("maps")
    MapProperty<String,String> keyMaps = new HashMapProperty<String, String>(CouchbaseUniqueKey.this);

    public Map<String,String> getMaps(){ return keyMaps.get();}
    public void setMaps(Map<String,String> maps){keyMaps.set(maps);}

    public void addKey(String key,CouchbaseDocument doc) throws DuplicateUniqueKeyException {
        if(keyMaps.containsKey(key)) {
            if (!doc.getBaseMeta().getKey().equals(keyMaps.get(key))) {
                throw new DuplicateUniqueKeyException(key,keyMaps.get(key),doc,this);
            }
        }
        keyMaps.put(key,doc.getBaseMeta().getKey());
    }

    public String getKeyRefDocKey(String key){
        return keyMaps.get(key);
    }

    public boolean isEmpty(){
        return keyMaps.size()==0;
    }

    public boolean removeKey(String buildKey){
        return keyMaps.remove(buildKey)!=null;
    }
}
