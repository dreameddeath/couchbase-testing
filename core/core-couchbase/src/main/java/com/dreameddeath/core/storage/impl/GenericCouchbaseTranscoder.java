package com.dreameddeath.core.storage.impl;

import com.couchbase.client.core.lang.Tuple;
import com.couchbase.client.core.lang.Tuple2;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.deps.io.netty.buffer.ByteBuf;
import com.couchbase.client.deps.io.netty.buffer.Unpooled;
import com.dreameddeath.core.exception.storage.DocumentSetUpException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.storage.BucketDocument;
import com.dreameddeath.core.storage.ICouchbaseTranscoder;
import com.dreameddeath.core.transcoder.ITranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by ceaj8230 on 12/10/2014.
 */
public class GenericCouchbaseTranscoder<T extends CouchbaseDocument> implements ICouchbaseTranscoder<T> {
    private final static Logger logger = LoggerFactory.getLogger(GenericCouchbaseTranscoder.class);
    private ITranscoder<T> _transcoder;
    private final Class<T> _dummyClass;
    private final Class<? extends BucketDocument<T>> _baseDocumentClazz;
    private final Constructor<? extends BucketDocument<T>> _baseDocumentContructor;

    public GenericCouchbaseTranscoder(Class<T> clazz, Class<? extends BucketDocument<T>> baseDocumentClazz) {
        super();
        try {
            _dummyClass = clazz;
            _baseDocumentClazz = baseDocumentClazz;
            _baseDocumentContructor = _baseDocumentClazz.getDeclaredConstructor(_dummyClass);

        } catch (Exception e) {
            logger.error("Error during transcoder init for class <{}>", clazz.getName(), e);
            throw new RuntimeException("Error during transcoder init for class <" + clazz.getName() + ">");
        }
    }

    public GenericCouchbaseTranscoder(ITranscoder<T> transcoder, Class<? extends BucketDocument<T>> baseDocumentClazz) {
        this(transcoder.getBaseClass(),baseDocumentClazz);
        setTranscoder(transcoder);
    }


    public final ITranscoder<T> getTranscoder(){return _transcoder;};
    public final void setTranscoder(ITranscoder<T> transcoder){_transcoder=transcoder;};


    @Override
    public BucketDocument<T> newDocument(String id, int expiry, T content, long cas) {
        content.getBaseMeta().setKey(id);
        content.getBaseMeta().setCas(cas);
        content.getBaseMeta().setExpiry(expiry);
        try {
            return _baseDocumentContructor.newInstance(content);
        }
        catch (IllegalAccessException|InstantiationException|InvocationTargetException e) {
            throw new DocumentSetUpException("Error during setup", e);
        }
    }

    @Override
    public BucketDocument<T> newDocument(T baseDocument){
        try {
            return _baseDocumentContructor.newInstance(baseDocument);
        }
        catch (IllegalAccessException|InstantiationException|InvocationTargetException e) {
            throw new DocumentSetUpException("Error during setup", e);
        }
    }

    @Override
    public Class<BucketDocument<T>> documentType() {
        return (Class<BucketDocument<T>>)_baseDocumentClazz;
    }


    @Override
    public BucketDocument<T> decode(String id, ByteBuf content, long cas, int expiry, int flags, ResponseStatus status) {
        return newDocument(id,expiry,_transcoder.decode(content.array()),cas);
    }

    @Override
    public Tuple2<ByteBuf, Integer> encode(BucketDocument<T> document){
        return Tuple.create(Unpooled.wrappedBuffer(_transcoder.encode(document.content())), document.content().getBaseMeta().getEncodedFlags());
    }
}