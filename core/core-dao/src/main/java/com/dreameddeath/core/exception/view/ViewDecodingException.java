package com.dreameddeath.core.exception.view;

/**
 * Created by ceaj8230 on 22/12/2014.
 */
public class ViewDecodingException extends Exception {
    Object _sourceObj;

    public ViewDecodingException(Object sourceObj, String message, Throwable e){
        super(message,e);
        _sourceObj = sourceObj;
    }
}
