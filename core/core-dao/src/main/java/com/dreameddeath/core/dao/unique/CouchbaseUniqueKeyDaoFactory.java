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

import com.dreameddeath.core.couchbase.ICouchbaseTranscoder;
import com.dreameddeath.core.couchbase.impl.GenericCouchbaseTranscoder;
import com.dreameddeath.core.dao.exception.DaoNotFoundException;
import com.dreameddeath.core.dao.model.IHasUniqueKeysRef;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.mapper.DuplicateMappedEntryInfoException;
import com.dreameddeath.core.model.mapper.IDocumentInfoMapper;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 11/09/2014.
 */
public class CouchbaseUniqueKeyDaoFactory{
    private IDocumentInfoMapper documentInfoMapper;
    private Map<String, CouchbaseUniqueKeyDao> daosMap  = new ConcurrentHashMap<String, CouchbaseUniqueKeyDao>();

    private ICouchbaseTranscoder<CouchbaseUniqueKey> defaultTranscoder;


    public CouchbaseUniqueKeyDaoFactory(Builder builder){
        documentInfoMapper = builder.documentInfoMapper;
        try {
            documentInfoMapper.addDocument(CouchbaseUniqueKey.class, CouchbaseUniqueKeyDao.UNIQ_KEY_PATTERN);
        }
        catch(DuplicateMappedEntryInfoException e){
            //ignore error
        }
        defaultTranscoder = builder.defaultTranscoder;
    }


    public void addDaoFor(String nameSpace,CouchbaseUniqueKeyDao dao){
        if(dao.getTranscoder()==null){
            dao.setTranscoder(defaultTranscoder);
        }
        daosMap.put(nameSpace,dao);
    }

    public CouchbaseUniqueKeyDao getDaoFor(String nameSpace) throws DaoNotFoundException{
        CouchbaseUniqueKeyDao result =daosMap.get(nameSpace);
        if(result==null){
            throw new DaoNotFoundException(nameSpace, DaoNotFoundException.Type.KEY);
        }
        return daosMap.get(nameSpace);
    }

    public CouchbaseUniqueKeyDao getDaoForInternalKey(String key) throws DaoNotFoundException{
        for(Map.Entry<String,CouchbaseUniqueKeyDao> entry:daosMap.entrySet()){
            String nameSpace = entry.getValue().extractNameSpace(key);
            if(entry.getKey().equals(nameSpace)){
                return entry.getValue();
            }
        }
        throw new DaoNotFoundException(key, DaoNotFoundException.Type.KEY);
    }

    public Map<CouchbaseUniqueKeyDao,List<String>> mapRemovedUniqueKeys(CouchbaseDocument doc) throws DaoNotFoundException{
        Map<CouchbaseUniqueKeyDao, List<String>> mapKeys = new HashMap<CouchbaseUniqueKeyDao, List<String>>();

        if(doc instanceof IHasUniqueKeysRef) {
            Set<String> removedKeys = ((IHasUniqueKeysRef)doc).getRemovedUniqueKeys();
            for (String key : removedKeys) {
                CouchbaseUniqueKeyDao dao = getDaoForInternalKey(key);
                if (!mapKeys.containsKey(dao)) {
                    mapKeys.put(dao, new ArrayList<String>());
                }
                mapKeys.get(dao).add(key);
            }

        }
        return mapKeys;
    }


    public static Builder builder(){
        return new Builder();
    }
    public static class Builder{
        private IDocumentInfoMapper documentInfoMapper;
        private ICouchbaseTranscoder<CouchbaseUniqueKey> defaultTranscoder;

        public Builder withDocumentInfoMapper(IDocumentInfoMapper mapper){
            documentInfoMapper = mapper;
            return this;
        }

        public Builder withDefaultTranscoder(ICouchbaseTranscoder<CouchbaseUniqueKey> trans){
            defaultTranscoder=trans;
            return this;
        }

        public Builder withDefaultTranscoder(ITranscoder<CouchbaseUniqueKey> trans){
            GenericCouchbaseTranscoder<CouchbaseUniqueKey> transcoder = new GenericCouchbaseTranscoder<>(CouchbaseUniqueKey.class,CouchbaseUniqueKeyDao.LocalBucketDocument.class);
            transcoder.setTranscoder(trans);
            defaultTranscoder = transcoder;
            return this;
        }

        public CouchbaseUniqueKeyDaoFactory build(){
            return new CouchbaseUniqueKeyDaoFactory(this);
        }

    }

}
