package com.dreameddeath.core.transcoder;

import com.dreameddeath.core.exception.transcoder.DocumentDecodingException;
import com.dreameddeath.core.exception.transcoder.DocumentEncodingException;
import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by ceaj8230 on 21/11/2014.
 */
public interface ITranscoder<T extends CouchbaseDocument> {
    public Class<T> getBaseClass();
    public T decode(byte[] buf) throws DocumentDecodingException;
    public byte[] encode(T doc) throws DocumentEncodingException;
}
