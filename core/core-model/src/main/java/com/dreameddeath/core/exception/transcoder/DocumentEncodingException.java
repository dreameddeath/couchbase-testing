package com.dreameddeath.core.exception.transcoder;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by ceaj8230 on 16/09/2014.
 */
public class DocumentEncodingException extends RuntimeException {
    private CouchbaseDocument _doc;
    public DocumentEncodingException(CouchbaseDocument doc,String message,Throwable e){
        super(message,e);
    }
}
