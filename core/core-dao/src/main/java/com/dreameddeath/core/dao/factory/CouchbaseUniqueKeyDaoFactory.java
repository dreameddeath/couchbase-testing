/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.dao.factory;

import com.dreameddeath.core.couchbase.exception.TranscoderNotFoundException;
import com.dreameddeath.core.couchbase.utils.CouchbaseUtils;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.exception.DaoNotFoundException;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.model.exception.mapper.DuplicateMappedEntryInfoException;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;
import com.dreameddeath.core.model.mapper.IDocumentInfoMapper;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 11/09/2014.
 */
public class CouchbaseUniqueKeyDaoFactory implements IDaoFactory{
    private IDocumentInfoMapper documentInfoMapper;
    private Map<NameSpaceEntry, CouchbaseUniqueKeyDao> daosMap  = new ConcurrentHashMap<>();

    public CouchbaseUniqueKeyDaoFactory(Builder builder){
        documentInfoMapper = builder.documentInfoMapper;
    }

    public void addDaoFor(String nameSpace,CouchbaseUniqueKeyDao dao){
        daosMap.put(new NameSpaceEntry(dao.getDomain(),nameSpace),dao);
    }

    public CouchbaseUniqueKeyDao getDaoFor(String domain,String nameSpace) throws DaoNotFoundException{
        NameSpaceEntry nameSpaceEntry = new NameSpaceEntry(domain,nameSpace);
        CouchbaseUniqueKeyDao result = daosMap.get(nameSpaceEntry);
        if(result==null){
            throw new DaoNotFoundException(domain,nameSpace, DaoNotFoundException.Type.UNIQ_KEY);
        }
        return daosMap.get(nameSpaceEntry);
    }

    public CouchbaseUniqueKeyDao getDaoFor(final String nameSpace, final CouchbaseDocumentDao docDao){
        //CouchbaseUniqueKeyDao dao = new CouchbaseUniqueKeyDao.Builder().withBaseDao(docDao).build();
        return daosMap.computeIfAbsent(new NameSpaceEntry(docDao.getDomain(),nameSpace),ns->
                CouchbaseUniqueKeyDao.builder().withBaseDao(docDao).withNameSpace(ns.namespace).withClient(docDao.getClient()).build()
        );
    }

    public CouchbaseUniqueKeyDao getDaoForInternalKey(String domain,String key) throws DaoNotFoundException{
        for(Map.Entry<NameSpaceEntry,CouchbaseUniqueKeyDao> entry:daosMap.entrySet()){
            String nameSpace = entry.getValue().extractNameSpace(key);
            if(entry.getKey().namespace.equals(nameSpace) && entry.getKey().domain.equals(domain)){
                return entry.getValue();
            }
        }
        throw new DaoNotFoundException(domain,key, DaoNotFoundException.Type.UNIQ_KEY);
    }

    @Override
    public synchronized void cleanup() {
        daosMap.clear();
    }

    @Override
    public synchronized void init() {
        try {
            documentInfoMapper.addDocument(CouchbaseUniqueKey.class, CouchbaseUniqueKeyDao.UNIQ_KEY_PATTERN);
            documentInfoMapper.getMappingFromClass(CouchbaseUniqueKey.class).attachObject(ITranscoder.class, CouchbaseUtils.resolveTranscoderForClass(CouchbaseUniqueKey.class));
        }
        catch(DuplicateMappedEntryInfoException|MappingNotFoundException e){
            //ignore error
        }
        catch(TranscoderNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    /*public Map<CouchbaseUniqueKeyDao,List<String>> mapRemovedUniqueKeys(CouchbaseDocument doc) throws DaoNotFoundException{
        Map<CouchbaseUniqueKeyDao, List<String>> mapKeys = new HashMap<>();

        if(doc.getBaseMeta() instanceof IHasUniqueKeysRef) {
            Set<String> removedKeys = ((IHasUniqueKeysRef)doc.getBaseMeta()).getRemovedUniqueKeys();
            for (String key : removedKeys) {
                CouchbaseUniqueKeyDao dao = getDaoForInternalKey(key);
                if (!mapKeys.containsKey(dao)) {
                    mapKeys.put(dao, new ArrayList<>());
                }
                mapKeys.get(dao).add(key);
            }

        }
        return mapKeys;
    }*/


    public static Builder builder(){
        return new Builder();
    }
    public static class Builder{
        private IDocumentInfoMapper documentInfoMapper;

        public Builder withDocumentInfoMapper(IDocumentInfoMapper mapper){
            documentInfoMapper = mapper;
            return this;
        }

        public CouchbaseUniqueKeyDaoFactory build(){
            return new CouchbaseUniqueKeyDaoFactory(this);
        }
    }

    private static class NameSpaceEntry{
        private final String domain;
        private final String namespace;

        public NameSpaceEntry(String domain, String namespace) {
            this.domain = domain;
            this.namespace = namespace;
        }
    }
}
