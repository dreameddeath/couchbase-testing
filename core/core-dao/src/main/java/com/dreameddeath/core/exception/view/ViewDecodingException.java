package com.dreameddeath.core.exception.view;

import com.couchbase.client.java.document.json.JsonObject;

/**
 * Created by ceaj8230 on 22/12/2014.
 */
public class ViewDecodingException extends Exception {
    JsonObject _sourceObj;

    public ViewDecodingException(JsonObject sourceObj, String message, Throwable e){
        super(message,e);
        _sourceObj = sourceObj;
    }
}
