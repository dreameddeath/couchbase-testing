package com.dreameddeath.core.exception.view;

/**
 * Created by ceaj8230 on 22/12/2014.
 */
public class ViewEncodingException extends Exception {
    Object _sourceObj;

    public ViewEncodingException(Object sourceObj, String message, Throwable e){
        super(message,e);
        _sourceObj = sourceObj;
    }


}
