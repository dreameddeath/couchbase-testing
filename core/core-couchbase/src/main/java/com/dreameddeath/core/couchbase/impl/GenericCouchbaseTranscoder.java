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

package com.dreameddeath.core.couchbase.impl;

import com.couchbase.client.core.lang.Tuple;
import com.couchbase.client.core.lang.Tuple2;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.core.message.kv.MutationToken;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.buffer.Unpooled;
import com.dreameddeath.core.couchbase.BucketDocument;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.ICouchbaseTranscoder;
import com.dreameddeath.core.couchbase.exception.DocumentSetUpException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Christophe Jeunesse on 12/10/2014.
 */
public class GenericCouchbaseTranscoder<T extends CouchbaseDocument> implements ICouchbaseTranscoder<T> {
    private final static Logger logger = LoggerFactory.getLogger(GenericCouchbaseTranscoder.class);
    private ITranscoder<T> transcoder;
    private final Class<T> dummyClass;
    private final Class<? extends BucketDocument<T>> baseDocumentClazz;
    private final Constructor<? extends BucketDocument<T>> baseDocumentContructor;
    private String keyPrefix;

    public GenericCouchbaseTranscoder(Class<T> clazz, Class<? extends BucketDocument<T>> baseDocumentClazz) {
        super();
        try {
            dummyClass = clazz;
            this.baseDocumentClazz = baseDocumentClazz;
            baseDocumentContructor = baseDocumentClazz.getDeclaredConstructor(dummyClass);
        } catch (Exception e) {
            logger.error("Error during transcoder init for class <{}>", clazz.getName(), e);
            throw new RuntimeException("Error during transcoder init for class <" + clazz.getName() + ">");
        }
    }

    public GenericCouchbaseTranscoder(ITranscoder<T> transcoder, Class<? extends BucketDocument<T>> baseDocumentClazz) {
        this(transcoder.getBaseClass(),baseDocumentClazz);
        setTranscoder(transcoder);
    }


    public final ITranscoder<T> getTranscoder(){return transcoder;}

    public final void setTranscoder(ITranscoder<T> transcoder){this.transcoder=transcoder;}


    @Override
    public BucketDocument<T> newDocument(String id, int expiry, T content, long cas) {
        return newDocument(id,expiry,content,cas,null);
    }

    @Override
    public BucketDocument<T> newDocument(String id, int expiry, T content, long cas, MutationToken mutationToken) {
        id = ICouchbaseBucket.Utils.extractKey(keyPrefix,id);
        content.getBaseMeta().setKey(id);
        content.getBaseMeta().setCas(cas);
        content.getBaseMeta().setExpiry(expiry);
        if(mutationToken!=null){
            content.getBaseMeta().setVbucketID(mutationToken.vbucketID());
            content.getBaseMeta().setVbucketUUID(mutationToken.vbucketUUID());
            content.getBaseMeta().setSequenceNumber(mutationToken.sequenceNumber());
        }
        try {
            return baseDocumentContructor.newInstance(content);
        }
        catch (IllegalAccessException|InstantiationException|InvocationTargetException e) {
            throw new DocumentSetUpException("Error during setup", e);
        }
    }

    @Override
    public BucketDocument<T> newDocument(T baseDocument){
        try {
            BucketDocument<T> newDocument=baseDocumentContructor.newInstance(baseDocument);
            if(keyPrefix!=null){
                newDocument.setKeyPrefix(keyPrefix);
            }
            return newDocument;
        }
        catch (IllegalAccessException|InstantiationException|InvocationTargetException e) {
            throw new DocumentSetUpException("Error during setup", e);
        }
    }

    @Override
    public Class<BucketDocument<T>> documentType() {
        return (Class<BucketDocument<T>>)baseDocumentClazz;
    }


    @Override
    public BucketDocument<T> decode(String id, ByteBuf content, long cas, int expiry, int flags, ResponseStatus status) {
        return newDocument(id,expiry,transcoder.decode(content.array()),cas);
    }

    @Override
    public Tuple2<ByteBuf, Integer> encode(BucketDocument<T> document){
        return Tuple.create(Unpooled.wrappedBuffer(transcoder.encode(document.content())), document.content().getBaseMeta().getEncodedFlags());
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    @Override
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
}