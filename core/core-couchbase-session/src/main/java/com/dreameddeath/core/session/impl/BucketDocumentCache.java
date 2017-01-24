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

package com.dreameddeath.core.session.impl;

import com.couchbase.client.core.message.kv.MutationToken;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;
import com.dreameddeath.core.model.mapper.IDocumentClassMappingInfo;
import com.dreameddeath.core.model.transcoder.ITranscoder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 03/10/2016.
 */
public class BucketDocumentCache {
    private final CouchbaseSession parentSession;
    private final Map<String,CacheValue> cache = new ConcurrentHashMap<>();

    public BucketDocumentCache(CouchbaseSession parentSession) {
        this.parentSession = parentSession;
    }

    public <T extends CouchbaseDocument> T get(String key) throws DaoException{
        CacheValue value = cache.get(key);
        if(value!=null) {
            return extractValue(value,(Class<T>) value.clazz);
        }
        else{
            return null;
        }
    }

    public void clear(){
        cache.clear();
    }

    private <T extends CouchbaseDocument> T extractValue(CacheValue value, Class<T> clazz) throws DaoException{
        if(value!=null) {
            try {
                final IDocumentClassMappingInfo mappingInfo = parentSession.getDocumentFactory().getDocumentInfoMapper().getMappingFromClass(parentSession.getDomain(),clazz);
                final CouchbaseDocumentDao<T> dao=parentSession.getDocumentFactory().getDaoForClass(parentSession.getDomain(),clazz);
                @SuppressWarnings("unchecked")
                final ITranscoder<T> transcoder = mappingInfo.getAttachedObject(ITranscoder.class);
                T doc=transcoder.decode(value.content);
                doc.getBaseMeta().setKey(value.id());
                doc.getBaseMeta().setCas(value.cas());
                doc.getBaseMeta().setExpiry(value.expiry());
                doc.getBaseMeta().setEncodedFlags(value.flags());
                doc.getBaseMeta().setBucketName(value.mutationToken().bucket());
                doc.getBaseMeta().setVbucketID(value.mutationToken().vbucketID());
                doc.getBaseMeta().setVbucketUUID(value.mutationToken().vbucketUUID());
                doc.getBaseMeta().setSequenceNumber(value.mutationToken().sequenceNumber());
                doc.getBaseMeta().setStateSync();
                doc.getBaseMeta().setDbData(value.content);
                return dao.managePostReading(doc);
            } catch (MappingNotFoundException e) {
                throw new DaoException("Cannot find transcoder ");
            }
        }
        return null;
    }

    public <T extends CouchbaseDocument> T get(String key, Class<T> clazz) throws DaoException{
        return extractValue(cache.get(key),clazz);

    }

    public void put(final CouchbaseDocument newDoc){
        cache.compute(newDoc.getBaseMeta().getKey(),(key,old)->{
            //Empty cache create new entry
            if(old==null){
                return new CacheValue(newDoc);
            }
            //Compare old cas, with new cas
            else if(old.cas==newDoc.getBaseMeta().getUpdatedFromCas()){
                return new CacheValue(newDoc);
            }
            //Compare cas mismatch, disable the cache
            else{
                return null;
            }
        });
    }

    private static class CacheValue {
        private final Class<? extends CouchbaseDocument> clazz;
        private final String key;
        private final long cas;
        private final byte[] content;
        private final int expiry;
        private final MutationToken mutationToken;
        private final int flags;

        CacheValue(CouchbaseDocument doc){
            clazz=doc.getClass();
            CouchbaseDocument.BaseMetaInfo metaInfo=doc.getBaseMeta();
            key=metaInfo.getKey();
            cas=metaInfo.getCas();
            content=metaInfo.getDbData();
            expiry=metaInfo.getExpiry();
            mutationToken = new MutationToken(metaInfo.getVbucketID(),metaInfo.getVbucketUUID(),metaInfo.getSequenceNumber(),metaInfo.getBucketName());
            flags=metaInfo.getEncodedFlags();
        }

        public String id() {
            return key;
        }

        public byte[] content() {
            return content;
        }

        public long cas() {
            return cas;
        }

        public int expiry() {
            return expiry;
        }

        public MutationToken mutationToken() {
            return mutationToken;
        }

        public int flags() {
            return flags;
        }

    }
}
