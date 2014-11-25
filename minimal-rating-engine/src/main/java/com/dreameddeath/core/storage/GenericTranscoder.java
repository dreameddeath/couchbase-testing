package com.dreameddeath.core.storage;

import com.couchbase.client.java.transcoder.Transcoder;
import com.dreameddeath.core.exception.storage.DocumentSetUpException;
import com.dreameddeath.core.model.document.BaseCouchbaseDocument;
import com.dreameddeath.core.model.document.BucketDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by ceaj8230 on 12/10/2014.
 */
public abstract class GenericTranscoder<T extends BaseCouchbaseDocument> implements Transcoder<BucketDocument<T>,T> {
    private final static Logger logger = LoggerFactory.getLogger(GenericTranscoder.class);
    private final Class<T> _dummyClass;
    private final Class<? extends BucketDocument<T>> _baseDocumentClazz;
    private final Constructor<? extends BucketDocument<T>> _baseDocumentContructor;

    public GenericTranscoder(Class<T> clazz, Class<? extends BucketDocument<T>> baseDocumentClazz) {
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

    public final Class<T> getBaseClass(){ return _dummyClass;}

    @Override
    public BucketDocument<T> newDocument(String id, int expiry, T content, long cas) {
        content.getBaseMeta().setKey(id);
        content.getBaseMeta().setCas(cas);
        content.getBaseMeta().setExpiry(expiry);
        try {
            return _baseDocumentContructor.newInstance(content);
        } catch (IllegalAccessException e) {
            throw new DocumentSetUpException("Error during setup", e);
        } catch (InstantiationException e) {
            throw new DocumentSetUpException("Error during setup", e);
        } catch (InvocationTargetException e) {
            throw new DocumentSetUpException("Error during setup", e);
        }
    }

    public BucketDocument<T> newDocument(T baseDocument){
        try {
            return _baseDocumentContructor.newInstance(baseDocument);
        } catch (IllegalAccessException e) {
            throw new DocumentSetUpException("Error during setup", e);
        } catch (InstantiationException e) {
            throw new DocumentSetUpException("Error during setup", e);
        } catch (InvocationTargetException e) {
            throw new DocumentSetUpException("Error during setup", e);
        }
    }

    @Override
    public Class<BucketDocument<T>> documentType() {
        return (Class<BucketDocument<T>>)_baseDocumentClazz;
    }
}