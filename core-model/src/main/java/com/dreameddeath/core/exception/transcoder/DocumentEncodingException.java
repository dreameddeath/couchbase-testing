package com.dreameddeath.core.exception.transcoder;

import com.dreameddeath.core.model.common.RawCouchbaseDocument;

/**
 * Created by ceaj8230 on 16/09/2014.
 */
public class DocumentEncodingException extends RuntimeException {
    private RawCouchbaseDocument _doc;
    public DocumentEncodingException(RawCouchbaseDocument doc,String message,Throwable e){
        super(message,e);
    }
}
