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

import com.dreameddeath.core.dao.exception.dao.DaoNotFoundException;
import com.dreameddeath.core.model.business.CouchbaseDocument;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 11/09/2014.
 */
public class CouchbaseUniqueKeyDaoFactory{
    private Map<String, CouchbaseUniqueKeyDao> _daosMap  = new ConcurrentHashMap<String, CouchbaseUniqueKeyDao>();

    public void addDaoFor(String nameSpace,CouchbaseUniqueKeyDao dao){
        _daosMap.put(nameSpace,dao);
    }

    public CouchbaseUniqueKeyDao getDaoFor(String nameSpace) throws DaoNotFoundException{
        CouchbaseUniqueKeyDao result =_daosMap.get(nameSpace);
        if(result==null){
            throw new DaoNotFoundException(nameSpace, DaoNotFoundException.Type.KEY);
        }
        return _daosMap.get(nameSpace);
    }

    public CouchbaseUniqueKeyDao getDaoForInternalKey(String key) throws DaoNotFoundException{
        for(Map.Entry<String,CouchbaseUniqueKeyDao> entry:_daosMap.entrySet()){
            String nameSpace = entry.getValue().extractNameSpace(key);
            if(entry.getKey().equals(nameSpace)){
                return entry.getValue();
            }
        }
        throw new DaoNotFoundException(key, DaoNotFoundException.Type.KEY);
    }

    public Map<CouchbaseUniqueKeyDao,List<String>> mapRemovedUniqueKeys(CouchbaseDocument doc) throws DaoNotFoundException{
        Set<String> removedKeys = doc.getRemovedUniqueKeys();
        Map<CouchbaseUniqueKeyDao,List<String>> mapKeys=new HashMap<CouchbaseUniqueKeyDao, List<String>>();
        for(String key:removedKeys){
            CouchbaseUniqueKeyDao dao = getDaoForInternalKey(key);
            if(!mapKeys.containsKey(dao)){
                mapKeys.put(dao,new ArrayList<String>());
            }
            mapKeys.get(dao).add(key);
        }
        return mapKeys;
    }

}
