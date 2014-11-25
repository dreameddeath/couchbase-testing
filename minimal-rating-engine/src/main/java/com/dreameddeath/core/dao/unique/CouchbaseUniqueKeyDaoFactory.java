package com.dreameddeath.core.dao.unique;

import com.dreameddeath.core.exception.dao.DaoNotFoundException;
import com.dreameddeath.core.model.business.CouchbaseDocument;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ceaj8230 on 11/09/2014.
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
